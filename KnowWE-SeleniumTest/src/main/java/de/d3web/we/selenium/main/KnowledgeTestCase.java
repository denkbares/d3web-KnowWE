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

package de.d3web.we.selenium.main;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Class containing some special methods being helpful to test
 * knowledge functionality like questionsheet or dialog.
 * 
 * @author Max Diez
 * 
 */
public abstract class KnowledgeTestCase extends KnowWETestCase {

	/* Some string constants mostly for locators; explanations in property file */
	final String OLAY_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.olay_loc");
	final String OLAY_CLOSE = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.olay_close");
	final String RADIO_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.radio_loc");
	final String CBOX_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.cbox_loc");
	final String ST_CLEAR = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.st_clear");
	final String ST_UPDATE = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.st_update");
	final String ST_SOL_EST = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.st_sol_est");
	final String ST_SOL_SUG = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.st_sol_sug");
	final String OBS_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.obs_loc");
	final String QPAGE_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.qpage_loc");
	final String CAT_LOC = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.cat_loc");
	final String CAT_OK = rb.getString("KnowWE.SeleniumTest.Knowledge-Test.cat_ok");
	final int MAXLOOP = Integer.parseInt(rb.getString("KnowWE.SeleniumTest.Knowledge-Test.maxloop"));

	/**
	 * Hash map to store the input needed for the tests.
	 */
	protected Map<String, Integer[]> map = new HashMap<String, Integer[]>();
	/**
	 * Stores some information for a particular test.
	 */
	protected String testResult = new String();
	/**
	 * Specifies whether the test is inspection the dialog or not.
	 */
	protected Boolean isDialog;

	/**
	 * @see checkAndUncheckSolutions
	 */
	protected boolean checkSolutions(String[] solutions, Map<String, Integer[]> input, boolean isDialog) {
		return checkAndUncheckSolutions(solutions, new String[] {}, input, isDialog);
	}

	/**
	 * This method will help to check the functionality of questionsheet and
	 * dialog of an existing knowledgebase.
	 * 
	 * @param input This map contains the information about what should be
	 *        chosen (no text fields). Key is the name of the category and value
	 *        is an array of the option's numbers (int beginning with 1).
	 * @param expSolutions This is an array of all the solutions which should
	 *        appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @param isDialog If true the dialog is checked, otherwise the
	 *        questionsheet
	 * @return true if with the input parameters all expected Solutions are
	 *         shown at solutionsstates and all not expected not; else false.
	 */
	protected boolean checkAndUncheckSolutions(String[] expSolutions, String[] notExpSolutions,
			Map<String, Integer[]> input, boolean isDialog) {
		if (isDialog) {
			String startPage = selenium.getTitle();
			doSelActionAndWait(ST_CLEAR, "click");
			// open dialog
			loadAndWait("//img[@title='Fall']");
			assertEquals("d3web Dialog", selenium.getTitle());

			// Try all observations on the left (Frageboegen)
			for (int i = 1; selenium.isElementPresent(OBS_LOC + i + "]//span"); i++) {
				doSelActionAndWait(OBS_LOC + i + "]//span", "click");
				refreshAndWait();
				// Try all categories of input
				for (String elem : input.keySet()) {
					if (selenium.getText("//table[@id='qPage']").contains(elem)) {
						boolean elemFound = false;
						// Try all listed categories of the chosen observation
						for (int j = 1; !elemFound && j < 10000; j++) {
							if (selenium.isElementPresent(QPAGE_LOC + CAT_LOC + j + "']")
									&&
									selenium.getText(QPAGE_LOC + CAT_LOC + j + "']").contains(
											elem.trim())) {
								// Choose all answers given in the int array of
								// input
								for (int k = 0; k < input.get(elem).length; k++) {
									doSelActionAndWait(QPAGE_LOC + CAT_LOC + j + "']//input[@id='Q"
											+ j + "a" + input.get(elem)[k] + "']", "click");
									doSelActionAndWait(QPAGE_LOC + CAT_LOC + j + "']" + CAT_OK,
											"click");
								}
								elemFound = true;

								// Category should be chosen now. If not, repeat
								// last iteration.
								boolean allSelected = true;
								for (int k = 0; k < input.get(elem).length; k++) {
									allSelected &= selenium.isChecked(CAT_LOC + j
											+ "']//input[@id='Q" + j + "a" + input.get(elem)[k]
											+ "']");
								}
								if (!allSelected) {
									elemFound = false;
									j--;
								}
							}
						}
					}
				}
			}
			doSelActionAndWait("link=Ergebnisseite", "click");
			// return to window with the startPage
			selenium.close();
			selenium.selectWindow(null);
			selenium.windowFocus();
			assertEquals(startPage, selenium.getTitle());
		}
		else {
			doSelActionAndWait(ST_CLEAR, "click");
			Object[] elem = input.keySet().toArray();
			for (int j = 0; j < elem.length; j++) {
				Integer[] actCategoryInput = input.get(elem[j]);

				// Open the category's interview; it's recommended to wait until
				// the frame pops up
				doSelActionAndWait("//span[text()='" + elem[j].toString().trim() + "']", "click");
				waitForElement(OLAY_LOC);

				// Locator for radio button or check box
				String actInputLocator = "";
				// Counts the tries for setting the input for the actual
				// category
				// stop if this counter is >= MAXLOOP
				int loopRepeatCounter = 0;
				// Iterating over all options of the actual category which
				// should be chosen
				for (int i = 0; i < actCategoryInput.length && loopRepeatCounter < MAXLOOP; i++) {
					// -> Radio button part
					actInputLocator = RADIO_LOC + actCategoryInput[i] + "]";
					if (selenium.isElementPresent(actInputLocator)) {
						doSelActionAndWait(actInputLocator, "click");

						// Check if input was accepted
						doSelActionAndWait("//span[text()='" + elem[j].toString().trim() + "']",
								"click");
						// It's recommended to wait until the dialog pops up
						waitForElement(OLAY_LOC);

						if (!selenium.isElementPresent(actInputLocator)
								|| !selenium.isChecked(actInputLocator)) {
							// Something went wrong, so repeat this iteration
							// step
							i--;
							loopRepeatCounter++;
							continue;
						}
						// Choosing was succesful -> next one
						loopRepeatCounter = 0;
						continue;
					}
					// -> Check boxes part(semanomc: ...multiple choice)
					actInputLocator = CBOX_LOC + actCategoryInput[i] + "]";
					if (selenium.isElementPresent(actInputLocator)) {
						doSelActionAndWait(actInputLocator, "click");
						// CheckBoxes need some special treatment
						threadSleep(sleepTime);

						// Check if input was accepted
						doSelActionAndWait("//span[text()='" + elem[j].toString().trim() + "']",
								"click");
						// It's recommended to wait until the interview pops up
						waitForElement(OLAY_LOC);
						if (!selenium.isElementPresent(actInputLocator)
								|| !selenium.isChecked(actInputLocator)) {
							// Something went wrong so repeat this iteration
							// step
							i--;
							loopRepeatCounter++;
							continue;
						}
						// Choosing was successful -> next one
						loopRepeatCounter = 0;
						continue;
					}

					// If interview is still opened, the input wasn't correct ->
					// cancel test
					if (selenium.isElementPresent(OLAY_LOC + "/h3")
							&& selenium.getText(OLAY_LOC + "/h3").equals(elem[j].toString())) {
						selenium.click("Your answer number " + actCategoryInput[i]
								+ " from category " + elem[j].toString() + " is not existing.");
					}

					// No radio button no check box present (pop-up
					// closed already), so repeat this step
					j--;
					refreshAndWait();
					break;
				}
				// Close interview window if not done yet
				if (selenium.isElementPresent(OLAY_CLOSE)) {
					doSelActionAndWait(OLAY_CLOSE, "click");
				}
				threadSleep(sleepTime);
			}
			threadSleep(sleepTime);
		}
		return verifySolutions(expSolutions, notExpSolutions, System.currentTimeMillis());
	}

