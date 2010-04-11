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

import com.thoughtworks.selenium.KnowWESeleneseTestCase;

/**
 * Abstract Class to provide some methods simplifiying the selenium tests
 * for KnowWE
 * @author Max Diez
 *
 */
public abstract class KnowWETestCase extends KnowWESeleneseTestCase {
	
	protected String comment = new String();

	ResourceBundle rb = ResourceBundle.getBundle("KnowWE-Selenium-Test");
	
	private Long sleepTime = Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime"));
	
	private String pageLoadTime = rb.getString("KnowWE.SeleniumTest.PageLoadTime");
	
	
	/**
	 * This method specifies on which server the tests are
	 * run and with which browser.
	 * Both can be set in the properties file.
	 */
	public void setUp() throws Exception {
		setUp(rb.getString("KnowWE.SeleniumTest.url"),
				rb.getString("KnowWE.SeleniumTest.browser"),
				rb.getString("KnowWE.SeleniumTest.server"),
				Integer.parseInt(rb.getString("KnowWE.SeleniumTest.port")));
	}
	
	/**
	 * This method is used if you want to open a link and wait for the
	 * page to be loaded before continuing with the test. If this
	 * link opens a new window (target=_blank or = kwiki-dialog) the 
	 * method openWindowBlank is called.
	 * If time (standard value set in properties file) expires the test will
	 * quit by error.
	 * @param linkName This is the string/locator Selenium uses to search for the link.
	 */
	protected void loadAndWait(String locator) {
		boolean isNewTab = false;
		try {
			String aParent = getAncestor(locator, "a");
			String target = selenium.getAttribute(aParent + "@target").toString();
			if (target.equals("_blank") || target.equals("kwiki-dialog")) {
				isNewTab = true;
				String aHref = selenium.getAttribute( aParent + "@href");
				openWindowBlank(rb.getString("KnowWE.SeleniumTest.url") + aHref , aHref, target.equals("kwiki-dialog"));			
			}
		} catch (Exception e) {
			// locator has no "a" parent or it has no attribute "target"
			// no need of using openWindowBlank
		}
		if (!isNewTab) {
			doSelActionAndWait(locator, "click");
			selenium.waitForPageToLoad(pageLoadTime);
		}
	}
	
