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
package de.d3web.we.testcase.renderer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.d3web.empiricaltesting.TestCase;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.testcase.TestCaseUtils;
import de.d3web.we.testcase.kdom.TestCaseRunnerType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * Renderer for the TestCaseResultType.
 *
 * @see TestCaseRunnerType
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestCaseRunnerRenderer extends DefaultMarkupRenderer<TestCaseRunnerType> {

	private ResourceBundle rb;
	private MessageFormat formatter;

	public TestCaseRunnerRenderer() {
		super("KnowWEExtension/d3web/icon/testsuite24.png", false);
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<TestCaseRunnerType> section, UserContext user, StringBuilder string) {

		this.rb = D3webModule.getKwikiBundle_d3web(user);
		this.formatter = new MessageFormat("");

		string.append(mask("<strong>" + TestCaseRunnerType.getText(section)
				+ "</strong><br />\n"));

		String testCaseArticleName = TestCaseRunnerType.getTestCase(section);

		if (testCaseArticleName.equals("this")) testCaseArticleName = section.getArticle().getTitle();

		TestCase testSuite = getTestSuiteFor(testCaseArticleName, article.getWeb());
		if (testSuite == null) {
			string.append(mask("<img src='KnowWEExtension/d3web/icon/uses_error16.gif' align='top' /> "));
			string.append(loadMessage("KnowWE.TestCase.notestcasefound",
					new Object[] { testCaseArticleName }));
		}
		else {
			string.append(mask("<div id='testcase-result' >"));
			renderTestCaseDescription(string, testCaseArticleName, testSuite, inDebugMode(section));
			renderTestCaseRun(string, testSuite, inDebugMode(section));
			string.append(mask("</div>\n"));
		}

	}

	private boolean inDebugMode(Section<TestCaseRunnerType> section) {
		String mode = TestCaseRunnerType.getMode(section);
		return mode != null && mode.equals(TestCaseRunnerType.MODE_DEBUG);
	}

	private void renderTestCaseDescription(StringBuilder string, String testCaseTopic, TestCase testSuite, boolean debugMode) {
		string.append(mask("<img src='KnowWEExtension/d3web/icon/comment16.png' align='top' /> "));
		String link = mask("<a href='Wiki.jsp?page=" + testCaseTopic
				+ "'><span id='testcase-topic'>"
				+ testCaseTopic + "</span></a>");
		string.append(loadMessage("KnowWE.TestCase.testcasesfound", new Object[] {
				link, testSuite.getRepository().size() }));
		string.append(mask("<br />\n"));
	}

	private void renderTestCaseRun(StringBuilder string, TestCase testSuite, boolean debug) {
		String runText = "Run";
		if (debug) {
			runText = rb.getString("KnowWE.TestCase.debugbutton");

		}
		else {
			runText = rb.getString("KnowWE.TestCase.runbutton");
		}

		if (debug) {
			string.append(mask("<p onclick='debugTestCase()' id='testcase-run-link'>"));
			string.append(mask("<img src='KnowWEExtension/images/testcase/debug.gif' "));
		}
		else {
			string.append(mask("<p onclick='runTestCase()' id='testcase-run-link'>"));
			string.append(mask("<img src='KnowWEExtension/images/testcase/run.gif' "));
		}
		string.append("alt='" + runText + "' ");
		string.append("title='" + runText + "' ");
		string.append("align='absmiddle' ");
		string.append(mask("/> "));
		string.append(mask("<strong>" + runText + "</strong></p>"));
	}

	private String mask(String string) {
		return KnowWEUtils.maskHTML(string);
	}

	/**
	 * Loads the required test case.
	 *
	 * @created 25/10/2010
	 * @param article the article containing the test case
	 * @param web current web
	 * @return loaded test case or null (in case of errors)
	 */
	private TestCase getTestSuiteFor(String article, String web) {
		return TestCaseUtils.loadTestSuite(article, web);
	}

	private String loadMessage(String key, Object[] arguments) {
		formatter.applyPattern(rb.getString(key));
		return formatter.format(arguments);
	}

}
