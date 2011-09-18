/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.we.testcase.action;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.functions.Diff;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.SessionBroker;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.testcase.TestCaseUtils;
import de.d3web.we.utils.KnowWEUtils;

/**
 * The action to debug a suite of test cases, i.e., the run stops when a test
 * case fails and shows this session as the current case session of KnowWE.
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 15.03.2011
 */
public class TestCaseDebugAction extends TestCaseRunningAction {

	private class TestCaseBreakpoint {

		SequentialTestCase testCase;
		RatedTestCase ratedTestCase;
		int numberOfRatedTestCase = -1;

		public boolean isEmpty() {
			return (testCase == null || ratedTestCase == null || numberOfRatedTestCase < 0);
		}
	}

	// millisecs between two rate test cases
	public static final long DEFAULT_RTC_DURATION = 15000;

	@Override
	public void execute(UserActionContext context) throws IOException {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(context);
		MessageFormat msgFormatter = new MessageFormat("");

		String testCaseName = context.getParameter("testcase");
		String web = context.getParameter(KnowWEAttributes.WEB);

		TestCase t = TestCaseUtils.loadTestSuite(testCaseName, web);
		Writer writer = context.getWriter();
		if (t == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Test case was null. Unable to execute it.");
			writer.write(rb.getString("KnowWE.TestCase.loaderror"));
		}
		else {
			TestCaseAnalysis analysis = new TestCaseAnalysis();
			TestCaseAnalysisReport result = analysis.runAndAnalyze(t);
			if (result.precision() == 1.0 && result.recall() == 1.0) {
				writer.write(renderTestCasePassed(t, result, rb, msgFormatter));

			}
			else if (!t.isConsistent()) {
				writer.write(renderTestCaseNotConsistent(t, result, rb, msgFormatter));

			}
			else {
				TestCaseBreakpoint breakpoint = findFirstFailedTestCase(t, result);
				if (breakpoint.isEmpty()) {
					writer.write("Internal error: no failed test case found.");
					return;
				}

				writeFailedTestHeader(writer, breakpoint);

				pushTestCase2KnowWESession(context, breakpoint, t.getKb());

			}
		}
	}

	private void pushTestCase2KnowWESession(UserActionContext context, TestCaseBreakpoint breakpoint, KnowledgeBase knowledgeBase) {
		// clear the current running session
		SessionBroker broker = D3webModule.getBroker(context.getParameters());
		broker.clear();

		// Create a new session
		String kbid = knowledgeBase.getId();
		Session session = broker.getServiceSession(kbid);

		// Run to the breakpoint
		int currentPosition = 0;
		long timestamp = 0;
		for (RatedTestCase rtc : breakpoint.testCase.getCases()) {
			timestamp = setValues(session, rtc, timestamp);
			if (currentPosition >= breakpoint.numberOfRatedTestCase) {
				break;
			}
		}
	}

	private long setValues(Session session, RatedTestCase rtc, long lastTimestamp) {
		Date rtcDate = rtc.getTimeStamp();
		long time = 0;
		if (rtcDate == null) {
			if (lastTimestamp == 0) {
				time = new Date().getTime();
			}
			else {
				time = lastTimestamp + DEFAULT_RTC_DURATION;
			}
		}
		else {
			time = rtcDate.getTime();
		}

		session.getPropagationManager().openPropagation(time);
		for (Finding finding : rtc.getFindings()) {
			session.getBlackboard().addValueFact(
					FactFactory.createUserEnteredFact(finding.getQuestion(), finding.getValue()));
		}
		session.getPropagationManager().commitPropagation();
		return time;
	}

	private void writeFailedTestHeader(Writer writer, TestCaseBreakpoint breakpoint) throws IOException {
		// writer.write(mask("<img src='KnowWEExtension/images/testcase/debug.gif' align='absmiddle' />"));
		writer.write(mask(" First test case failed: "));
		writer.write(mask(breakpoint.testCase.getName() + " ("
				+ breakpoint.numberOfRatedTestCase + ")"));
	}

	private TestCaseBreakpoint findFirstFailedTestCase(TestCase t, TestCaseAnalysisReport result) {
		TestCaseBreakpoint breakpoint = new TestCaseBreakpoint();
		for (SequentialTestCase testCase : t.getRepository()) {
			if (result.hasDiff(testCase)) {
				int noOfRatedTestCase = 0;
				Diff stcDiff = result.getDiffFor(testCase);
				for (RatedTestCase ratedTestCase : testCase.getCases()) {
					if (stcDiff.hasDiff(ratedTestCase)) {
						breakpoint.testCase = testCase;
						breakpoint.ratedTestCase = ratedTestCase;
						breakpoint.numberOfRatedTestCase = noOfRatedTestCase;
						return breakpoint;
					}
					noOfRatedTestCase++;
				}
			}
		}
		return breakpoint;
	}

	private String mask(String string) {
		return KnowWEUtils.maskHTML(string);
	}
}
