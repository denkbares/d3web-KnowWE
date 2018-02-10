/*
 * Copyright (C) 2012 denkbares GmbH
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

package de.d3web.we.quicki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * @author Benedikt Kaemmerer
 * @created 28.11.2012
 */

public class QuickInterviewSaveAction extends AbstractAction {

	/**
	 * Stores a new .xml file with quickinterview session information in the
	 * attachments of he article.
	 *
	 * @param context UserActionContext with params
	 */
	@Override
	public void execute(UserActionContext context) throws IOException {

		// get WikiConnector and Session

		String sectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionId);
		if (section != null && KnowWEUtils.canView(section, context)) {
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
			if (kb == null) {
				return;
			}
			Session session = SessionProvider.getSession(context, kb);
			boolean download = Boolean.parseBoolean(context.getParameter("download"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String filename = sdf.format(new Date()) + "-QuickInterview.xml";

			// store new Attachment with Session information
			if (download) {
				saveToFile(context, session, filename);
			}
			else {
				saveToAttachments(context, session, filename, section);
			}
		}
	}

	private void saveToFile(UserActionContext context, Session session, String filename) throws IOException {
		context.setContentType("application/xml");
		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		OutputStream os = context.getOutputStream();
		SessionPersistenceManager.getInstance().saveSessions(os, SessionConversionFactory.copyToSessionRecord(session));
		os.flush();
		os.close();
	}

	private void saveToAttachments(UserActionContext context, Session session, String filename, Section<?> section) throws IOException {
		/* WikiAttachment attachment = */
		if (!KnowWEUtils.canWrite(section, context)) return;
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SessionPersistenceManager.getInstance().saveSessions(outputStream, SessionConversionFactory.copyToSessionRecord(session));
		outputStream.flush();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		wikiConnector.storeAttachment(context.getTitle(), filename, context.getUserName(), inputStream);
		outputStream.close();
		inputStream.close();
	}

}
