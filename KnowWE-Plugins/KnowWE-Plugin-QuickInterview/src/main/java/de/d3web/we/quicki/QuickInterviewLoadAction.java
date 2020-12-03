/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
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
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * @author Jonas Müller
 * @created 05.02.18
 */
public class QuickInterviewLoadAction extends AbstractAction {
	/**
	 * Loads a .xml file with quickinterview session information from locally stored drive/attachments
	 *
	 * @param context UserActionContext with params
	 */
	@Override
	public void execute(UserActionContext context) throws IOException {
		Collection<SessionRecord> sessionRecords;

		boolean fromFile = Boolean.parseBoolean(context.getParameter("fromFile"));
		if (fromFile) {
			sessionRecords = getSessionRecordsFromFile(context);
		} else {
			sessionRecords = getSessionRecordsFromAttachments(context);
		}
		if (sessionRecords == null || sessionRecords.isEmpty()) return;

		String sectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionId);
		if (!KnowWEUtils.canView(section, context)) return;

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(context, section);
		if (kb == null) return;

		// deletes current Session and creates a new one, gets Blackboard
		Session session;
		try {
			session = SessionConversionFactory.replayToSession(kb, sessionRecords.iterator().next());
		} catch (NullPointerException e) {
			context.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
			context.getResponse().getWriter().write("Facts could not be set. The file seems to not match with this interview.");
			context.getResponse().flushBuffer();
			return;
		}
		if (session == null) return;
		SessionProvider.setSession(context, session);

	}

	private Collection<SessionRecord> getSessionRecordsFromAttachments(UserActionContext context) throws IOException {
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		List<WikiAttachment> attachments = wikiConnector
				.getAttachments(context.getTitle());
		for (WikiAttachment wikiAttachment : attachments) {
			String fileName = wikiAttachment.getFileName();
			if (fileName.startsWith(context.getParameter("loadname"))) {
				SessionPersistenceManager manager = SessionPersistenceManager.getInstance();
				return manager.loadSessions(wikiAttachment.getInputStream());
			}
		}
		return null;
	}

	private Collection<SessionRecord> getSessionRecordsFromFile(UserActionContext context) throws IOException {
		return SessionPersistenceManager.getInstance()
					.loadSessions(new ByteArrayInputStream(context.getParameter("data").getBytes("UTF-8")));
	}
}
