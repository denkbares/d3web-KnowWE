/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testsuite;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestSuite;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;

/**
 * KnowWEAction which is used to run a test suite and to display the result of
 * the run.
 * 
 * This action is called by a JavaScript-Snippet used by the
 * TestSuiteResultType.
 * 
 * @see TestSuite
 * @see TestSuiteResultType
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestSuiteRunAction extends AbstractAction {

	private ResourceBundle rb;
	private final DecimalFormat formatter = new DecimalFormat("0.00");
	private MessageFormat msgFormatter;

	@Override
	public void execute(ActionContext context) throws IOException {
		this.rb = D3webModule.getKwikiBundle_d3web(context.getWikiContext());
		this.msgFormatter = new MessageFormat("");

		String testSuiteName = context.getParameter("testsuite");
		String web = context.getParameter(KnowWEAttributes.WEB);

		TestSuite t = TestSuiteUtils.loadTestSuite(testSuiteName, web);
		if (t == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Test suite was null. Unable to execute it.");
			context.getWriter().write(rb.getString("KnowWE.Testsuite.loaderror"));
		}
		else {
			context.getWriter().write(renderTestSuiteResult(t));
		}

	}

	private String renderTestSuiteResult(TestSuite t) {
		if (t.totalPrecision() == 1.0 && t.totalRecall() == 1.0) {
			return renderTestsuitePassed(t);

		}
		else if (!t.isConsistent()) {
			return renderTestsuiteNotConsistent(t);

		}

		return renderTestsuiteFailed(t);
	}

	private String renderTestsuitePassed(TestSuite t) {
		StringBuilder html = new StringBuilder();

		// TestSuite passed text and green bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/green_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.Testsuite.passed",
				new Object[] { t.getRepository().size() }));
		html.append("</strong>");
		html.append("</p>");

		// TestSuite Result Detais
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(t.totalPrecision());
		html.append("<br />");
		html.append("Recall: ");
		html.append(t.totalRecall());
		html.append("<br /><br />");
		html.append("</p>");

		return html.toString();
	}

	private String renderTestsuiteNotConsistent(TestSuite t) {
		StringBuilder html = new StringBuilder();

		// TestSuite failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.Testsuite.failed",
				new Object[] { t.getRepository().size() }));
		html.append("</strong>");
		html.append("</p>");

		// TestSuite Result Detais
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(formatter.format(t.totalPrecision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(t.totalRecall()));
		html.append("<br /><br />");
		html.append(rb.getString("KnowWE.Testsuite.notconsistent"));
		html.append("</p>\n");

		html.append(renderNotConsistentDetails(t));

		return html.toString();
	}

	private String renderNotConsistentDetails(TestSuite t) {
		StringBuilder html = new StringBuilder();

		// Pointer and Text
		html.append("<p id='testsuite2-show-extend' class='show-extend pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.Testsuite.detail"));
		html.append("</p>");

		// Div containing details
		html.append("<div id='testsuite-detail-panel' class='hidden'>");
		html.append("<p style='margin-left:22px'>");
		html.append(findInconsistentRTC(t));
		html.append("</p>");
		html.append("</div>\n");

		return html.toString();
	}

	private String findInconsistentRTC(TestSuite t) {

		StringBuilder message = new StringBuilder();

		for (SequentialTestCase stc1 : t.getRepository())
			for (SequentialTestCase stc2 : t.getRepository())
				for (int i = 0; i < stc1.getCases().size() && i < stc2.getCases().size(); i++) {
					RatedTestCase rtc1 = stc1.getCases().get(i);
					RatedTestCase rtc2 = stc2.getCases().get(i);

					// when the findings are equal...
					if (rtc1.getFindings().equals(rtc2.getFindings())) {
						// ...but not the solutions...
						if (!rtc1.getExpectedSolutions().equals(
								rtc2.getExpectedSolutions())) {
							// ...the TestSuite is not consistent!
							message.append("Rated-Test-Case ");
							message.append(stc1.getCases().indexOf(rtc1));
							message.append(" in ");
							message.append(stc1.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.Testsuite.and"));
							message.append(" ");
							message.append("Rated-Test-Case ");
							message.append(stc2.getCases().indexOf(rtc2));
							message.append(" in ");
							message.append(stc2.getName());
							message.append(" ");
							message.append(rb.getString("KnowWE.Testsuite.havesamefindings"));
							message.append("<br />");

						}
					}
					else break;
				}

		// Not very nice but prevents double listing of RTCs
		return message.substring(0, message.length() / 2).toString();
	}

	private String renderTestsuiteFailed(TestSuite t) {
		StringBuilder html = new StringBuilder();

		// TestSuite failed text and red bulb
		html.append("<p>");
		html.append("<img src='KnowWEExtension/images/red_bulb.gif' width='16' height='16' /> ");
		html.append("<strong>");
		html.append(loadMessage("KnowWE.Testsuite.failed",
				new Object[] { t.getRepository().size() }));
		html.append("</strong>");
		html.append("</p>");

		// TestSuite Result Detais
		html.append("<p style='margin-left:22px'>");
		html.append("Precision: ");
		html.append(formatter.format(t.totalPrecision()));
		html.append("<br />");
		html.append("Recall: ");
		html.append(formatter.format(t.totalRecall()));
		html.append("</p>\n");

		html.append(renderDifferenceDetails(t));

		return html.toString();
	}

	private String renderDifferenceDetails(TestSuite t) {

		StringBuilder html = new StringBuilder();

		// Pointer and Text

		html.append("<p id='testsuite-failed-extend' onclick='extendTestSuiteFailed()'>");
		html.append("<img id='testsuite-failed-extend-img' src='KnowWEExtension/images/arrow_right.png' ");
		html.append("align='absmiddle' /> ");
		html.append(rb.getString("KnowWE.Testsuite.detail"));
		html.append("</p>");

		html.append("<div style='clear:both'></div>");

		// Table containing details
		html.append("<div id='testsuite-detail-panel' style='display:none'>");
		html.append(renderDetailResultTable(t));
		html.append("</div>\n");

		return html.toString();
	}

	private String renderDetailResultTable(TestSuite t) {

		StringBuilder html = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		temp.append("");

		
		temp.append("</table>");

		// HTML-Code
		for (SequentialTestCase stc : t.getRepository()) {
			temp = new StringBuilder();
			for (RatedTestCase rtc : stc.getCases()) {
				if (!rtc.isCorrect()) {
					temp.append("<tr>");
					temp.append("<th colspan='2' >");
					temp.append("Rated-Test-Case ");
					temp.append(stc.getCases().indexOf(rtc) + 1);
					temp.append("</th>");
					temp.append("</tr>");
					temp.append("<tr>");
					temp.append("<th>");
					temp.append(rb.getString("KnowWE.Testsuite.expected"));
					temp.append("</th>");
					temp.append("<th>");
					temp.append(rb.getString("KnowWE.Testsuite.derived"));
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

	private String loadMessage(String key, Object[] arguments) {
		msgFormatter.applyPattern(rb.getString(key));
		return msgFormatter.format(arguments);
	}

}
