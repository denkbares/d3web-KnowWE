/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.selenium.tests;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.d3web.we.selenium.main.KnowledgeTestCase;

/**
 * Selenium test for the Renaming tool of KnowWE.
 * 
 * @author Stefan Mark
 */
public class RenamingToolTest extends KnowledgeTestCase {

	final String PREVIEW = "//input[@name='submit']";
	final String REPLACE = "//input[@id='renaming-replace']";
	final String CHECK_ALL = "//table[@id='sortable1']/thead[2]/tr/td[3]/input[1]";
	final String UNCHECK_ALL = "//table[@id='sortable1']/thead[2]/tr/td[3]/input[2]";

	final String SEARCH_INPUT = "//input[@id='renameInputField']";
	final String REPLACE_INPUT = "//input[@id='replaceInputField']";

	final String RENAME_EXTEND = "//p[@id='rename-show-extend']";
	final String RENAME_RESULT = "//div[@id='rename-result']";

	final String PREVIOUS_CONTEXT = "//input[@id='renamePreviousInputContext']";
	final String AFTER_CONTEXT = "//input[@id='renameAfterInputContext']";
	final String SEARCH_SENSITIVE = "//input[@id='search-sensitive']";

	final String RESULT_INFO = "//div[@id='rename-result']/p";

