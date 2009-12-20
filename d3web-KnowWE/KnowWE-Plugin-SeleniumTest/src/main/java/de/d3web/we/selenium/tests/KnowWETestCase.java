/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.selenium.tests;

import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.Assert;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * Abstract Class to provide some methods simplifiying the selenium tests
 * for KnowWE
 * @author Max Diez
 *
 */
public abstract class KnowWETestCase extends SeleneseTestCase{
	
	protected String comment = new String();
	
	ResourceBundle rb = ResourceBundle.getBundle("KnowWE-Selenium-Test");
	
	/**
	 * This method specifies on which server the tests are
	 * run and with which browser.
	 * Both can be set in the properties file.
	 */
	public void setUp() throws Exception {
		setUp(rb.getString("KnowWE.SeleniumTest.server"),
				rb.getString("KnowWE.SeleniumTest.browser"));	
	}
	
	/**
	 * This method is used if you want to open a link and wait for the
	 * page to be loaded before continuing with the test.
	 * If time (standard value set in properties file) expires the test will
	 * quit by error.
	 * @param linkName This is the string/locator Selenium uses to search for the link.
	 */
	public void loadAndWait(String locator) {
		selenium.click(locator);
		selenium.waitForPageToLoad(rb.getString("KnowWE.SeleniumTest.PageLoadTime"));
	}
	
	/**
	 * If you open a link, which opens a new window because of
	 * "target=_blank" Selenium can't find this new window. Use this method
	 * instead of selenium.click or loadAndWait
	 * @param url
	 * @param name
	 */
	public void openWindowBlank (String url, String name) {
		selenium.openWindow(url, name);
		selenium.selectWindow(name);
		selenium.waitForPageToLoad(rb.getString("KnowWE.SeleniumTest.PageLoadTime"));
		selenium.windowFocus();
	}
	
	/**
	 * This method simulates the user's action clicking on the browser's
	 * refresh and waits until the page finished loading.
	 */
	private void refreshAndWait() {
		selenium.refresh();
		selenium.waitForPageToLoad(rb.getString("KnowWE.SeleniumTest.PageLoadTime"));		
	}
	
