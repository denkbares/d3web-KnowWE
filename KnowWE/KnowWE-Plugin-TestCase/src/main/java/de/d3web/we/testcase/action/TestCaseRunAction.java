/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.RTCDiff;
import de.d3web.empiricaltesting.caseAnalysis.functions.Diff;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.testcase.TestCaseUtils;
import de.d3web.we.testcase.kdom.TestCaseRunnerType;

/**
 * KnowWEAction which is used to run a test case and to display the result of
 * the run.
 * 
 * This action is called by a JavaScript-Snippet used by the TestCaseResultType.
 * 
 * @see TestCase
 * @see TestCaseRunnerType
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestCaseRunAction extends TestCaseRunningAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String testCaseName = context.getParameter("testcase");
		String web = context.getParameter(KnowWEAttributes.WEB);

		TestCase t = TestCaseUtils.loadTestSuite(testCaseName, web);

		renderTests(context, t);

	}

	public static void renderTests(UserActionContext context, TestCase t) throws IOException {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(context);
		MessageFormat mf = new MessageFormat("");

		if (t == null) {
			Logger.getLogger(TestCaseRunAction.class.getName()).warning(
					"Test case was null. Unable to execute it.");
			context.getWriter().write(rb.getString("KnowWE.TestCase.loaderror"));
		}
		else {
			context.getWriter().write(renderTestCaseResult(t, rb, mf));
		}
	}

	public static String renderTestCaseResult(TestCase t, ResourceBundle rb, MessageFormat mf) {
		TestCaseAnalysis analysis = new TestCaseAnalysis();
		TestCaseAnalysisReport result = analysis.runAndAnalyze(t);
		return renderTestAnalysisResult(t, result, rb, mf);

	}

	/**
	 * 
	 * @created 16.09.2011
	 * @param t
	 * @param result
	 * @param rb
	 * @param mf
	 * @return
	 */
	public static String renderTestAnalysisResult(TestCase t, TestCaseAnalysisReport result, ResourceBundle rb, MessageFormat mf) {
		if (result.precision() == 1.0 && result.recall() == 1.0) {
			return renderTestCasePassed(t, result, rb, mf);

		}
		else if (!t.isConsistent()) {
			return renderTestCaseNotConsistent(t, result, rb, mf);

		}
		else {
			return renderTestCaseFailed(t, result, rb, mf);
		}
	}

	private static String renderTestCaseFailed(TestCase t, TestCaseAnalysisReport result, ResourceBundle rb, MessageFormat mf) {
		StringBuilder html = new StringBuilder();

		// TestCase failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.TestCase.failed",
				new Object[] { t.getRepository().size() }, rb, mf));
		html.append("</strong>");
		html.append("</p>");

		// TestCase TestCaseAnalysisReport Detais
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(formatter.format(result.precision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(result.recall()));
		html.append("</p>\n");

		html.append(renderDifferenceDetails(t, result, rb));

		return html.toString();
	}

	private static String renderDifferenceDetails(TestCase t, TestCaseAnalysisReport result, ResourceBundle rb) {

		StringBuilder html = new StringBuilder();

		// Pointer and Text

		html.append("<p id='testcase-failed-extend' onclick='extendTestCaseFailed()'>");
		html.append("<img id='testcase-failed-extend-img' src='KnowWEExtension/images/arrow_right.png' ");
		html.append("align='absmiddle' /> ");
		html.append(rb.getString("KnowWE.TestCase.detail"));
		html.append("</p>");

		html.append("<div style='clear:both'></div>");

		// Table containing details
		html.append("<div id='testcase-detail-panel' style='display:none'>");
		html.append(renderDetailResultTable(t, result, rb));
		html.append("</div>\n");

		return html.toString();
	}

	private static String renderDetailResultTable(TestCase t, TestCaseAnalysisReport result, ResourceBundle rb) {

		StringBuilder html = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		temp.append("</table>");

		// HTML-Code
		for (SequentialTestCase stc : t.getRepository()) {
			Diff stcDiff = result.getDiffFor(stc);
			temp = new StringBuilder();
			for (RatedTestCase rtc : stc.getCases()) {
				if (stcDiff.hasDiff(rtc)) {
					temp.append("<tr>");
					temp.append("<th colspan='2' >");
					temp.append("Rated-Test-Case ");
					temp.append(stc.getCases().indexOf(rtc) + 1);
					temp.append("</th>");
					temp.append("</tr>");

					if (rtc.getExpectedSolutions().size() > 0
							|| rtc.getDerivedSolutions().size() > 0) {

						temp.append("<tr>");
						temp.append("<th>");
						temp.append(rb.getString("KnowWE.TestCase.expected"));
						temp.append("</th>");
						temp.append("<th>");
						temp.append(rb.getString("KnowWE.TestCase.derived"));
						temp.append("</th>");
						temp.append("</tr>");
						temp.append("<tr>");
						temp.append("<td>");
						temp.append("<ul>");
						Collections.sort(rtc.getExpectedSolutions(),
								new RatedSolution.RatingComparatorByName());
						for (RatedSolution rs : rtc.getExpectedSolutions()) {
							temp.append("<li>");
							temp.append(rs.toString());
							temp.append("</li>");
						}
						temp.append("</ul>");
						temp.append("</td>");
						temp.append("<td>");
						temp.append("<ul>");
						Collections.sort(rtc.getDerivedSolutions(),
								new RatedSolution.RatingComparatorByName());
						for (RatedSolution rs : rtc.getDerivedSolutions()) {
							temp.append("<li>");
							temp.append(rs.toString());
							temp.append("</li>");
						}
						temp.append("</ul>");
						temp.append("</td>");
						temp.append("</tr>");
					}

					temp.append("<tr>");
					temp.append("<th>");
					temp.append(rb.getString("KnowWE.TestCase.expectedFindings"));
					temp.append("</th>");
					temp.append("<th>");
					temp.append(rb.getString("KnowWE.TestCase.derivedFindings"));
					temp.append("</th>");
					temp.append("</tr>");

					// expected findings
					temp.append("<tr>");
					temp.append("<td>");
					temp.append("<ul>");
					RTCDiff diff = stcDiff.getDiff(rtc);
					for (TerminologyObject obj : diff.getDiffObjects()) {
						temp.append("<li>");
						temp.append(obj.getName() + " = " + diff.getDiffFor(obj).expected);
						temp.append("</li>");
					}
					temp.append("</ul>");
					temp.append("</td>");

					temp.append("<td>");
					temp.append("<ul>");
					for (TerminologyObject obj : diff.getDiffObjects()) {
						temp.append("<li>");
						temp.append(diff.getDiffFor(obj).derived);
						temp.append("</li>");
					}
					temp.append("</ul>");
					temp.append("</td>");
					temp.append("</tr>");
				}
			}

			if (temp.length() > 0) {
				temp.insert(0, "</tr>");
				temp.insert(0, "</th>");
				temp.insert(0, stc.getName());
				temp.insert(0, "Sequential-Test-Case ");
				temp.insert(0, "<th colspan='2'>");
				temp.insert(0, "<tr>");
				temp.insert(0, "<table class='wikitable' border='1'>");
				temp.append("</table>");
				html.append(temp);
			}
		}

		return html.toString();
	}

}
