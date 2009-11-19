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

import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * Abstract Class to provide some methods simplifiying the selenium tests
 * for KnowWE
 * @author Max Diez
 *
 */
public abstract class KnowWETestCase extends SeleneseTestCase{
	
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
	 * @param linkName This is the string Selenium uses to search for the link.
	 */
	public void loadAndWait(String linkName) {
		selenium.click(linkName);
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
		try {
			Thread.sleep(Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		selenium.selectWindow(name);
		selenium.windowFocus();
	}
	
	public boolean checkSolutions(String[] solutions, Map<String, Integer> input) {
		return checkAndUncheckSolutions(solutions, new String[] {}, input);
	}
	
	/**
	 * This will help you to check your questionsheet.
	 * @param input This map contains the information about what should
	 * be chosen (no text fields). Key is the name of the category and 
	 * value is the option's number (int beginning with 1).  
	 * @param expSolutions This is an array of all the solutions which should
	 * appear after setting the input,
	 * @param notExpSolutions and these should not appear.
	 * @return true if with your input parameters all expected Solutions 
	 * are shown and all not expected not; else false.
	 */
	public boolean checkAndUncheckSolutions(String[] expSolutions, String[] notExpSolutions,
			Map<String,Integer> input) {
		Long sleep = Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime"));
		
		selenium.click("sstate-clear");
		for (String elem : input.keySet()) {
			selenium.click("//span[text()='" + elem.trim()+ "']");
			
			//It's recommended to wait until the dialog pops up
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ie) {
				return false;
			}
			
			//Radio buttons or check box
			if (selenium.isElementPresent("//form[@name='semanooc']/input[" + input.get(elem) + "]")) {
				selenium.click("//form[@name='semanooc']/input[" + input.get(elem) + "]");
			}
			if(selenium.isElementPresent("//form[@name='semanomc']/input[" + input.get(elem) + "]")){
				selenium.click("//form[@name='semanomc']/input[" + input.get(elem) + "]");
				//CheckBoxes need some special treatment
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException ie) {
					return false;
				}
				//Close pop-up window if not done yet
				if (selenium.isElementPresent("o-lay-close")) {
					selenium.click("o-lay-close");				
				}
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ie) {
				return false;
			}
		}			
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException ie) {
			return false;
		}
		
		String actSolutions = "";
		if (selenium.isElementPresent("//div[@id='sstate-result']/div/ul/")) {
			actSolutions = selenium.getText("//div[@id='sstate-result']/div/ul/");			
		}
		//If there are suggested AND established solutions
		if (selenium.isElementPresent("//div[@id='sstate-result']/div[2]/ul/")) {
			actSolutions += selenium.getText("//div[@id='sstate-result']/div[2]/ul/");
		}
		boolean result = true;
		for (int i = 0; i < expSolutions.length; i++) {
			result = result && actSolutions.contains(expSolutions[i]);
		}
		for (int i = 0; i < notExpSolutions.length; i++) {
			result = result && !actSolutions.contains(notExpSolutions[i]);
		}
		return result;
	}

}