	/**
	 * Clicks on a certain element and stops for a fixed time.
	 * @param string Locator to find the element to click on
	 */
	private void clickAndWait(String string) {
		selenium.click(string);
		try {
			Thread.sleep(Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkSolutions(String[] solutions, Map<String, Integer[]> input, boolean isDialog) {
		return checkAndUncheckSolutions(solutions, new String[] {}, input, isDialog);
	}
	
	/**
	 * This method will help to check the functionality of
	 * questionsheet and dialog of an existing knowledgebase.
	 * @param input This map contains the information about what should
	 * be chosen (no text fields). Key is the name of the category and 
	 * value is an array of the option's numbers (int beginning with 1).  
	 * @param expSolutions This is an array of all the solutions which should
	 * appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @param isDialog If true the dialog is checked else the questionsheet
	 * @return true if with the input parameters all expected Solutions 
	 * are shown at solutionsstates and all not expected not; else false.
	 */
	public boolean checkAndUncheckSolutions(String[] expSolutions, String[] notExpSolutions,
			Map<String, Integer[]> input, boolean isDialog) {
		if (isDialog) {
			clickAndWait("sstate-clear");
			clickAndWait("//img[@title='Fall']");
			open("dialog.jsf");
			assertEquals(selenium.getTitle(), "d3web Dialog");
			
			//Try all observations on the left (Frageboegen)
			for (int i = 1; selenium.isElementPresent("//div[@id='qasettree']//table[" + i + "]//span"); i++) {
				clickAndWait("//div[@id='qasettree']//table[" + i + "]//span");
				refreshAndWait();
				//Try all categories of input
				for (String elem : input.keySet()) {
					if (selenium.getText("//table[@id='qPage']").contains(elem)) {
						boolean elemFound = false;
						//Try all listed categories of the choosen observation
						for (int j = 1; !elemFound && j < 10000; j++) {
							if (selenium.isElementPresent("//table[@id='qPage']//td[@id='qTableCell_Q" + j + "']") &&
									selenium.getText("//table[@id='qPage']//td[@id='qTableCell_Q" + j + "']").contains(elem.trim())){
								//Choose all answers given in the int array of input
								for (int k = 0; k < input.get(elem).length; k++) {
									clickAndWait("//td[@id='qTableCell_Q" + j + "']//input[@id='Q" + j + "a" + input.get(elem)[k] + "']");
									clickAndWait("//td[@id='qTableCell_Q" + j + "']//input[@id='dialogForm:questions:q_ok']");									
								}
								elemFound = true;
							}
						}
					}
				}
			}
			clickAndWait("link=Ergebnisseite");
			open("Wiki.jsp?page=Car-Diagnosis-Test");
			return verifySolutions(expSolutions, notExpSolutions);
		} else {
			Long sleepTime = Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime"));
			
			selenium.click("sstate-clear");
			for (String elem : input.keySet()) {
				Integer[] actCategory = input.get(elem);
				clickAndWait("//span[text()='" + elem.trim()+ "']");
				
				//It's recommended to wait until the dialog pops up
				threadSleep(sleepTime);
				
				//Radio buttons or check box
				for (int i = 0; i < actCategory.length; i++) {
					if (selenium.isElementPresent("//form[@name='semanooc']/input[" + actCategory[i] + "]")) {
						clickAndWait("//form[@name='semanooc']/input[" + actCategory[i] + "]");						
					}
					if(selenium.isElementPresent("//form[@name='semanomc']/input[" + actCategory[i] + "]")){
						selenium.click("//form[@name='semanomc']/input[" + actCategory[i] + "]");
						//CheckBoxes need some special treatment
						threadSleep(sleepTime);
					}
				}
				//Close pop-up window if not done yet
				if (selenium.isElementPresent("o-lay-close")) {
					clickAndWait("o-lay-close");				
				}						
				threadSleep(sleepTime);
			}			
			threadSleep(sleepTime);
			return verifySolutions(expSolutions, notExpSolutions);
		}
	}
		
	/**
	 * After selecting all answers in checkAndUncheckSolution, this method
	 * evaluates if the right solutions are shown in solutionstate
	 * @param expSolutions This is an array of all the solutions which should
	 * appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @return true if with the input parameters all expected Solutions 
	 * are shown at solutionsstates and all not expected not; else false.
	 */
	private boolean verifySolutions(String[] expSolutions, String[] notExpSolutions) {
		String actSolutions = "";
		comment = "";
		clickAndWait("sstate-update");
		if (selenium.isElementPresent("//div[@id='sstate-result']/div/ul/")) {
			actSolutions = selenium.getText("//div[@id='sstate-result']/div/ul/");			
		}
		//If there are suggested AND established solutions
		if (selenium.isElementPresent("//div[@id='sstate-result']/div[2]/ul/")) {
			actSolutions += selenium.getText("//div[@id='sstate-result']/div[2]/ul/");
		}
		boolean result = true;
		for (int i = 0; i < expSolutions.length; i++) {
			boolean hasExpSol = actSolutions.contains(expSolutions[i]);
			if (!hasExpSol) {
				comment += "Didn't saw solution " + expSolutions[i] + ". ";
			}
			result = result && hasExpSol;
		}
		for (int i = 0; i < notExpSolutions.length; i++) {
			boolean hasntNotExpSol = !actSolutions.contains(notExpSolutions[i]);
			if (!hasntNotExpSol) {
				comment += "Saw not expected solution " + notExpSolutions[i] + ". ";
			}
			result = result && hasntNotExpSol;
		}
		clickAndWait("sstate-clear");
		return result;
	}
	
	/**
	 * This method sets a little break. Thread is temporary paused.
	 * @param sleepTime Break duration
	 */
	private void threadSleep(Long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * This method opens in the chosen window a new page and waits for its
	 * loading.
	 * @param url Address of the new page
	 */
	public void open(String url) {
		selenium.open(url);
		selenium.waitForPageToLoad(rb.getString("KnowWE.SeleniumTest.PageLoadTime"));
	}
}
