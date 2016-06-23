package de.knowwe.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import de.d3web.collections.WeakValueHashMap;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.kdom.attachment.AttachmentMarkup;

/**
 * Manages compilation of attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.06.16
 */
public class AttachmentManager implements EventListener {

	private ArticleManager articleManager;

	private final Map<String, Section<AttachmentMarkup>> attachmentMarkupSections = new WeakValueHashMap<>();

	public AttachmentManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Arrays.asList(AttachmentStoredEvent.class, AttachmentDeletedEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (event instanceof AttachmentStoredEvent) {
			registerAttachment(((AttachmentStoredEvent) event).getPath());
		}
		else if (event instanceof AttachmentDeletedEvent) {
			unregisterAttachment(((AttachmentDeletedEvent) event).getPath());
		}
	}

	public void registerAttachment(@NotNull String attachmentPath) {

//		if (isCompiledAttachment(attachmentPath)) {
//			try {
//				WikiAttachment attachment = KnowWEUtils.getAttachment(event.getParentName(), event.getFileName());
//				String attachmentText = Strings.readStream(attachment.getInputStream());
//				articleManager.registerArticle(Article.createArticle(attachmentText, getAttachmentPath(),
//						articleManager.getWeb()));
//			}
//			catch (IOException e) {
//				Log.severe("Unable to compile attachment " + getAttachmentPath(), e);
//			}
//		}

	}

	public void unregisterAttachment(@NotNull String attachmentPath) {
//		Section<AttachmentMarkup> attachmentMarkupSection = attachmentMarkupSections.get(attachmentPath);
//		return attachmentMarkupSection != null && attachmentMarkupSection.is

	}

//	private static boolean isCompiledAttachment(String attachmentPath) {
//		List<Section<AttachmentMarkup>> attachmentMarkups = Sections.successors(article, AttachmentMarkup.class);
//		for (Section<AttachmentMarkup> attachmentMarkup : attachmentMarkups) {
//			Section<AttachmentType> attachmentTypeSection = Sections.successor(attachmentMarkup, AttachmentType.class);
//			if (attachmentTypeSection == null) continue;
//			String path = AttachmentType.getPath(attachmentTypeSection);
//			if (path.equals(attachmentPath)) {
//				return "true".equals(DefaultMarkupType.getAnnotation(attachmentMarkup, AttachmentMarkup.COMPILE));
//			}
//		}
//		return false;
//	}
//
//	public void registerAttachmentMarkupSection(Section<AttachmentMarkup> attachmentMarkupSection) {
//		if (!"true".equals(DefaultMarkupType.getAnnotation(attachmentMarkupSection, AttachmentMarkup.COMPILE))) return;
//		Section<AttachmentType> attachmentTypeSection = $(attachmentMarkupSection).successor(AttachmentType.class)
//				.getFirst();
//		if (attachmentTypeSection == null) return;
//		attachmentMarkupSections.put(AttachmentType.getPath(attachmentTypeSection), attachmentMarkupSection);
//	}

}
