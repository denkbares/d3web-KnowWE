/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.selenium.main.KnowledgeTestCase;

/**
 * Selenium Test Class for checking the functionality of QuickEdit.
 * 
 * @author Max Diez
 * 
 */
public class QuickEditTest extends KnowledgeTestCase {

	final String CANCEL = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.button_cancel");
	final String ACCEPT = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.button_save");
	final String QEB = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.button_QuickEdit");
	final String kopicID = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.KopicID");
	final String tableID = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.TableID");
	final String attTableID = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.AttributeTableID");
	final String eventID = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.TimeEventID");
	final String divLocator = "//div[@id='" + tableID + "']";

	/**
	 * Testing the quick-edit functionality on a table. Checks multiple changes,
	 * refrehsing etc..
	 */
	public void testQuickTableEditing() {

		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Quick-Edit-Test");
		assertTrue(selenium.getTitle().contains("KnowWE: Quick-Edit-Test"));

		// Saves the page-content before the test starts (for comparison)
		loadAndWait(B_EDIT);
		final String oldPageContent = selenium.getText(EA);
		loadAndWait(B_SAVE);

		// Saves the changes with the old values to restore them
		// Warning! If one cell changed more than once: inconsistency possible
		Map<String, String> oldCellValues = new HashMap<String, String>();

		openQuickEdit(tableID);
		verifyFalse(selenium.isElementPresent(tableID + QEB));
		doSelActionAndWait(tableID + CANCEL, "click");

		// Test: Input -> saving
		oldCellValues.putAll(doQuickEditTableChange("/table/tbody/tr[4]/td[2]/select", "+", true));
		refreshAndWait();
		assertEquals("Input wasn't adopted (in the right way).", "+", selenium.getText(divLocator
				+ "//table/tbody/tr[4]/td[2]"));

		// Test: Input -> not saving
		oldCellValues.putAll(doQuickEditTableChange("//table/tbody/tr[2]/td[4]/select", "+", false));
		refreshAndWait();
		assertEquals("Input was adopted although quickedit being canceled.", "hm",
				selenium.getText(divLocator + "//table/tbody/tr[2]/td[4]"));

		// Test: More changes in one "session" and refresh:
		Map<String, String> input = new HashMap<String, String>();
		input.put("//table/tbody/tr[3]/td[2]/select", "0");
		input.put("//table/tbody/tr[3]/td[3]/select", "0");
		input.put("//table/tbody/tr[3]/td[4]/select", "0");
		oldCellValues.putAll(doQuickEditTableChange(input, true, true));
		for (int i = 2; i <= 4; i++) {
			assertEquals("Changes at Cell 3," + i + " had no effect.", "0",
					selenium.getText(divLocator + "//table/tbody/tr[3]/td[" + i + "]"));
		}

		// Changed values reseted -> page-content should be the same as at the
		// beginning
		for (String key : oldCellValues.keySet()) {
			doQuickEditTableChange(key, oldCellValues.get(key), true);
		}
		refreshAndWait();
		loadAndWait(B_EDIT);
		final String newPageContent = selenium.getText(EA);
		loadAndWait(B_SAVE);
		assertEquals("There were unexpected changes in the content of the page.", oldPageContent,
				newPageContent);
	}

