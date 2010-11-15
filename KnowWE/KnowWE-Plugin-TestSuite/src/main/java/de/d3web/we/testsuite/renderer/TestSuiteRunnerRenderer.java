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
package de.d3web.we.testsuite.renderer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.d3web.empiricaltesting.TestSuite;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.testsuite.TestSuiteUtils;
import de.d3web.we.testsuite.kdom.TestSuiteRunnerType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Renderer for the TestSuiteResultType.
 * 
 * @see TestSuiteRunnerType
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestSuiteRunnerRenderer extends DefaultMarkupRenderer<TestSuiteRunnerType> {

	private ResourceBundle rb;
	private MessageFormat formatter;

	public TestSuiteRunnerRenderer() {
		super("KnowWEExtension/d3web/icon/testsuite24.png", false);
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<TestSuiteRunnerType> section, KnowWEUserContext user, StringBuilder string) {

		this.rb = D3webModule.getKwikiBundle_d3web(user);
		this.formatter = new MessageFormat("");

		string.append(mask("<strong>" + TestSuiteRunnerType.getText(section) + "</strong><br />\n"));

		String testSuiteArticleName = TestSuiteRunnerType.getTestSuite(section);
		TestSuite testSuite = getTestSuiteFor(testSuiteArticleName, article.getWeb());
		if (testSuite == null) {
			string.append(mask("<img src=\"KnowWEExtension/d3web/icon/uses_error16.gif\" align=\"top\" /> "));
			string.append(loadMessage("KnowWE.Testsuite.notestsuitefound",
					new Object[] { testSuiteArticleName }));
		}
		else {
			string.append(mask("<div id=\"testsuite-result\" >"));
			renderTestSuiteDescription(string, testSuiteArticleName, testSuite);
			renderTestSuiteRun(string, testSuite);
			string.append(mask("</div>\n"));
		}
	}

	private void renderTestSuiteDescription(StringBuilder string, String testSuiteTopic, TestSuite testSuite) {
		string.append(mask("<img src=\"KnowWEExtension/d3web/icon/comment16.png\" align=\"top\" /> "));
		String link = mask("<a href=\"Wiki.jsp?page=" + testSuiteTopic
				+ "\"><span id=\"testsuite-topic\">"
				+ testSuiteTopic + "</span></a>");
		string.append(loadMessage("KnowWE.Testsuite.testsuitesfound", new Object[] {
				link, testSuite.getRepository().size() }));
		string.append(mask("<br />\n"));
	}

	private void renderTestSuiteRun(StringBuilder string, TestSuite testSuite) {
		String runText = rb.getString("KnowWE.Testsuite.runbutton");
		string.append(mask("<p onclick=\"runTestSuite()\" id=\"testsuite-run-link\">"));
		string.append(mask("<img "));
		string.append("src=\"KnowWEExtension/d3web/icon/run24.png\" ");
		string.append("alt=\"" + runText + "\" ");
		string.append("title=\"" + runText + "\" ");
		string.append("align=\"absmiddle\" ");
		string.append(mask("/> "));
		string.append(mask("<strong>" + runText + "</strong></p>"));
	}


	private String mask(String string) {
		return KnowWEUtils.maskHTML(string);
	}

	/**
	 * Loads the required test suite.
	 * 
	 * @created 25/10/2010
	 * @param article the article containing the test suite
	 * @param web current web
	 * @return loaded test suite or null (in case of errors)
	 */
	private TestSuite getTestSuiteFor(String article, String web) {
		return TestSuiteUtils.loadTestSuite(article, web);
	}

	private String loadMessage(String key, Object[] arguments) {
		formatter.applyPattern(rb.getString(key));
		return formatter.format(arguments);
	}

}
