package de.d3web.we.kdom.kopic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.MessageUtils;
import de.d3web.we.wikiConnector.ConnectorAttachment;

public class ResourceHandler extends D3webSubtreeHandler<ResourceType> {

	private final static class ByteArrayResource implements Resource {

		private final String path;
		private final byte[] bytes;

		private ByteArrayResource(String path, byte[] bytes) {
			this.path = path;
			this.bytes = bytes;
		}

		@Override
		public long getSize() {
			return bytes.length;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(bytes);
		}

		@Override
		public String getPathName() {
			return path;
		}
	}

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ResourceType> section) {
		KnowledgeBaseManagement kbm = getKBM(article);
		if (kbm == null) return null;

		String content = DefaultMarkupType.getContent(section);
		String destinationPath =
				DefaultMarkupType.getAnnotation(section, ResourceType.ANNOTATION_PATH);
		String sourcePath =
				DefaultMarkupType.getAnnotation(section, ResourceType.ANNOTATION_SRC);

		boolean hasContent = content != null && !content.trim().isEmpty();
		boolean hasDestinationPath = destinationPath != null && !destinationPath.trim().isEmpty();
		boolean hasSourcePath = sourcePath != null && !sourcePath.trim().isEmpty();

		// prepare the resource to be added
		Resource resource = null;
		if (sourcePath == null) {
			// if we have no src annotation specified
			// the target path must be specified
			if (!hasDestinationPath) {
				return MessageUtils.syntaxErrorAsList(
						"missing pathname to store the content into");
			}
			// we use the content for a text file
			// the content must not be empty
			// (this might be a unwanted restriction for the user
			// but it is for its own security to get notice of missing content)
			if (!hasContent) {
				return MessageUtils.syntaxErrorAsList(
						"missing attachment or content block to be stored");
			}
			resource = new ByteArrayResource(destinationPath, content.getBytes());
		}
		else {
			// otherwise search all attachments of the wiki
			// remember that the article name is optional for local attachments
			String sourceFile;
			String sourceArticle;
			if (sourcePath.contains("/")) {
				int index = sourcePath.indexOf('/');
				sourceFile = sourcePath.substring(index + 1);
				sourceArticle = sourcePath.substring(0, index);
			}
			else {
				sourceFile = sourcePath.trim();
				sourceArticle = section.getArticle().getTitle();
			}
			// is we have no destination path, we use the attachments path
			if (destinationPath == null) {
				destinationPath = sourceArticle + "/" + sourceFile;
			}
			else if (destinationPath.trim().isEmpty()) {
				return MessageUtils.syntaxErrorAsList("empty destination path");
			}
			// do the search
			Collection<ConnectorAttachment> attachments =
					KnowWEEnvironment.getInstance().getWikiConnector().getAttachments();
			for (ConnectorAttachment attachment : attachments) {
				if (!attachment.getFileName().equalsIgnoreCase(sourceFile)) continue;
				if (!attachment.getParentName().equalsIgnoreCase(sourceArticle)) continue;
				resource = new WikiAttachmentResource(destinationPath, attachment);
				break;
			}
			if (resource == null) {
				return MessageUtils.syntaxErrorAsList("no attachment " + sourcePath + " found");
			}
		}

		// and add the resource to the knowledge base
		KnowledgeBase kb = kbm.getKnowledgeBase();
		kb.addResouce(resource);

		// if we have both content and source, warn this
		// (but adding was still successful!)
		if (hasContent && hasSourcePath) {
			return MessageUtils.asList(new KDOMWarning() {

				@Override
				public String getVerbalization() {
					return "both src and content is specified, the content has been ignored";
				}
			});
		}
		else {
			return null;
		}
	}

}