	/**
	 * Use this method if you want to open a link, which opens 
	 * a new window because of "target=_blank" Selenium can't 
	 * find this new window.
	 * @param url
	 * @param name The window's name
	 * @param forwarding Boolean if the given url leads automatically to
	 * another page
	 */
	private void openWindowBlank (String url, String name, Boolean forwarding) {
		selenium.openWindow(url, name);
		selenium.selectWindow(name);
		selenium.windowFocus();
		System.out.println("Titel vor 1. Refresh: " + selenium.getTitle() + ", " + selenium.getLocation() + " @ " + System.currentTimeMillis());
		open(url);
		System.out.println("Titel nach 1. Refresh: " + selenium.getTitle() + ", " + selenium.getLocation()+ " @ " + System.currentTimeMillis());
		if (forwarding) {
			System.out.println("Titel in if Zweig:" + selenium.getTitle() + ", " + selenium.getLocation()+ " @ " + System.currentTimeMillis());
			//Waiting till forwarded page starts loading
			Long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < Long.parseLong(pageLoadTime)
					&& selenium.getLocation().equals(url)) {
				threadSleep(sleepTime);
			}
			System.out.println("Titel nach warten:" + selenium.getTitle() + ", " + selenium.getLocation()+ " @ " + System.currentTimeMillis());
			selenium.getTitle(); //Setting new flag for "AndWait"
			refreshAndWait();
			System.out.println("Titel nach 2. Refresh:" + selenium.getTitle() + ", " + selenium.getLocation()+ " @ " + System.currentTimeMillis() + "\n");
		}
	}
	
	/**
	 * This method simulates the user's action clicking on the browser's
	 * refresh and waits until the page finished loading.
	 */
	protected void refreshAndWait() {
		selenium.refresh();
		selenium.waitForPageToLoad(pageLoadTime);
	}
	
	protected void doSelActionAndWait(String locator, String selActionName) {
		doSelActionAndWait(locator, selActionName, "");
	}
	
	/**
	 * Executes a Selenium Command on a certain element and stops for
	 * a fixed time. If the element is not present yet, this method waits
	 * on its arrival until "RetryTime" expires.
	 * @param locator Locator to find the element
	 * @param selMethodName The name of the Selenium method to be run
	 * (only limited number of Selenium Commands available; they have to
	 * be added manually to this method)
	 * @param value Some additional parameter if being needed (e.g. select: optionLocator) 
	 */
	protected void doSelActionAndWait(String locator, String selMethodName, String value) {
		Long startTime = System.currentTimeMillis();
		while (!selenium.isElementPresent(locator) 
				&& System.currentTimeMillis() - startTime < 
				Long.parseLong(rb.getString("KnowWE.SeleniumTest.RetryTime"))) {
			//wait until Element appears
			refreshAndWait();
		}
		try {
			if (selMethodName.equals("click")) selenium.click(locator);
			else if (selMethodName.equals("select")) selenium.select(locator, value);
			else if (selMethodName.equals("type")) selenium.type(locator, value);
			else throw new IllegalSelMethodException( selMethodName + " is not an accepted Selenium method (in case it exists in Selenese you can add it");
		} catch(IllegalSelMethodException isae) {
			isae.printStackTrace();
		}
		
		threadSleep(sleepTime);
	}
	
	protected boolean checkSolutions(String[] solutions, Map<String, Integer[]> input, boolean isDialog) {
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
	protected boolean checkAndUncheckSolutions(String[] expSolutions, String[] notExpSolutions,
			Map<String, Integer[]> input, boolean isDialog) {
		if (isDialog) {
			String startPage = selenium.getTitle();
			doSelActionAndWait("sstate-clear", "click");
			loadAndWait("//img[@title='Fall']");
			assertEquals("d3web Dialog", selenium.getTitle());
			
			//Try all observations on the left (Frageboegen)
			for (int i = 1; selenium.isElementPresent("//div[@id='qasettree']//table[" + i + "]//span"); i++) {
				doSelActionAndWait("//div[@id='qasettree']//table[" + i + "]//span", "click");
				refreshAndWait();
				//Try all categories of input
				for (String elem : input.keySet()) {
					if (selenium.getText("//table[@id='qPage']").contains(elem)) {
						boolean elemFound = false;
						//Try all listed categories of the chosen observation
						for (int j = 1; !elemFound && j < 10000; j++) {
							if (selenium.isElementPresent("//table[@id='qPage']//td[@id='qTableCell_Q" + j + "']") &&
									selenium.getText("//table[@id='qPage']//td[@id='qTableCell_Q" + j + "']").contains(elem.trim())){
								//Choose all answers given in the int array of input
								for (int k = 0; k < input.get(elem).length; k++) {
									doSelActionAndWait("//td[@id='qTableCell_Q" + j + "']//input[@id='Q" + j + "a" + input.get(elem)[k] + "']", "click");
									doSelActionAndWait("//td[@id='qTableCell_Q" + j + "']//input[@id='dialogForm:questions:q_ok']", "click");									
								}
								elemFound = true;
								
								//Category should be chosen now. If not, repeat last iteration.
								boolean allSelected = true;
								for (int k = 0; k < input.get(elem).length; k++) {
									allSelected &= selenium.isChecked("//td[@id='qTableCell_Q" + j + "']//input[@id='Q" + j + "a" + input.get(elem)[k] + "']");
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
			selenium.close();
			selenium.selectWindow(null);
			selenium.windowFocus();
			assertEquals(startPage, selenium.getTitle());
		} else {
			selenium.click("sstate-clear");
			Object[] elem = input.keySet().toArray();
			for (int j = 0; j < elem.length; j++) {
				Integer[] actCategoryInput = input.get(elem[j]);
				
				doSelActionAndWait("//span[text()='" + elem[j].toString().trim()+ "']", "click");
				//It's recommended to wait until the dialog pops up
				threadSleep(sleepTime);
				
				//Radio buttons or check box
				String actInputLocator = "";
				int loopRepeatCounter = 0;
				for (int i = 0; i < actCategoryInput.length && loopRepeatCounter < 5; i++) {
					actInputLocator = "//form[@name='semanooc']/input[" + actCategoryInput[i] + "]";
					if (selenium.isElementPresent(actInputLocator)) {
						doSelActionAndWait(actInputLocator, "click");
						
						//Check if input was accepted
						doSelActionAndWait("//span[text()='" + elem[j].toString().trim()+ "']", "click");						
						//It's recommended to wait until the dialog pops up
						threadSleep(sleepTime);
						
						if (!selenium.isElementPresent(actInputLocator) 
								|| !selenium.isChecked(actInputLocator)) {
							i--;
							loopRepeatCounter++;
							continue;
						}
						//Choosing was succesful -> next one
						loopRepeatCounter = 0;
						continue;
					}
					actInputLocator = "//form[@name='semanomc']/input[" + actCategoryInput[i] + "]";
					if(selenium.isElementPresent(actInputLocator)){
						doSelActionAndWait(actInputLocator, "click");
						//CheckBoxes need some special treatment
						threadSleep(sleepTime);
						
						//Check if input was accepted
						doSelActionAndWait("//span[text()='" + elem[j].toString().trim()+ "']", "click");						
						//It's recommended to wait until the dialog pops up
						threadSleep(sleepTime);
						if (!selenium.isElementPresent(actInputLocator)
								|| !selenium.isChecked(actInputLocator)) {
							i--;
							loopRepeatCounter++;
							continue;
						}
						//Choosing was succesful -> next one
						loopRepeatCounter = 0;
						continue;
					}
					
					//If Pop-Up is still opened, the input wasen't correct
					if (selenium.isElementPresent("//div[@id='o-lay-body']")
							&& selenium.getText("//div[@id='o-lay-body']/h3").equals(elem[j].toString())) {
						selenium.click("Your answer number is not existing.");
					}
					
					//No Radiobutten no Checkbox present (pop-up
					//closed already), so repeat this step
					j--;
					refreshAndWait();
					break;
				}
				//Close pop-up window if not done yet
				if (selenium.isElementPresent("o-lay-close")) {
					doSelActionAndWait("o-lay-close", "click");				
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
	 * @param expSolutions This is an array of all the solutions which should
	 * appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @return true if with the input parameters all expected Solutions 
	 * are shown at solutionsstates and all not expected not; else false.
	 */
	private boolean verifySolutions(String[] expSolutions, String[] notExpSolutions, long startTime) {
		String actSolutions = "";
		comment = "";
		doSelActionAndWait("sstate-update", "click");
		assertEquals("No solutions displayed", true, selenium.isElementPresent("//div[@id='sstate-result']"));
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
				comment += "Didn't see solution " + expSolutions[i] + ". ";
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
		doSelActionAndWait("sstate-clear", "click");
		//Retry the check if result is false and time isn't expired
		if (System.currentTimeMillis() - startTime < 
				Long.parseLong(rb.getString("KnowWE.SeleniumTest.RetryTime")) && !result) {
			refreshAndWait();
			return verifySolutions(expSolutions, notExpSolutions, startTime);
		}
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
	protected void open(String url) {
		selenium.open(url);
		selenium.waitForPageToLoad(pageLoadTime);
	}
	
	/**
	 * A little help if you have a locator but want to access an 
	 * ancestor of a special type of the given element.
	 * @param locator specifies the element (child)
	 * @param type specifies the ancestor's type (i.e. "a" or "div")
	 * @return the ancestor's xpath
	 */
	private String getAncestor(String locator, String type) {
		String parent = locator + "/../..";
		while (selenium.isElementPresent(parent)) {
			for (int i = (Integer)selenium.getXpathCount(parent + "/" + type); i > 0; i--) {
				if (selenium.isElementPresent(parent + "/" + type + "[" + i + "]" + locator)) {
					return parent + "/" + type + "[" + i + "]";
				}
			}
			parent += "/..";
		}
		return "Not Found";
	}
	
	private class IllegalSelMethodException extends Exception {

		private static final long serialVersionUID = 1L;

		private IllegalSelMethodException(String errorMessage) {
	        super(errorMessage);
	    }
	}
}

