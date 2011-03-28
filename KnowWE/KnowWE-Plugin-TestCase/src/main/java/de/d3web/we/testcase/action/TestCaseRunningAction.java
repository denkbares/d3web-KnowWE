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

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.we.action.AbstractAction;

/**
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 15.03.2011
 */
public abstract class TestCaseRunningAction extends AbstractAction {

	protected ResourceBundle rb;
	protected final DecimalFormat formatter = new DecimalFormat("0.00");
	protected MessageFormat msgFormatter;

	protected String renderTestCasePassed(TestCase t) {
		StringBuilder html = new StringBuilder();

		// TestCase passed text and green bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/green_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.TestCase.passed",
				new Object[] { t.getRepository().size() }));
		html.append("</strong>");
		html.append("</p>");

		// TestCase TestCaseAnalysisReport Detais
		TestCaseAnalysis analysis = (TestCaseAnalysis) TestCaseAnalysis.getInstance();
		TestCaseAnalysisReport result = analysis.runAndAnalyze(t);
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(result.precision());
		html.append("<br />");
		html.append("Recall: ");
		html.append(result.recall());
		html.append("<br /><br />");
		html.append("</p>");

		return html.toString();
	}

	protected String loadMessage(String key, Object[] arguments) {
		msgFormatter.applyPattern(rb.getString(key));
		return msgFormatter.format(arguments);
	}

	protected String renderTestCaseNotConsistent(TestCase t) {
		StringBuilder html = new StringBuilder();

		// TestCase failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.TestCase.failed",
				new Object[] { t.getRepository().size() }));
		html.append("</strong>");
		html.append("</p>");

		// TestCase TestCaseAnalysisReport Detais
		TestCaseAnalysis analysis = (TestCaseAnalysis) TestCaseAnalysis.getInstance();
		TestCaseAnalysisReport result = analysis.runAndAnalyze(t);
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(formatter.format(result.precision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(result.recall()));
		html.append("<br /><br />");
		html.append(rb.getString("KnowWE.TestCase.notconsistent"));
		html.append("</p>\n");

		html.append(renderNotConsistentDetails(t));

		return html.toString();
	}

	private String renderNotConsistentDetails(TestCase t) {
		StringBuilder html = new StringBuilder();

		// Pointer and Text
		html.append("<p id='testcase2-show-extend' class='show-extend pointer extend-panel-right'>");
		html.append(rb.getString("KnowWE.TestCase.detail"));
		html.append("</p>");

		// Div containing details
		html.append("<div id='testcase-detail-panel' class='hidden'>");
		html.append("<p style='margin-left:22px'>");
		html.append(findInconsistentRTC(t));
		html.append("</p>");
		html.append("</div>\n");

		return html.toString();
	}

	private String findInconsistentRTC(TestCase t) {

		StringBuilder message = new StringBuilder();

		for (SequentialTestCase stc1 : t.getRepository()) {
			for (SequentialTestCase stc2 : t.getRepository()) {
				for (int i = 0; i < stc1.getCases().size() && i < stc2.getCases().size(); i++) {
					RatedTestCase rtc1 = stc1.getCases().get(i);
					RatedTestCase rtc2 = stc2.getCases().get(i);

					// when the findings are equal...
					if (rtc1.getFindings().equals(rtc2.getFindings())) {
						// ...but not the solutions...
						if (!rtc1.getExpectedSolutions().equals(
								rtc2.getExpectedSolutions())) {
							// ...the TestCase is not consistent!
							message.append("Rated-Test-Case ");
							message.append(stc1.getCases().indexOf(rtc1));
							message.append(" in ");
							message.append(stc1.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.TestCase.and"));
							message.append(" ");
							message.append("Rated-Test-Case ");
							message.append(stc2.getCases().indexOf(rtc2));
							message.append(" in ");
							message.append(stc2.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.TestCase.havesamefindings"));
							message.append("<br />");

						}
					}
					else {
						break;
					}
				}
			}
		}

		// Not very nice but prevents double listing of RTCs
		return message.substring(0, message.length() / 2).toString();
	}
}