	/**
	 * Very simple check of the renaming tool. First checks if the typical
	 * renaming HTML elements are present. If so, some text is entered in the
	 * search and replace box. All occurrences of the search word are replaced.
	 * The search and replace term are swapped and the resulting text compared
	 * to the text before starting the test. If the same everything is working
	 * fine.
	 */
	public void testSimple() {
		checkCorrectPageAndRenamingPresent();

		final String oldPageContent = getWikiPageContent();

		doSelActionAndWait(SEARCH_INPUT, "type", "Lorem");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(PREVIEW, "click");
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		String newPageContent = getWikiPageContent();
		assertNotEquals(oldPageContent, newPageContent);

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "Lorem");
		doSelActionAndWait(PREVIEW, "click");
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		newPageContent = getWikiPageContent();
		assertEquals("There were unexpected changes in the content of the page.",
				oldPageContent, newPageContent);
	}

	/**
	 * Tests the renaming of not all available elements only users choice are
	 * replaced. Only the findings in the Page "Renaming-Tool-Test" should be
	 * replaced.
	 * 
	 * @created 30.09.2010
	 */
	public void testSimpleCertain() {
		checkCorrectPageAndRenamingPresent();

		final String oldPageContent = getWikiPageContent();

		doSelActionAndWait(SEARCH_INPUT, "type", "Lorem");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found after preview action.", 7, findings);

		doSelActionAndWait("//input[@rel='{section: \"Renaming-Tool-Test\"}']", "click");
		doReplace();

		String newPageContent = getWikiPageContent();
		assertNotEquals(oldPageContent, newPageContent);

		doSelActionAndWait(SEARCH_INPUT, "type", "Lorem");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Replace action replaced also findings in other sections and pages.",
				1, findings);

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "Lorem");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found while restoring page content.",
				6, findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		newPageContent = getWikiPageContent();
		assertEquals("There were unexpected changes in the content of the page.",
				oldPageContent, newPageContent);
	}

	/**
	 * Test the renaming of the findings in certain context. If the context is
	 * wrong no renaming should happen.
	 * 
	 * @created 30.09.2010
	 */
	public void testSimpleContextPrevious() {
		checkCorrectPageAndRenamingPresent();

		final String oldPageContent = getWikiPageContent();

		doSelActionAndWait(SEARCH_INPUT, "type", "Lorem");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(PREVIOUS_CONTEXT, "type", "est");
		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found after the preview action.", 2, findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		String newPageContent = getWikiPageContent();
		assertNotEquals(oldPageContent, newPageContent);

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "Lorem");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found while restoring page content.", 2,
				findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		newPageContent = getWikiPageContent();
		assertEquals("There were unexpected changes in the content of the page.",
				oldPageContent, newPageContent);
	}

	/**
	 * Test the renaming of the findings in certain context. If the context is
	 * wrong no renaming should happen.
	 * 
	 * @created 30.09.2010
	 */
	public void testSimpleContextAfter() {
		checkCorrectPageAndRenamingPresent();

		final String oldPageContent = getWikiPageContent();

		doSelActionAndWait(SEARCH_INPUT, "type", "est");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(AFTER_CONTEXT, "type", "Lorem");
		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found after preview action.", 2, findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		String newPageContent = getWikiPageContent();
		assertNotEquals(oldPageContent, newPageContent);

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "est");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found while restoring page content.", 2,
				findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		newPageContent = getWikiPageContent();
		assertEquals("There were unexpected changes in the content of the page.",
				oldPageContent, newPageContent);
	}

	/**
	 * Test the renaming of the findings in case sensitive mode.
	 * 
	 * @created 30.09.2010
	 */
	public void testSimpleCase() {
		checkCorrectPageAndRenamingPresent();

		final String oldPageContent = getWikiPageContent();

		doSelActionAndWait(SEARCH_INPUT, "type", "Sed");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(SEARCH_SENSITIVE, "check");
		assertEquals("Case-Sensitive search not checked", true,
				selenium.isChecked(SEARCH_SENSITIVE));
		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found after preview action.", 2, findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		String newPageContent = getWikiPageContent();
		assertNotEquals(oldPageContent, newPageContent);

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "Sed");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found while restoring page content.", 2,
				findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		newPageContent = getWikiPageContent();
		assertEquals("There were unexpected changes in the content of the page.",
				oldPageContent, newPageContent);
	}

	/**
	 * Tests the renaming of knowledge elements like RuleSections etc.
	 * 
	 * @created 30.09.2010
	 */
	public void testKnowledgeElementsRenaming() {

		checkCorrectPageAndRenamingPresent();

		String oldHTML = extractHTML(selenium.getHtmlSource(), "pagecontent");

		doSelActionAndWait(SEARCH_INPUT, "type", "charm");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found after preview action.", 2, findings);

		doSelActionAndWait("//input[@rel='{section: \"Renaming-Tool-Test\"}']", "click");
		doReplace();

		doSelActionAndWait(SEARCH_INPUT, "type", "AAAAAAAAAAAA");
		doSelActionAndWait(REPLACE_INPUT, "type", "charm");
		doSelActionAndWait(PREVIEW, "click");

		findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals("Unexpected number of findings found while restoring page content.", 2,
				findings);
		doSelActionAndWait(CHECK_ALL, "click");
		doReplace();

		String newHTML = extractHTML(selenium.getHtmlSource(), "pagecontent");
		assertEquals("Unsupposed changes in the HTML after renaming knowledge elements",
				oldHTML, newHTML);
	}

	/**
	 * Checks if the Selection of certain section, which should be taken into
	 * account while searching for findings, works properly.
	 * 
	 * @created 30.09.2010
	 */
	public void testKnowledgeElementSelectionRenaming() {
		checkCorrectPageAndRenamingPresent();

		doSelActionAndWait(SEARCH_INPUT, "type", "charm");
		doSelActionAndWait(REPLACE_INPUT, "type", "AAAAAAAAAAAA");

		// open up the first hierarchy of the search tree:
		doSelActionAndWait("//td[@id='ygtvt1']", "click");
		// open up the second hierarchy of the search tree:
		doSelActionAndWait("//td[@id='ygtvt2']", "click");
		// deselect ALL elements:
		doSelActionAndWait("//span[text()='alle Bereiche']", "click");
		// select only 'SetCoveringList-section':
		doSelActionAndWait("//span[text()='SetCoveringList-section']", "click");

		doSelActionAndWait(PREVIEW, "click");

		int findings = countElements(selenium.getHtmlSource(), "span[id^=p]");
		assertEquals(
				"Unexpected number of findings found after preview action. Selection of to search in section is working wrong.",
				2, findings);
	}
	/**
	 * Checks if the Renaming Page is correctly opened and if the renaming tool
	 * is inserted in the page with all its buttons etc. before the check, the
	 * content of the renaming tool test page is set to the page data stored in
	 * the properties file to ensure the page contains the elements that should
	 * be present.
	 * 
	 * @created 30.09.2010
	 */
	private void checkCorrectPageAndRenamingPresent() {

		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp");
		assertTrue(selenium.getTitle().contains("Main"));
		resetWikiPageContent(rb.getString("KnowWE.SeleniumTest.Main"));

		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Renaming-Tool-Test");
		assertTrue(selenium.getTitle().contains("KnowWE: Renaming-Tool-Test"));
		resetWikiPageContent(rb.getString("KnowWE.SeleniumTest.Renaming"));

		verifyTrue(selenium.isElementPresent(PREVIEW));
		verifyTrue(selenium.isElementPresent(SEARCH_INPUT));
		verifyTrue(selenium.isElementPresent(REPLACE_INPUT));
		verifyTrue(selenium.isElementPresent(RENAME_EXTEND));
		verifyTrue(selenium.isElementPresent(RENAME_RESULT));
		verifyTrue(selenium.isElementPresent(SEARCH_INPUT));
		verifyTrue(selenium.isElementPresent(REPLACE_INPUT));
		verifyTrue(selenium.isElementPresent(PREVIOUS_CONTEXT));
		verifyTrue(selenium.isElementPresent(AFTER_CONTEXT));
		verifyTrue(selenium.isElementPresent(SEARCH_SENSITIVE));
	}

	/**
	 * This methods takes the page as HTML and extracts a certain HTML element.
	 * This element can then further used for verifying executed tasks e.g. Uses
	 * the JSoup library.
	 * 
	 * @created 30.09.2010
	 * @param page The page HTML source
	 * @param id The id of the to extract element
	 * @see Jsoup
	 * @return String the found HTML
	 */
	private String extractHTML(String page, String id) {

		Document doc = Jsoup.parse(page);
		Element e = doc.getElementById(id);
		String html = e.html();
		return html;
	}

	/**
	 * After the preview button is pressed, the request is send via AJAX to the
	 * server. Due to different processing times and sending times, this methods
	 * ensure, that the result has properly come back from the server.
	 * 
	 * @created 30.09.2010
	 */
	private void doReplace() {
		waitForElement(REPLACE);
		doSelActionAndWait(REPLACE, "click");

		waitForElement(RESULT_INFO);
		refreshAndWait();
	}

	/**
	 * Searches within the DOM for the elements specified through the
	 * <code>locator</code> string. The number of found elements is returned.
	 * 
	 * @created 30.09.2010
	 * @param page The content of the page as HTML.
	 * @param locator The DOM element identifier.
	 * @return Integer The number of found elements.
	 */
	private Integer countElements(String page, String locator) {
		Document doc = Jsoup.parse(page);

		Elements elements = doc.select(locator);
		return elements.size();
	}
}