	/**
	 * Testing the quick-edit functionality for knowledge elements like
	 * Questions-section, Solution-section ... . Checks adding new Solution,
	 * Question and Rule and if it's working in the questionsheet and refresh
	 * bug.
	 */
	public void testKnowledgeElementsEditing() {

		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Quick-Edit-Test");
		assertTrue(selenium.getTitle().contains("KnowWE: Quick-Edit-Test"));
		assertTrue("Solutionstates nicht eingebunden.", selenium.isElementPresent(ST_LOC));

		// Copy actual page content
		loadAndWait(B_EDIT);
		String oldPageContent = selenium.getValue(EA);
		loadAndWait(B_SAVE);

		// Some basic functionality test
		refreshAndWait();
		String sectionID = kopicID + "/SetCoveringList-section/SetCoveringList-section_content";
		openQuickEdit(sectionID);
		openQuickEdit("Quick-Edit-Test/RootType/Kopic/Kopic_content/Solutions-section/Solutions-section_content");
		assertFalse("Quick-Edit button still present.", selenium.isElementPresent(sectionID + QEB));
		assertTrue("Save button is not existing.", selenium.isElementPresent(sectionID + ACCEPT));
		assertTrue("Cancel button is not existing.", selenium.isElementPresent(sectionID + CANCEL));
		String actText = selenium.getBodyText();
		refreshAndWait();
		assertEquals("Unsupposed changes in the HTML code by refreshing and an opened quick-edit.",
				actText, selenium.getBodyText());

		// Short check for questionsheet functionality
		// Map<String, Integer[]> map = new HashMap<String, Integer[]>();
		// map.put("Exhaust pipe color", new Integer[] {4});
		// map.put("Fuel", new Integer[] {2});
		// result = checkSolutions(new String[] {"Clogged air filter"}, map,
		// false);
		// assertEquals("Questionsheet not working (no quick-edit failure): " +
		// comment, true, result);
		//
		// Adding new solution
		sectionID = kopicID + "/Solutions-section/Solutions-section_content";
		quickEditAdd(sectionID, "\nMissing wheel");
		assertTrue("//div[@id='" + sectionID + "'] isn't present (yet).",
				waitForElement("//div[@id='" + sectionID + "']"));
		assertTrue("New solution \"Missing wheel\" wasn't saved.", selenium.getText(
				"//div[@id='" + sectionID + "']").contains("Missing wheel"));

		// Adding new question
		sectionID = kopicID + "/Questions-section/Questions-section_content";
		quickEditAdd(sectionID,
				"\n-How many wheels do you have? [oc]\n-- \"< 4\"\n-- 4\n-- \"> 4\"\n");
		refreshAndWait();
		assertTrue(
				"Question 'How many wheels do you have?' isn't present (yet)",
				waitForElement("//div[@id='questionsheet']//span[text()='How many wheels do you have?']"));

		// Adding new rule connecting solution and question
		sectionID = kopicID + "/Rules-section/Rules-section_content";
		openQuickEdit(sectionID);
		quickEditAdd(sectionID,
				"\nIF \"How many wheels do you have?\" = \"< 4\"\nTHEN \"Missing wheel\" = P7");

		// //Again questionsheet test (still working?)
		// map.put("Exhaust pipe color", new Integer[] {4});
		// map.put("Fuel", new Integer[] {2});
		// result = checkSolutions(new String[] {"Clogged air filter"}, map,
		// false);
		// assertEquals(comment, true, result);
		//
		// //Testing if new Solution, Question and Rule are working
		// map.put("How many wheels do you have?", new Integer[] {1});
		// result = checkSolutions(new String[] {"Missing wheel"}, map, false);
		// assertEquals(comment + "(Quick-Edit changes may had no effect)",
		// true,
		// result);

		// restore page
		loadAndWait(B_EDIT);
		doSelActionAndWait(EA, "type", oldPageContent);
		loadAndWait(B_SAVE);
	}

	public void testAttributeTableEditing() {
		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Quick-Edit-Test");
		openQuickEdit(attTableID);
		doSelActionAndWait(attTableID + "/AttributeTableLine/AttributeTableCell2", "type",
				"Newinfo");
		doSelActionAndWait(attTableID + "/AttributeTableLine2/AttributeTableCell3", "type",
				"Beschreibung");
		doSelActionAndWait(attTableID + "/AttributeTableLine2/AttributeTableCell", "type",
				"Leere Batterie");
		doSelActionAndWait(attTableID + ACCEPT, "click");
		assertTrue("//div[@id='" + attTableID + "'] isn't present (yet)",
				waitForElement("//div[@id='" + attTableID + "']"));
		String attTableText = selenium.getText("//div[@id='" + attTableID + "']");
		assertTrue("New attribute wasn't saved", attTableText.contains("Leere Batterie"));
		assertTrue("New attribute wasn't saved", attTableText.contains("Newinfo"));
		assertTrue("New attribute wasn't saved", attTableText.contains("Beschreibung"));
	}

	// public void testHermesTimeEventEditing() {
	// open(rb.getString("KnowWE.SeleniumTest.url") +
	// "Wiki.jsp?page=Quick-Edit-Test");
	// openQuickEdit(eventID);
	// doSelActionAndWait("//div[@id='" + eventID + "']//textarea[@id='" +
	// eventID
	// + "/default-edit-area']", "type",
	// "<<Wiederaufbau Uni Wuerzburg(2)\n478v\n\nRenovierung der UniBib\n>>");
	// doSelActionAndWait(eventID + ACCEPT, "click");
	// assertTrue(waitForElement("//div[@id='" + eventID + "']"));
	// String eventText = selenium.getText("//div[@id='" + eventID + "']");
	// assertTrue("New content wasn't saved",
	// eventText.contains("Wiederaufbau Uni Wuerzburg"));
	// assertTrue("New content wasn't saved",
	// eventText.contains("478 v. Chr."));
	// assertTrue("New content wasn't saved",
	// eventText.contains("Renovierung der UniBib"));
	// }

