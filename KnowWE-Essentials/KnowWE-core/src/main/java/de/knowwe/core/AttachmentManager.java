package de.knowwe.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.d3web.collections.DefaultMultiMap;
import de.d3web.collections.MultiMap;
import de.d3web.collections.MultiMaps;
import de.d3web.collections.N2MMap;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.kdom.attachment.AttachmentMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static java.util.stream.Collectors.toSet;

/**
 * Manages compilation of attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.06.16
 */
public class AttachmentManager implements EventListener {

	private ArticleManager articleManager;

	private final MultiMap<String, Section<AttachmentType>> pathToSectionsMap = MultiMaps.synchronizedMultiMap(new N2MMap<>());
	private final MultiMap<String, Section<AttachmentType>> articleTitleToSectionsMap = MultiMaps.synchronizedMultiMap(new DefaultMultiMap<>());
	private boolean articlesInitialized = false;

	public AttachmentManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
		EventManager.getInstance().registerListener(this);
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Arrays.asList(AttachmentStoredEvent.class, AttachmentDeletedEvent.class,
				ArticleRegisteredEvent.class, InitializedArticlesEvent.class);
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
			if (articlesInitialized) {
				handleArticleUpdate(article);
			}
			else {
				addAttachmentSectionsOfNewArticle(article);
			}

		}
		else if (event instanceof InitializedArticlesEvent) {
			articlesInitialized = true;
			initializeAttachments();
		}
	}

	private synchronized void handleArticleUpdate(Article article) {
		Set<Section<AttachmentType>> outdatedAttachmentSections = removeAttachmentSectionsOfLastArticleVersion(article);
		Set<Section<AttachmentType>> currentAttachmentSections = addAttachmentSectionsOfNewArticle(article);

		for (String attachmentPath : getNewPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			Set<Section<AttachmentType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (values.size() != 1) return;
			createAndRegisterAttachmentArticle(attachmentPath);
		}

		for (String attachmentPath : getRemovedPathsOfArticle(outdatedAttachmentSections, currentAttachmentSections)) {
			Set<Section<AttachmentType>> values = pathToSectionsMap.getValues(attachmentPath);
			if (values != null && !values.isEmpty()) return;
			deleteAttachmentArticle(attachmentPath);
		}

	}

	private Set<Section<AttachmentType>> addAttachmentSectionsOfNewArticle(Article article) {
		registerCompiledAttachmentSections(article);
		Set<Section<AttachmentType>> addedAttachmentSections = articleTitleToSectionsMap.getValues(article.getTitle());
		return addedAttachmentSections == null ? Collections.emptySet() : addedAttachmentSections;
	}

	@NotNull
	private Set<Section<AttachmentType>> removeAttachmentSectionsOfLastArticleVersion(Article article) {
		Set<Section<AttachmentType>> lastVersionAttachments = articleTitleToSectionsMap.removeKey(article.getTitle());
		if (lastVersionAttachments == null) lastVersionAttachments = Collections.emptySet();
		for (Section<AttachmentType> attachmentSection : lastVersionAttachments) {
			pathToSectionsMap.removeValue(attachmentSection);
		}
		return lastVersionAttachments;
	}

	@NotNull
	private Set<String> getRemovedPathsOfArticle(Set<Section<AttachmentType>> lastVersionAttachments, Set<Section<AttachmentType>> newVersionAttachments) {
		return getDiff(lastVersionAttachments, newVersionAttachments);
	}

	@NotNull
	private Set<String> getNewPathsOfArticle(Set<Section<AttachmentType>> lastVersionAttachments, Set<Section<AttachmentType>> newVersionAttachments) {
		return getDiff(newVersionAttachments, lastVersionAttachments);
	}

	private Set<String> getDiff(Set<Section<AttachmentType>> addAll, Set<Section<AttachmentType>> removeAll) {
		Set<String> registrationCandidates = new HashSet<>();
		registrationCandidates.addAll(toPaths(addAll));
		registrationCandidates.removeAll(toPaths(removeAll));
		return registrationCandidates;
	}

	private Set<String> toPaths(Collection<Section<AttachmentType>> attachmentSections) {
		return attachmentSections.stream().map(AttachmentType::getPath).collect(toSet());
	}

	private void registerCompiledAttachmentSections(Article article) {
		for (Section<AttachmentMarkup> attachmentSection : Sections.successors(article, AttachmentMarkup.class)) {
			if (!"true".equals(DefaultMarkupType.getAnnotation(attachmentSection, AttachmentMarkup.COMPILE))) continue;
			Section<AttachmentType> attachmentTypeSection = $(attachmentSection).successor(AttachmentType.class)
					.getFirst();
			if (attachmentTypeSection == null) continue;
			String path = AttachmentType.getPath(attachmentTypeSection);
			pathToSectionsMap.put(path, attachmentTypeSection);
			articleTitleToSectionsMap.put(attachmentTypeSection.getTitle(), attachmentTypeSection);
		}
	}

	private void initializeAttachments() {
		ArrayList<String> paths;
		// create copy in case attachment contains additional compiling attachment markups...
		synchronized (pathToSectionsMap) {
			paths = new ArrayList<>(pathToSectionsMap.keySet());
		}
		paths.parallelStream().forEach(this::createAndRegisterAttachmentArticle);
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
			articleManager.registerArticle(Article.createArticle(attachmentText, attachmentPath,
					articleManager.getWeb()));
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
		articleManager.deleteArticle(articleManager.getArticle(attachmentPath));
	}

	private boolean isCompiledAttachment(String attachmentPath) {
		Set<Section<AttachmentType>> attachmentTypeSections = pathToSectionsMap.getValues(attachmentPath);
		if (attachmentTypeSections == null) return false;
		for (Section<AttachmentType> markupSection : attachmentTypeSections) {
			if (Sections.isLive(markupSection)) return true;
		}
		return false;
	}

}
