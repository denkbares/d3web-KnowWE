/*
 * Copyright (C) ${year} denkbares GmbH, Germany
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.d3web.resource;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Adds a file defined by resource markup to the d3web knowledge base.
 * 
 * @author volker_belli
 * @created 13.10.2010
 */
public class ResourceHandler extends D3webSubtreeHandler<ResourceType> {

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<ResourceType> section) {
		KnowledgeBase kb = getKB(article);
		if (kb == null) return null;

		String content = DefaultMarkupType.getContent(section);
		String destinationPath =
				DefaultMarkupType.getAnnotation(section, ResourceType.ANNOTATION_PATH);
		String sourcePath =
				DefaultMarkupType.getAnnotation(section, ResourceType.ANNOTATION_SRC);

		return addResource(section.getArticle(), kb, content, sourcePath, destinationPath);
	}

	/**
	 * Adds a resource into the knowledge base resource storage.
	 * 
	 * @created 11.06.2011
	 * @param article the article of the defining section
	 * @param kb the knowledge base to store the resource into
	 * @param content the content to be stored (or null if an attachment should
	 *        be used)
	 * @param sourcePath the wiki attachment path of the file to be stored
	 * @param destinationPath the resource path to store the content in
	 * @return the errors occurred or null if successful
	 */
	private static Collection<Message> addResource(KnowWEArticle article, KnowledgeBase kb, String content, String sourcePath, String destinationPath) {
		boolean hasContent = content != null && !content.trim().isEmpty();
		boolean hasDestinationPath = destinationPath != null && !destinationPath.trim().isEmpty();
		boolean hasSourcePath = sourcePath != null && !sourcePath.trim().isEmpty();

		// prepare the resource to be added
		Resource resource = null;
		if (sourcePath == null) {
			// if we have no src annotation specified
			// the target path must be specified
			if (!hasDestinationPath) {
				return Messages.asList(Messages.syntaxError(
						"missing pathname to store the content into"));
			}
			// we use the content for a text file
			// the content must not be empty
			// (this might be a unwanted restriction for the user
			// but it is for its own security to get notice of missing content)
			if (!hasContent) {
				return Messages.asList(Messages.syntaxError(
						"missing attachment or content block to be stored"));
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
				sourceArticle = article.getTitle();
			}
			// is we have no destination path, we use the attachments path
			if (destinationPath == null) {
				destinationPath = sourceArticle + "/" + sourceFile;
			}
			else if (destinationPath.trim().isEmpty()) {
				return Messages.asList(Messages.syntaxError("empty destination path"));
			}
			// do the search
			resource = getAttachmentResource(destinationPath, sourceFile, sourceArticle);
			if (resource == null) {
				return Messages.asList(Messages.syntaxError("no attachment " + sourcePath
						+ " found"));
			}
		}

		// and add the resource to the knowledge base
		kb.addResouce(resource);

		// if we have both content and source, warn this
		// (but adding was still successful!)
		if (hasContent && hasSourcePath) {
			return Messages.asList(Messages.warning("both src and content is specified, the content has been ignored"));
		}
		else {
			return null;
		}
	}

	/**
	 * Searches the wiki attachments for the specified attachment, denoted by
	 * "attachmentName" and "articleName". If such an attachment is found, a
	 * resource of this attachment will be constructed and returned. If no such
	 * attachment is found, null is returned. Both, "attachmentName" and
	 * "articleName" are treated case-insensitive.
	 * 
	 * @created 12.06.2011
	 * @param path the pathname for the resource to be constructed
	 * @param attachmentName the filename of the attachment to be searched for
	 * @param articleName the article name of the attachment to be searched for
	 * @return the constructed resource
	 */
	private static Resource getAttachmentResource(String path, String attachmentName, String articleName) {
		Collection<ConnectorAttachment> attachments =
				KnowWEEnvironment.getInstance().getWikiConnector().getAttachments();
		for (ConnectorAttachment attachment : attachments) {
			if (!attachment.getFileName().equalsIgnoreCase(attachmentName)) continue;
			if (!attachment.getParentName().equalsIgnoreCase(articleName)) continue;
			return new WikiAttachmentResource(path, attachment);
		}
		return null;
	}

}
