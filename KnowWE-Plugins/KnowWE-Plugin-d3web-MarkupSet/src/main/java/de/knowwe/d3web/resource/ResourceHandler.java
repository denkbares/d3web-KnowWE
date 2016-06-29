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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.utils.Log;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Adds a file defined by resource markup to the d3web knowledge base.
 *
 * @author volker_belli
 * @created 13.10.2010
 */
public class ResourceHandler implements D3webHandler<ResourceType> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<ResourceType> section) {
		KnowledgeBase kb = getKnowledgeBase(compiler);
		if (kb == null) return null;

		String content = DefaultMarkupType.getContent(section);

		String[] paths = DefaultMarkupType.getAnnotations(section, ResourceType.ANNOTATION_PATH);
		String[] sources = DefaultMarkupType.getAnnotations(section, ResourceType.ANNOTATION_SRC);
		String sourcePath = null;
		String destinationPath = null;
		Collection<Message> msgs = new ArrayList<>();
		if (paths.length > 0) {
			destinationPath = paths[0];
			if (paths.length > 1) {
				msgs.add(Messages.warning("More than one path found. Only the first path will be used."));
			}
		}
		if (sources.length > 0) {
			sourcePath = sources[0];
			if (sources.length > 1) {
				msgs.add(Messages.warning("More than one source found. Only the first source will be used."));
			}
		}
		msgs.addAll(addResource(section.getArticle(), kb, content, sourcePath, destinationPath));
		return msgs;
	}

	/**
	 * Adds a resource into the knowledge base resource storage.
	 *
	 * @param article         the article of the defining section
	 * @param kb              the knowledge base to store the resource into
	 * @param content         the content to be stored (or null if an attachment should
	 *                        be used)
	 * @param sourcePath      the wiki attachment path of the file to be stored
	 * @param destinationPath the resource path to store the content in
	 * @return the errors occurred or null if successful
	 * @created 11.06.2011
	 */
	private static Collection<Message> addResource(Article article, KnowledgeBase kb, String content, String sourcePath, String destinationPath) {
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
						"Missing pathname to store the content into."));
			}
			// we use the content for a text file
			// the content must not be empty
			// (this might be a unwanted restriction for the user
			// but it is for its own security to get notice of missing content)
			if (!hasContent) {
				return Messages.asList(Messages.syntaxError(
						"Missing attachment or content block to be stored."));
			}
			resource = new ByteArrayResource(destinationPath, content.getBytes());
		}
		else {
			// is we have no destination path, we use the attachments path
			if (destinationPath == null) {
				destinationPath = article.getTitle() + "/" + sourcePath;
			}
			else if (destinationPath.trim().isEmpty()) {
				return Messages.asList(Messages.syntaxError("Empty destination path."));
			}
			try {
				resource = getAttachmentResource(destinationPath, article.getTitle(), sourcePath);
				if (resource == null) {
					return Messages.asList(Messages.syntaxError("No attachment " + sourcePath
							+ " found."));
				}
			}
			catch (IOException e) {
				Log.severe("wiki error accessing attachments: ", e);
				return Messages.asList(
						Messages.error("Wiki error accessing attachments: " + e.getMessage()));
			}
		}

		// and add the resource to the knowledge base
		Resource existingResource = kb.getResource(resource.getPathName());
		if (existingResource == null) {
			kb.addResouce(resource);
		}
		else {
			return Messages.noMessage();
		}

		// if we have both content and source, warn this
		// (but adding was still successful!)
		if (hasContent && hasSourcePath) {
			return Messages.asList(Messages.warning("Both src and content is specified, the content has been ignored."));
		}
		else {
			return Messages.noMessage();
		}
	}

	/**
	 * Searches the wiki attachments for the specified attachment, denoted by
	 * "attachmentName" and "articleName". If such an attachment is found, a
	 * resource of this attachment will be constructed and returned. If no such
	 * attachment is found, null is returned. Both, "attachmentName" and
	 * "articleName" are treated case-insensitive.
	 *
	 * @param path                  the pathname for the resource to be constructed
	 * @param attachmentArticleName the article name of the attachment to be
	 *                              searched for
	 * @param attachmentFilename    the filename of the attachment to be searched
	 *                              for
	 * @return the constructed resource
	 * @created 12.06.2011
	 */
	private static Resource getAttachmentResource(String path, String attachmentArticleName, String attachmentFilename) throws IOException {
		WikiAttachment attachment = KnowWEUtils.getAttachment(attachmentArticleName,
				attachmentFilename);
		if (attachment != null) {
			return new WikiAttachmentResource(path, attachment.getPath());
		}
		return null;
	}

}
