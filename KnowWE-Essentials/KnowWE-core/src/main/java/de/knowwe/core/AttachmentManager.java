package de.knowwe.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.collections.N2MMap;
import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
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

	private final ArticleManager articleManager;

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
			registerAttachment(attachmentStoredEvent.getParentName(), attachmentStoredEvent.getFileName());
		}
		else if (event instanceof AttachmentDeletedEvent) {
			AttachmentDeletedEvent attachmentDeletedEvent = (AttachmentDeletedEvent) event;
			unregisterAttachment(attachmentDeletedEvent.getPath(), attachmentDeletedEvent.getFileName());
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
			Log.severe("Exception while fetching attachment on page " + section.getTitle(), e);
			return null;
		}
	}

	private synchronized void handleArticleUpdate(Article article) {
		Set<Section<AttachmentCompileType>> outdatedAttachmentSections = removeAttachmentSectionsOfLastArticleVersion(article);
		Set<Section<AttachmentCompileType>> currentAttachmentSections = addAttachmentSectionsOfNewArticle(article);

		for (String attachmentPath : getNewPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			@NotNull Set<Section<AttachmentCompileType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (values.size() != 1) return;
			createAndRegisterAttachmentArticle(attachmentPath);
		}

		for (String attachmentPath : getRemovedPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			@NotNull Set<Section<AttachmentCompileType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (!values.isEmpty()) return;
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
		return attachmentSections.stream().map(s -> getWikiAttachment(s)).filter(Objects::nonNull).map(WikiAttachment::getPath).collect(toSet());
	}

	private void registerCompiledAttachmentSections(Article article) {
		for (Section<AttachmentCompileType> attachmentSection : Sections.successors(article, AttachmentCompileType.class)) {
			WikiAttachment wikiAttachment = getWikiAttachment(attachmentSection);
			if (wikiAttachment == null) {
				continue;
			}
			String path = wikiAttachment.getPath();
			pathToSectionsMap.put(path, attachmentSection);
			articleTitleToSectionsMap.put(attachmentSection.getTitle(), attachmentSection);
		}
	}

	private void registerAttachment(@NotNull String parent, @NotNull String fileName) {

		String attachmentPath = asPath(parent, fileName);
		if (isCompiledAttachment(attachmentPath)) {
			createAndRegisterAttachmentArticle(attachmentPath);
		}
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
			Log.severe("Unable to compile attachment " + attachmentPath, e);
		}
	}

	private String asPath(String parent, String fileName) {
		return parent + "/" + fileName;
	}

	private void unregisterAttachment(@NotNull String parent, String fileName) {
		String attachmentPath = asPath(parent, fileName);
		if (isCompiledAttachment(attachmentPath)) {
			deleteAttachmentArticle(attachmentPath);
		}
	}

	private void deleteAttachmentArticle(String attachmentPath) {
		articleManager.deleteArticle(attachmentPath);
	}

	private boolean isCompiledAttachment(String attachmentPath) {
		Set<Section<AttachmentCompileType>> attachmentTypeSections = pathToSectionsMap.getValues(attachmentPath);
		for (Section<AttachmentCompileType> markupSection : attachmentTypeSections) {
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
