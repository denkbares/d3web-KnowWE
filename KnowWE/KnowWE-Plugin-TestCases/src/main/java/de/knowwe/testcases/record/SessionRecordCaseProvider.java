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
package de.knowwe.testcases.record;

import java.io.IOException;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.testcase.record.SessionRecordWrapper;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.testcases.AttachmentTestCaseProvider;

/**
 * Wraps an Attachment containing a {@link SessionRecord}
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.01.2012
 */
public class SessionRecordCaseProvider extends AttachmentTestCaseProvider {

	public SessionRecordCaseProvider(Article article, WikiAttachment attachment) {
		super(article, attachment);
	}

	@Override
	public void parse() {
		testCase = null;
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
		if (kb == null) {
			messages.add(Messages.error("Kb not found."));
			return;
		}
		try {
			Collection<SessionRecord> sessionRecords = SessionPersistenceManager.getInstance().loadSessions(
					attachment.getInputStream());
			if (sessionRecords.size() != 1) {
				messages.add(Messages.error("The attached SessionRecord file "
						+ attachment.getFileName()
							+ " has " + sessionRecords.size()
							+ " cases. Only files with exactly one case are allowed."));
				return;
			}
			else {
				testCase = new SessionRecordWrapper(
						sessionRecords.iterator().next());
				updateTestCaseMessages(kb);
			}
		}
		catch (IOException e) {
			messages.add(Messages.error("File " + attachment.getFileName()
					+ " does not contain correct xml markup."));
		}
	}

}
