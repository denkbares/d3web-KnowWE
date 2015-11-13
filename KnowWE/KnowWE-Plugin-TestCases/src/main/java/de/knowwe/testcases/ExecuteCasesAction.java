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
package de.knowwe.testcases;

import java.io.IOException;
import java.util.Date;

import de.d3web.core.inference.SessionTerminatedException;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.TestCase;
import de.d3web.utils.Log;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

import static de.d3web.testcase.model.TestCase.*;

/**
 * Action that gets called when cases should be executed with the
 * SessionDebugger
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 19.01.2012
 */
public class ExecuteCasesAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String providerId = context.getParameter("providerId");
		String playerId = context.getParameter("playerId");
		String testCaseName = context.getParameter("testCaseName");
		Date endDate;
		try {
			endDate = new Date(Long.parseLong((context.getParameter("date"))));
		}
		catch (NumberFormatException e) {
			throw new IOException(e);
		}
		Section<?> playerSection = Sections.get(playerId);
		Section<?> providerSection = Sections.get(providerId);
		if (providerSection == null || playerSection == null) {
			context.sendError(409, "Section '" + providerId
					+ "' and/or '" + playerId
					+ "' could not be found, possibly because somebody else"
					+ " has edited the page.");
			return;
		}
		boolean ignoreNumValueOutOfRange = TestCasePlayerType.skipNumValueOutOfRange(playerSection);
		TestCaseProviderStorage providerStorage = de.knowwe.testcases.TestCaseUtils.getTestCaseProviderStorage(
				providerSection);
		TestCaseProvider provider = providerStorage.getTestCaseProvider(testCaseName);
		Session session = provider.getActualSession(context);
		SessionDebugStatus status = provider.getDebugStatus(context);
		TestCase testCase = provider.getTestCase();
		Date lastExecuted = status.getLastExecuted();
		if (session != status.getSession() || lastExecuted == null
				|| lastExecuted.after(endDate) || lastExecuted.equals(endDate)) {
			session = SessionFactory.createSession(D3webUtils.getKnowledgeBase(playerSection), testCase.getStartDate());
			provider.storeSession(session, context);
			lastExecuted = null;
		}
		runTo(session, testCase, lastExecuted, endDate, status, ignoreNumValueOutOfRange);
		status.setLastExecuted(endDate);
		D3webUtils.handleLoopDetectionNotification(providerSection.getArticleManager(), context, session);
	}

	private static void runTo(Session session,
							  TestCase testCase, Date startDate, Date endDate,
							  SessionDebugStatus status, boolean ignoreNumValueOutOfRange) {
		try {
			for (Date date : testCase.chronology()) {
				if ((startDate == null || date.after(startDate))
						&& (date.before(endDate) || date.equals(endDate))) {
					testCase.applyFindings(date, session, new Settings(ignoreNumValueOutOfRange));
					for (Check check : testCase.getChecks(date, session.getKnowledgeBase())) {
						status.addCheckResult(date, check, check.check(session));
					}
					status.finished(date);
				}
			}
		}
		catch (SessionTerminatedException e) {
			Log.warning("Propagation terminated due to detected loop.", e);
		}
	}
}
