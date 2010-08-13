/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.imagequestion;

import java.io.IOException;
import java.util.Collection;

import java.util.logging.Logger;

import de.d3web.core.io.WikiAttachmentResource;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.wikiConnector.ConnectorAttachment;

/**
 * Stores an Attachment as a resource in the knowledgebase.
 * 
 * @author Johannes Dienst
 *
 */
public class ImageToResourceSubtreeHandler extends SubtreeHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
		KnowledgeBaseManagement kbm =
			D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKBM(article.getTitle());
		if (kbm==null) return null;
		
		KnowledgeBase kb = kbm.getKnowledgeBase();
		String imageName = DefaultMarkupType.getAnnotation(s, "Image");
		Collection<ConnectorAttachment> attachments =
			KnowWEEnvironment.getInstance().getWikiConnector().getAttachments();
		for (ConnectorAttachment att : attachments) {
			if (att.getFileName().equalsIgnoreCase(imageName)) {
				String path = "multimedia/" + att.getFileName();
				try {
					WikiAttachmentResource res =
						new WikiAttachmentResource(att.getInputStream(), path);
					kb.addResouce(res);
				} catch (IOException e) {
					Logger.getLogger(ImageToResourceSubtreeHandler.class.getName())
						.warning(
								"Could not store attachment"
								+ att.getFileName()
								+ " to knowledgebase : "
								+ e.getMessage());
				}
			}
		}
		return null;
	}

}