	public void testQuickCoveringTableEditing() {
		// TODO write test
	}

	/**
	 * Adds some new content to the page (e.g. to Rules-section) per QuickEdit.
	 * 
	 * @param sectionID The ID of the section containing the QuickEdit which
	 *        should be used.
	 * @param newText The new content which is being added after the existing
	 *        one.
	 */
	private void quickEditAdd(String sectionID, String newText) {
		openQuickEdit(sectionID);
		doSelActionAndWait("//div[@id='" + sectionID + "']//textarea[@id='" + sectionID
				+ "/default-edit-area']",
				"type", selenium.getValue("//div[@id='" + sectionID + "']//textarea[@id='"
						+ sectionID + "/default-edit-area']")
						+ newText);
		doSelActionAndWait(sectionID + ACCEPT, "click");
		waitForElement(sectionID + QEB);
	}

	/**
	 * This method simulates the using of quickedit: First the quickedit is
	 * opened, then the select button is changed to a new value and at last it
	 * is clicked on save or cancel.
	 * 
	 * @param tableCellLocator Locator of the select button in row x and column
	 *        y, normally in a form like //table/tbody/tr[x]/td[y]/select
	 * @param newValue is chosen (has to be in the select's drop down list)
	 * @param save whether changes should be saved or not
	 * @return a pair of Strings packed in a map: the tableCellLocator and the
	 *         old value of this cell
	 */
	private Map<String, String> doQuickEditTableChange(final String tableCellLocator,
			final String newValue, final Boolean save) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(tableCellLocator, newValue);
		return doQuickEditTableChange(map, save, false);
	}

	/**
	 * This method simulates the using of quickedit: First the quickedit is
	 * opened, eventually the page is refreshed and then all the select buttons
	 * are changed to their new value and at last it is clicked on save or
	 * cancel.
	 * 
	 * @param tableCellLocator Locator of the select button in row x and column
	 *        y, normally in a form like //table/tbody/tr[x]/td[y]/select
	 * @param newValue is chosen (has to be in the select's drop down list)
	 * @param save whether changes should be saved or not
	 * @param refresh whether the page should be refreshed before setting the
	 *        new values
	 * @return a map of pairs of Strings packed: the tableCellLocator and the
	 *         old value of this cell (for restoring the old values)
	 */
	private Map<String, String> doQuickEditTableChange(final Map<String, String> input,
			final Boolean save, Boolean refresh) {
		Map<String, String> output = new HashMap<String, String>();
		openQuickEdit(tableID);
		if (refresh) {
			refreshAndWait();
		}
		// Selects the new values for the given tablecells (=key)
		for (String key : input.keySet()) {
			assertTrue(divLocator + key + " does not exists",
					waitForElement(divLocator + key));
			output.put(key, selenium.getText(divLocator + key + "/option[@selected='selected']"));
			doSelActionAndWait(divLocator + key, "select", "label=" + input.get(key));
		}
		if (save) {
			doSelActionAndWait(tableID + ACCEPT, "click");
		}
		else {
			doSelActionAndWait(tableID + CANCEL, "click");
			output.clear();
		}
		// Checks if Quick-Edit-Button is visible (save/cancel action finished)
		assertTrue("Accept or cancel button didn't worked", waitForElement(tableID + QEB));
		return output;
	}

	/**
	 * Opens the Quick-Edit-Mode and checks if it was already opened.
	 * 
	 * @param locator The id of the section containing the Quick-Edit-Element
	 *        (e.g.
	 *        Quick-Edit-Test/RootType/Kopic/Kopic_content/Rules-section/Rules
	 *        -section_content)
	 */
	private void openQuickEdit(String locator) {
		// If another session opened Quick-Edit-Mode -> close it
		if (selenium.isElementPresent(locator + CANCEL)) {
			doSelActionAndWait(locator + CANCEL, "click");
		}
		doSelActionAndWait(locator + QEB, "click");
		// Kind of guaranteeing the completion of opening the QuickEdit
		waitForElement(locator + CANCEL);
	}
}