	/**
	 * After selecting all answers in checkAndUncheckSolution, this method
	 * evaluates if the right solutions are shown in solutionstate
	 * 
	 * @param expSolutions This is an array of all the solutions which should
	 *        appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @return true if with the input parameters all expected Solutions are
	 *         shown at solutionsstates and all not expected not; else false.
	 */
	private boolean verifySolutions(String[] expSolutions, String[] notExpSolutions, long startTime) {
		String actSolutions = "";
		testResult = "";
		doSelActionAndWait(ST_UPDATE, "click");
		assertEquals("No solutions displayed", true,
				selenium.isElementPresent("//div[@id='solutionPanelResults']"));
		if (selenium.isElementPresent(ST_SOL_EST)) {
			actSolutions = selenium.getText(ST_SOL_EST);
		}
		// If there are suggested AND established solutions
		if (selenium.isElementPresent(ST_SOL_SUG)) {
			actSolutions += selenium.getText(ST_SOL_SUG);
		}
		boolean result = true;
		for (int i = 0; i < expSolutions.length; i++) {
			boolean hasExpSol = actSolutions.contains(expSolutions[i]);
			if (!hasExpSol) {
				testResult += "Didn't see solution " + expSolutions[i] + ". ";
			}
			result = result && hasExpSol;
		}
		for (int i = 0; i < notExpSolutions.length; i++) {
			boolean hasntNotExpSol = !actSolutions.contains(notExpSolutions[i]);
			if (!hasntNotExpSol) {
				testResult += "Saw not expected solution " + notExpSolutions[i] + ". ";
			}
			result = result && hasntNotExpSol;
		}
		// Retry the check if result is false and time isn't expired
		if (System.currentTimeMillis() - startTime <
				Long.parseLong(rb.getString("KnowWE.SeleniumTest.RetryTime")) && !result) {
			refreshAndWait();
			return verifySolutions(expSolutions, notExpSolutions, startTime);
		}
		doSelActionAndWait(ST_CLEAR, "click");
		return result;
	}

	protected void initKnowledgeTest() {
		open("Wiki.jsp?page=CD-compiled-KB");
		assertEquals("KnowWE: CD-compiled-KB", selenium.getTitle());
		assertTrue("SolutionPanel nicht eingebunden",
				selenium.isElementPresent(ST_LOC));
		map.clear();
	}
}
