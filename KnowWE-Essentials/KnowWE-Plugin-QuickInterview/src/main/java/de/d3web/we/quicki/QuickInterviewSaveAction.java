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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * 
 * @author Benedikt Kaemmerer
 * @created 28.11.2012
 */

public class QuickInterviewSaveAction extends AbstractAction {

	/**
	 * Stores a new .xml file with quickinterview session information in the
	 * attachments of he article.
	 *
	 * @param context UserActionContext with params
	 * 
	 */
	@Override
	public void execute(UserActionContext context) throws IOException {

		// get WikiConnector and Session
		WikiConnector wikiConnector = Environment.getInstance()
				.getWikiConnector();

		String sectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionId);
		if (section != null && KnowWEUtils.canView(section, context)) {
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
			if (kb == null) {
				return;
			}
			Session session = SessionProvider.getSession(context, kb);

			// create SessionRecord List
			List<SessionRecord> srList = new ArrayList<SessionRecord>();
			srList.add(SessionConversionFactory.copyToSessionRecord(session));

			// store new Attachment with Session information
			File attachmentFile = new File(context.getParameter("savename")
					+ ".xml");
			attachmentFile.createNewFile();
			SessionPersistenceManager.getInstance().saveSessions(
					attachmentFile, srList);

			/* WikiAttachment attachment = */
			wikiConnector.storeAttachment(
					context.getTitle(), context.getUserName(), attachmentFile);
		}
	}

}
