package de.knowwe.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.collections.N2MMap;
import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.event.WikiContentReplacedEvent;

import static java.util.stream.Collectors.toSet;

/**
 * Manages compilation of attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.06.16
 */
public class AttachmentManager implements EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentManager.class);

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final ArticleManager articleManager;
	private final Object queueLock = new Object();
	private final Set<String> registrationQueue = new LinkedHashSet<>();
	private final Set<String> unregistrationQueue = new LinkedHashSet<>();
	private final Set<String> attachmentArticles = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final MultiMap<String, Section<AttachmentCompileType>> pathToSectionsMap = MultiMaps.synchronizedMultiMap(new N2MMap<>());
	/**
	 * All sections compiling attachments by the title of their article, also the ones that have no path at the moment
	 */
	private final MultiMap<String, Section<AttachmentCompileType>> articleTitleToSectionsMap = MultiMaps.synchronizedMultiMap(new DefaultMultiMap<>());
	/**
	 * The sections deciding about the compilers of an attachment, as they were when the path was registered or last
	 * refreshed: section compiling attachments -> path -> condition sections. Only for paths having such a condition,
	 * only accessed while holding the monitor of this manager.
	 */
	private final Map<Section<AttachmentCompileType>, Map<String, Set<Section<?>>>> compileConditions = new HashMap<>();
	/**
	 * The texts of the package rules of the articles compiling attachments, by the lower case title of the article,
	 * to detect changes of the packages. Only accessed while holding the monitor of this manager.
	 */
	private final Map<String, List<String>> packageStatements = new HashMap<>();
	/**
	 * Whether the articles of the wiki are initialized. Until then, the articles of compiled attachments are queued into
	 * the registration frame of the initialization instead of being registered: the initialization queues the articles
	 * of the wiki on the workers of a parallel stream, which handle the resulting events, and a worker opening the
	 * article manager would wait for the frame, while its owner waits for the workers. Reset when the wiki content is
	 * replaced, as the wiki then initializes its articles again.
	 */
	private volatile boolean allArticlesInitialized = false;
	/** From this duration on, a refresh of the compiled attachment paths is logged as info instead of debug. */
	private static final long REFRESH_LOG_THRESHOLD_MILLIS = 50;

	public AttachmentManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
		synchronized (KnowWEUtils.getEventManager(articleManager)) {
			KnowWEUtils.getEventManager(articleManager).registerListener(this, EventManager.RegistrationType.WEAK);
		}
	}

	/**
	 * Stops the background thread of this manager and unregisters it from the event manager. Called when the owning
	 * environment is closed.
	 */
	public void shutdown() {
		KnowWEUtils.getEventManager(articleManager).unregister(this);
		executor.shutdownNow();
	}

	/**
	 * Stops the background thread like {@link #shutdown()} and waits for it to end. For tests that must not leak the
	 * thread into the next test; the servlet shutdown does not wait.
	 *
	 * @return whether the thread ended within the given time
	 */
	public boolean shutdownAndAwait(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		shutdown();
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Arrays.asList(AttachmentStoredEvent.class, AttachmentDeletedEvent.class,
				ArticleRegisteredEvent.class, InitializedArticlesEvent.class, WikiContentReplacedEvent.class,
				FullParseEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (event instanceof AttachmentStoredEvent attachmentStoredEvent) {
			registerAttachment(attachmentStoredEvent.getPath());
		}
		else if (event instanceof AttachmentDeletedEvent attachmentDeletedEvent) {
			unregisterAttachment(attachmentDeletedEvent.getPath());
		}
		else if (event instanceof ArticleRegisteredEvent articleRegisteredEvent) {
			handleArticleUpdate(articleRegisteredEvent.getArticle());
		}
		else if (event instanceof FullParseEvent fullParseEvent) {
			doFullParseOfCompiledAttachments(fullParseEvent.getArticles());
		}
		else if (event instanceof InitializedArticlesEvent) {
			allArticlesInitialized = true;
		}
		else if (event instanceof WikiContentReplacedEvent) {
			allArticlesInitialized = false;
		}
	}

	private void doFullParseOfCompiledAttachments(Collection<Article> articles) {
		Collection<String> queuedArticles = articleManager.getQueuedArticles()
				.stream()
				.map(Article::getTitle)
				.collect(toSet());
		Set<String> compiledAttachmentArticleTitles = articles.stream()
				.flatMap(article -> articleTitleToSectionsMap.getValues(article.getTitle()).stream())
				.filter(section -> section.get().isCompilingTheAttachment(section))
				.flatMap(section -> pathToSectionsMap.getKeys(section).stream())
				.filter(title -> !queuedArticles.contains(title))
				.collect(toSet());
		for (String attachmentArticleTitle : compiledAttachmentArticleTitles) {
			// does nothing if the attachment does not exist
			createAndRegisterAttachmentArticle(attachmentArticleTitle);
		}
	}

	private synchronized void handleArticleUpdate(Article article) {
		Set<String> outdatedPaths = removeAttachmentSectionsOfLastArticleVersion(article);
		// first register the sections, then read their paths: see refreshCompiledAttachmentPaths()
		Set<String> currentPaths = addAttachmentSectionsOfNewArticle(article);

		Set<String> newPaths = new LinkedHashSet<>(currentPaths);
		newPaths.removeAll(outdatedPaths);
		Set<String> removedPaths = new LinkedHashSet<>(outdatedPaths);
		removedPaths.removeAll(currentPaths);
		// The attachments the article still compiles are compiled again together with the article, if required:
		// - their packages are the ones of the compiling markup or of the article, which may have changed
		// - the markup may have switched compiling the attachment on or off
		// - the attachment itself may have been stored right before, e.g. by an update that also changes the article
		// Otherwise they are left alone, so the compilers of unchanged attachments have nothing to do.
		// (remembered for all articles with sections compiling attachments, also if these have no path at the moment)
		boolean packagesChanged = updatePackageStatements(article,
				!articleTitleToSectionsMap.getValues(article.getTitle()).isEmpty());
		Set<String> changedPaths = new LinkedHashSet<>();
		for (String path : currentPaths) {
			if (!outdatedPaths.contains(path)) continue;
			if (packagesChanged || countCompilingSections(path) == 0 || !isAttachmentArticleUpToDate(path)) {
				changedPaths.add(path);
			}
		}
		applyPathChanges(newPaths, removedPaths, changedPaths);
	}

	/**
	 * Remembers the package statements of the given article, as far as it compiles attachments: the package rules of
	 * its markups (including the ones compiling the attachments) and its default packages.
	 *
	 * @return whether there were other package statements for the previous version of the article
	 */
	private boolean updatePackageStatements(Article article, boolean compilesAttachments) {
		String key = article.getTitle().toLowerCase();
		if (!compilesAttachments) {
			packageStatements.remove(key);
			return false;
		}
		List<String> current = Sections.successors(article, PackageRule.class).stream().map(Section::getText).toList();
		List<String> previous = packageStatements.put(key, current);
		return previous != null && !previous.equals(current);
	}

	/**
	 * Recalculates the attachment paths of all sections compiling attachments and updates the attachment articles
	 * accordingly: an article is registered for each attachment that is compiled now and was not before, and the
	 * articles of attachments that are no longer compiled are deleted. Additionally, the article of an attachment is
	 * registered again if the sections deciding about the compilers of the attachment have changed (see
	 * {@link AttachmentCompileType#getCompileConditionSections(Section, String)}), so its packages are registered
	 * anew.
	 * <p>
	 * To be called whenever the information the paths of a section depend on has changed outside the article of the
	 * section itself, e.g. by the {@link de.knowwe.core.compile.variables.VariableIndex} after a variable definition
	 * was registered. The caller has to make the new information visible <b>before</b> calling this method and must
	 * not hold a lock that is also taken while reading the information. As this manager in turn registers a section
	 * before it reads its paths, the result is the same for any order in which the article of the section and the
	 * article with the information are registered, also in different threads: whoever comes second sees what the
	 * first one has written.
	 * <p>
	 * Like any registration of articles, this method must be called within the registration frame of the article
	 * manager while the articles of the wiki are initialized, and from the thread owning the frame afterward. Both is
	 * given if it is called while handling an {@link ArticleRegisteredEvent}.
	 */
	public synchronized void refreshCompiledAttachmentPaths() {
		long start = System.currentTimeMillis();
		int checkedSections = 0;
		Set<String> newPaths = new LinkedHashSet<>();
		Set<String> removedPaths = new LinkedHashSet<>();
		Set<String> changedPaths = new LinkedHashSet<>();
		for (Section<AttachmentCompileType> section : new ArrayList<>(articleTitleToSectionsMap.valueSet())) {
			// sections of outdated article versions are replaced as soon as the new version is handled
			if (!Sections.isLive(section)) continue;
			checkedSections++;
			Set<String> registeredPaths = new LinkedHashSet<>(pathToSectionsMap.getKeys(section));
			Set<String> currentPaths = getCompiledAttachmentPaths(section);
			for (String path : currentPaths) {
				if (registeredPaths.contains(path)) {
					if (updateCompileConditions(section, path)) changedPaths.add(path);
				}
				else {
					registerPath(section, path);
					newPaths.add(path);
				}
			}
			for (String path : registeredPaths) {
				if (currentPaths.contains(path)) continue;
				unregisterPath(section, path);
				removedPaths.add(path);
			}
		}
		long checked = System.currentTimeMillis();
		// only now change the articles: this causes events and may lead to reentrant calls of this method
		applyPathChanges(newPaths, removedPaths, changedPaths);

		long applied = System.currentTimeMillis();
		String message = "Refreshed the compiled attachments of " + checkedSections + " sections in "
				+ (checked - start) + "ms: " + newPaths.size() + " new, " + removedPaths.size() + " removed, "
				+ changedPaths.size() + " to compile again; registering their articles took " + (applied - checked) + "ms";
		if (applied - start >= REFRESH_LOG_THRESHOLD_MILLIS) {
			LOGGER.info(message);
		}
		else {
			LOGGER.debug(message);
		}
	}

	/**
	 * Registers, re-registers and deletes the attachment articles for the given changes of the registered paths.
	 *
	 * @param newPaths     the paths that were added for a section
	 * @param removedPaths the paths that were removed for a section
	 * @param changedPaths the paths that are still registered, but have to be compiled again (or no longer), e.g.
	 *                     because their packages or compile conditions have changed
	 */
	private void applyPathChanges(Collection<String> newPaths, Collection<String> removedPaths, Collection<String> changedPaths) {
		for (String attachmentPath : newPaths) {
			// if there are more sections compiling the attachment, the article already exists
			if (countCompilingSections(attachmentPath) != 1) continue;
			createAndRegisterAttachmentArticle(attachmentPath);
		}

		if (!changedPaths.isEmpty()) {
			// articles that are (re-)registered in the current frame anyway will get the current packages
			Set<String> queuedArticles = articleManager.getQueuedArticles().stream().map(Article::getTitle).collect(toSet());
			for (String attachmentPath : changedPaths) {
				if (newPaths.contains(attachmentPath) || queuedArticles.contains(attachmentPath)) continue;
				if (countCompilingSections(attachmentPath) > 0) {
					createAndRegisterAttachmentArticle(attachmentPath);
				}
				else {
					// no longer compiled, e.g. the markup has switched off compiling the attachment
					deleteAttachmentArticle(attachmentPath);
				}
			}
		}

		for (String attachmentPath : removedPaths) {
			if (countCompilingSections(attachmentPath) > 0) continue;
			deleteAttachmentArticle(attachmentPath);
		}
	}

	private long countCompilingSections(String attachmentPath) {
		return pathToSectionsMap.getValues(attachmentPath).stream()
				.filter(s -> s.get().isCompilingTheAttachment(s))
				.count();
	}

	/**
	 * Registers the attachment sections of the given article with their paths.
	 *
	 * @return the paths registered for the sections of the article
	 */
	@NotNull
	private Set<String> addAttachmentSectionsOfNewArticle(Article article) {
		Set<String> paths = new LinkedHashSet<>();
		for (Section<AttachmentCompileType> attachmentSection : Sections.successors(article, AttachmentCompileType.class)) {
			// also register sections that have no path at the moment, they may get one with the next refresh
			articleTitleToSectionsMap.put(attachmentSection.getTitle(), attachmentSection);
			for (String path : getCompiledAttachmentPaths(attachmentSection)) {
				registerPath(attachmentSection, path);
				paths.add(path);
			}
		}
		return paths;
	}

	/**
	 * Removes the attachment sections of the previous version of the given article.
	 *
	 * @return the paths that were registered for the sections of the previous version
	 */
	@NotNull
	private Set<String> removeAttachmentSectionsOfLastArticleVersion(Article article) {
		Set<String> paths = new LinkedHashSet<>();
		Set<Section<AttachmentCompileType>> lastVersionAttachments = articleTitleToSectionsMap.removeKey(article.getTitle());
		for (Section<AttachmentCompileType> attachmentSection : lastVersionAttachments) {
			for (String path : new ArrayList<>(pathToSectionsMap.getKeys(attachmentSection))) {
				unregisterPath(attachmentSection, path);
				paths.add(path);
			}
		}
		return paths;
	}

	@NotNull
	private Set<String> getCompiledAttachmentPaths(Section<AttachmentCompileType> section) {
		Set<String> paths = new LinkedHashSet<>(section.get().getCompiledAttachmentPaths(section));
		paths.remove(null);
		return paths;
	}

	private void registerPath(Section<AttachmentCompileType> section, String path) {
		pathToSectionsMap.put(path, section);
		attachmentArticles.add(path);
		updateCompileConditions(section, path);
	}

	private void unregisterPath(Section<AttachmentCompileType> section, String path) {
		pathToSectionsMap.remove(path, section);
		if (pathToSectionsMap.getValues(path).isEmpty()) {
			attachmentArticles.remove(path);
		}
		Map<String, Set<Section<?>>> conditionsOfSection = compileConditions.get(section);
		if (conditionsOfSection != null) {
			conditionsOfSection.remove(path);
			if (conditionsOfSection.isEmpty()) compileConditions.remove(section);
		}
	}

	/**
	 * Remembers the sections that currently decide about the compilers of the given attachment of the given section.
	 *
	 * @return whether they differ from the ones remembered before
	 */
	private boolean updateCompileConditions(Section<AttachmentCompileType> section, String path) {
		Collection<? extends Section<?>> conditionSections = section.get().getCompileConditionSections(section, path);
		Set<Section<?>> current = conditionSections == null ? null : new HashSet<>(conditionSections);
		Map<String, Set<Section<?>>> conditionsOfSection = compileConditions.get(section);
		Set<Section<?>> previous = conditionsOfSection == null ? null : conditionsOfSection.get(path);
		if (Objects.equals(previous, current)) return false;
		if (current != null) {
			compileConditions.computeIfAbsent(section, k -> new HashMap<>()).put(path, current);
		}
		else {
			conditionsOfSection.remove(path);
			if (conditionsOfSection.isEmpty()) compileConditions.remove(section);
		}
		return true;
	}

	private void registerAttachment(@NotNull String path) {
		if (isCompiledAttachment(path)) {
			synchronized (queueLock) {
				unregistrationQueue.remove(path);
				registrationQueue.add(path);
			}
			updateRegisteredAndDeletedAttachments();
		}
	}

	private void updateRegisteredAndDeletedAttachments() {
		// try to batch process changes as soon as article manager can be opened,
		// get all (de)registrations that arrived while waiting
		executor.submit(() -> {
			articleManager.open();
			try {
				ArrayList<String> registered;
				ArrayList<String> unregistered;
				synchronized (queueLock) {
					registered = new ArrayList<>(registrationQueue);
					unregistered = new ArrayList<>(unregistrationQueue);
					registrationQueue.clear();
					unregistrationQueue.clear();
				}
				for (String path : registered) {
					// the article may already be up-to-date, if the page compiling the attachment was registered
					// after the attachment was stored, as the attachment is compiled together with its page then
					if (isAttachmentArticleUpToDate(path)) continue;
					createAndRegisterAttachmentArticle(path);
				}
				for (String path : unregistered) {
					deleteAttachmentArticle(path);
				}
			}
			finally {
				articleManager.commit();
			}
		});
	}

	/**
	 * Waits until the attachments stored and deleted so far are handled, i.e. their articles are registered or
	 * deleted. The compilation of these changes may still be running then. For tests.
	 *
	 * @return whether the changes were handled within the given time
	 */
	public boolean awaitPendingChanges(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		try {
			executor.submit(() -> { }).get(timeout, unit);
			return true;
		}
		catch (ExecutionException | TimeoutException e) {
			return false;
		}
	}

	/**
	 * Whether there is an article for the given attachment that already has the current text of the attachment.
	 */
	private boolean isAttachmentArticleUpToDate(String attachmentPath) {
		Article article = articleManager.getArticle(attachmentPath);
		if (article == null) return false;
		try {
			String attachmentText = readAttachment(attachmentPath);
			return attachmentText != null && Article.cleanupText(attachmentText).equals(article.getText());
		}
		catch (IOException e) {
			return false;
		}
	}

	@Nullable
	private String readAttachment(String attachmentPath) throws IOException {
		WikiAttachment attachment = articleManager.getEnvironment().getWikiConnector().getAttachment(attachmentPath);
		if (attachment == null) return null;
		return Strings.readStream(attachment.getInputStream());
	}

	private void createAndRegisterAttachmentArticle(String attachmentPath) {
		try {
			String attachmentText = readAttachment(attachmentPath);
			if (attachmentText == null) return;
			if (allArticlesInitialized) {
				articleManager.registerArticle(attachmentPath, attachmentText);
			}
			else {
				((DefaultArticleManager) articleManager).queueArticle(attachmentPath, attachmentText);
			}
		}
		catch (IOException e) {
			LOGGER.error("Unable to compile attachment " + attachmentPath, e);
		}
	}

	private void unregisterAttachment(@NotNull String path) {
		if (isCompiledAttachment(path)) {
			synchronized (queueLock) {
				registrationQueue.remove(path);
				unregistrationQueue.add(path);
			}
			updateRegisteredAndDeletedAttachments();
		}
	}

	private void deleteAttachmentArticle(String attachmentPath) {
		if (articleManager.getArticle(attachmentPath) == null) return;
		if (allArticlesInitialized) {
			articleManager.deleteArticle(attachmentPath);
		}
		else {
			// while the articles are initialized, other threads than the owner of the registration frame must not
			// open the article manager, so we only queue the deletion like we queue the registration
			((DefaultArticleManager) articleManager).queueArticleDeletion(attachmentPath);
		}
	}

	private boolean isCompiledAttachment(String attachmentPath) {
		Set<Section<AttachmentCompileType>> attachmentTypeSections = pathToSectionsMap.getValues(attachmentPath);
		for (Section<AttachmentCompileType> markupSection : attachmentTypeSections) {
			if (!markupSection.get().isCompilingTheAttachment(markupSection)) continue;
			if (Sections.isLive(markupSection)) return true;
		}
		return false;
	}

	/**
	 * In case the given article represents the content of an attachment compiled via the %%Attachment markup, we check
	 * which {@link Section}s of type {@link AttachmentType} reference the attachment represented by the given article.
	 *
	 * @param article the object for which we check if there are attachment sections
	 */
	public Set<Section<AttachmentCompileType>> getCompilingAttachmentSections(Article article) {
		return pathToSectionsMap.getValues(article.getTitle());
	}

	/**
	 * Checks whether the given article is compiled as an article (e.g. %%InterWikiImport or %%Attachment-Markup with
	 *
	 * @param article the article to check
	 * @return true if the given article is an attachment article, false otherwise
	 * @compile: true)
	 */
	public boolean isAttachmentArticle(Article article) {
		return isAttachmentArticle(article.getTitle());
	}

	/**
	 * Checks whether the given article is compiled as an article (e.g. %%InterWikiImport or %%Attachment-Markup with
	 *
	 * @param title the title of the article to check
	 * @return true if the given article is an attachment article, false otherwise
	 * @compile: true)
	 */
	public boolean isAttachmentArticle(String title) {
		return attachmentArticles.contains(title);
	}

	/**
	 * Checks whether there is any article compiled from an attachment (e.g. via %%InterWikiImport or %%Attachment-Markup
	 * with @compile: true).
	 *
	 * @return true if there is at least one attachment article, false otherwise
	 */
	public boolean hasAttachmentArticles() {
		return !attachmentArticles.isEmpty();
	}
}
