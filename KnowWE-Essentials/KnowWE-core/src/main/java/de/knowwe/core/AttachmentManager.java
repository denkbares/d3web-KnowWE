package de.knowwe.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.InitializedArticlesEvent;

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
	private final MultiMap<String, Section<AttachmentCompileType>> pathToSectionsMap = MultiMaps.synchronizedMultiMap(new N2MMap<>());
	private final MultiMap<String, Section<AttachmentCompileType>> articleTitleToSectionsMap = MultiMaps.synchronizedMultiMap(new DefaultMultiMap<>());
	private boolean allArticlesInitialized = false;

	public AttachmentManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
		synchronized (EventManager.getInstance()) {
			EventManager.getInstance().registerListener(this, EventManager.RegistrationType.WEAK);
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Arrays.asList(AttachmentStoredEvent.class, AttachmentDeletedEvent.class,
				ArticleRegisteredEvent.class, InitializedArticlesEvent.class, FullParseEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (event instanceof AttachmentStoredEvent) {
			AttachmentStoredEvent attachmentStoredEvent = (AttachmentStoredEvent) event;
			registerAttachment(attachmentStoredEvent.getPath());
		}
		else if (event instanceof AttachmentDeletedEvent) {
			AttachmentDeletedEvent attachmentDeletedEvent = (AttachmentDeletedEvent) event;
			unregisterAttachment(attachmentDeletedEvent.getPath());
		}
		else if (event instanceof ArticleRegisteredEvent) {
			Article article = ((ArticleRegisteredEvent) event).getArticle();
			handleArticleUpdate(article);
		}
		else if (event instanceof FullParseEvent) {
			Article article = ((FullParseEvent) event).getArticle();
			doFullParseOfCompiledAttachments(article);
		}
		else if (event instanceof InitializedArticlesEvent) {
			allArticlesInitialized = true;
		}
	}

	private void doFullParseOfCompiledAttachments(Article article) {
		Collection<String> queuedArticles = articleManager.getQueuedArticles()
				.stream()
				.map(Article::getTitle)
				.collect(toSet());
		Set<String> compiledAttachmentArticleTitles = articleTitleToSectionsMap.getValues(article.getTitle())
				.stream()
				.map(this::getWikiAttachment)
				.filter(Objects::nonNull)
				.map(WikiAttachment::getPath)
				.filter(title -> !queuedArticles.contains(title))
				.collect(toSet());
		for (String attachmentArticleTitle : compiledAttachmentArticleTitles) {
			createAndRegisterAttachmentArticle(attachmentArticleTitle);
		}
	}

	@Nullable
	private WikiAttachment getWikiAttachment(Section<AttachmentCompileType> section) {
		try {
			return section.get().getCompiledAttachment(section);
		}
		catch (IOException e) {
			LOGGER.error("Exception while fetching attachment on page " + section.getTitle(), e);
			return null;
		}
	}

	private synchronized void handleArticleUpdate(Article article) {
		Set<Section<AttachmentCompileType>> outdatedAttachmentSections = removeAttachmentSectionsOfLastArticleVersion(article);
		Set<Section<AttachmentCompileType>> currentAttachmentSections = addAttachmentSectionsOfNewArticle(article);

		for (String attachmentPath : getNewPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			@NotNull Set<Section<AttachmentCompileType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (values.stream().filter(s -> s.get().isCompilingTheAttachment(s)).count() != 1) return;
			createAndRegisterAttachmentArticle(attachmentPath);
		}

		for (String attachmentPath : getRemovedPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			@NotNull Set<Section<AttachmentCompileType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (values.stream().anyMatch(s -> s.get().isCompilingTheAttachment(s))) return;
			deleteAttachmentArticle(attachmentPath);
		}
	}

	private @NotNull Set<Section<AttachmentCompileType>> addAttachmentSectionsOfNewArticle(Article article) {
		registerCompiledAttachmentSections(article);
		return articleTitleToSectionsMap.getValues(article.getTitle());
	}

	@NotNull
	private Set<Section<AttachmentCompileType>> removeAttachmentSectionsOfLastArticleVersion(Article article) {
		Set<Section<AttachmentCompileType>> lastVersionAttachments = articleTitleToSectionsMap.removeKey(article.getTitle());
		for (Section<AttachmentCompileType> attachmentSection : lastVersionAttachments) {
			pathToSectionsMap.removeValue(attachmentSection);
		}
		return lastVersionAttachments;
	}

	@NotNull
	private Set<String> getRemovedPathsOfArticle(Set<Section<AttachmentCompileType>> lastVersionAttachments, Set<Section<AttachmentCompileType>> newVersionAttachments) {
		return getDiff(lastVersionAttachments, newVersionAttachments);
	}

	@NotNull
	private Set<String> getNewPathsOfArticle(Set<Section<AttachmentCompileType>> lastVersionAttachments, Set<Section<AttachmentCompileType>> newVersionAttachments) {
		return getDiff(newVersionAttachments, lastVersionAttachments);
	}

	private Set<String> getDiff(Set<Section<AttachmentCompileType>> addAll, Set<Section<AttachmentCompileType>> removeAll) {
		Set<String> registrationCandidates = new HashSet<>(toPaths(addAll));
		registrationCandidates.removeAll(toPaths(removeAll));
		return registrationCandidates;
	}

	private Set<String> toPaths(Collection<Section<AttachmentCompileType>> attachmentSections) {
		return attachmentSections.stream()
				.map(s -> s.get().getCompiledAttachmentPath(s))
				.filter(Objects::nonNull)
				.collect(toSet());
	}

	private void registerCompiledAttachmentSections(Article article) {
		for (Section<AttachmentCompileType> attachmentSection : Sections.successors(article, AttachmentCompileType.class)) {
			String path = attachmentSection.get().getCompiledAttachmentPath(attachmentSection);
			if (path == null) continue;
			pathToSectionsMap.put(path, attachmentSection);
			articleTitleToSectionsMap.put(attachmentSection.getTitle(), attachmentSection);
		}
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

	private void createAndRegisterAttachmentArticle(String attachmentPath) {
		try {
			WikiAttachment attachment = Environment.getInstance().getWikiConnector().getAttachment(attachmentPath);
			if (attachment == null) return;
			String attachmentText = Strings.readStream(attachment.getInputStream());
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
		articleManager.deleteArticle(attachmentPath);
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
	 * Checks whether the given article is compiled via an %%Attachment markup using the @compile: true flag.
	 *
	 * @param article the article to check
	 * @return true if the given article is an attachment article, false otherwise
	 */
	public boolean isAttachmentArticle(Article article) {
		return !pathToSectionsMap.getValues(article.getTitle()).isEmpty();
	}
}
