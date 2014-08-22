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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.FactRecord;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
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
 * 
 * @author Benedikt Kaemmerer
 * @created 28.11.2012
 */

public class QuickInterviewLoadAction extends AbstractAction {

	/**
	 * Loads a .xml file with quickinterview session information to restore a
	 * previous session.
	 * 
	 * @param context UserActionContext with params
	 * 
	 */
	@Override
	public void execute(UserActionContext context) throws IOException {

		// get WikiConnector, KnowledgeBase and Session
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();

		String sectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionId);
		if (section != null && KnowWEUtils.canView(section, context)) {

			KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
			if (kb == null) {
				return;
			}

			// deletes current Session and creates a new one, gets Blackboard
			SessionProvider.removeSession(context, kb);
			Session session = SessionProvider.createSession(context, kb);
			Blackboard blackboard = session.getBlackboard();

			// writes information an Blackboard
			try {

				List<WikiAttachment> attachments = wikiConnector
						.getAttachments(context.getTitle());
				for (WikiAttachment wikiAttachment : attachments) {
					String fileName = wikiAttachment.getFileName();

					if (fileName.startsWith(context.getParameter("loadname"))) {
						Collection<SessionRecord> sessionRecords = SessionPersistenceManager.getInstance().loadSessions(
								wikiAttachment.getInputStream());
						Iterator<SessionRecord> iterator = sessionRecords.iterator();
						while (iterator.hasNext()) {
							SessionRecord rec = iterator.next();
							List<FactRecord> valueFacts = rec.getValueFacts();
							for (FactRecord factRecord : valueFacts) {
								Fact fact = FactFactory.createUserEnteredFact(kb,
										factRecord.getObjectName(), factRecord.getValue());
								blackboard.addValueFact(fact);
							}

						}

					}
				}

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
