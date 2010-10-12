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

import java.util.ResourceBundle;

import com.thoughtworks.selenium.KnowWESeleneseTestCase;

/**
 * Abstract Class to provide some methods simplifiying the selenium tests for
 * KnowWE.
 * 
 * @author Max Diez
 * 
 */
public abstract class KnowWETestCase extends KnowWESeleneseTestCase {

	/**
	 * The KnowWE-Selenium-Test ResourceBundle, containing specific information
	 * being needed for the tests (e.g. sleepTime, server url, ...)
	 */
	final protected ResourceBundle rb = ResourceBundle.getBundle("KnowWE-Selenium-Test");

	/**
	 * Time duration for a short break, mostly used to stop the Thread.
	 */
	final protected Long sleepTime = Long.parseLong(rb.getString("KnowWE.SeleniumTest.SleepTime"));

	/**
	 * If this time duration is expired, the page load will be canceled and the
	 * test fails with a TimedOut Exception.
	 */
	final private String pageLoadTime = rb.getString("KnowWE.SeleniumTest.PageLoadTime");

	final protected String B_EDIT = rb.getString("KnowWE.SeleniumTest.KnowWETestCase.button_edit");
	final protected String B_SAVE = rb.getString("KnowWE.SeleniumTest.KnowWETestCase.button_save");
	final protected String EA = rb.getString("KnowWE.SeleniumTest.KnowWETestCase.editorarea");
	final protected String ST_LOC = rb.getString("KnowWE.SeleniumTest.KnowWETestCase.st_loc");

	/**
	 * This method specifies on which server the tests are run, with which
	 * browser on which port and the URL of the KnowWE under test. All
	 * parameters can be set in the properties file.
	 */
	@Override
	public void setUp() throws Exception {
		setUp(rb.getString("KnowWE.SeleniumTest.url"),
				rb.getString("KnowWE.SeleniumTest.browser"),
				rb.getString("KnowWE.SeleniumTest.server"),
				Integer.parseInt(rb.getString("KnowWE.SeleniumTest.port")));
	}

	/**
	 * This method opens, in the actual chosen window, a new page and waits for
	 * its loading. Mostly used at the beginning of the test, to start a new
	 * session.
	 * 
	 * @param url Address of the new page
	 */
	protected void open(String url) {
		selenium.open(url);
		selenium.waitForPageToLoad(pageLoadTime);
	}

	/**
	 * Use this method if you want to open a link, which opens a new window
	 * (e.g. because of "target=_blank"). Selenium can't find this new window,
	 * so it's necessary to call this method.
	 * 
	 * @param url URL address of the page which should be opened.
	 * @param name The window's name
	 * @param forwarding Boolean if the given url leads automatically to another
	 *        page
	 */
	private void openWindowBlank(String url, String name, Boolean forwarding) {
		// First opening a new window, and tell Selenium to focus on this one.
		selenium.openWindow(url, name);
		selenium.selectWindow(name);
		selenium.windowFocus();
		// Now Selenium is focused on this new window and the page with the
		// given URL can be opened (with waitForPageToLoad)
		open(url);
		if (forwarding) {
			// Waiting till forwarded page starts loading (actual URL changes)
			Long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < Long.parseLong(pageLoadTime)
					&& selenium.getLocation().equals(url)) {
				threadSleep(sleepTime);
			}
			selenium.getTitle(); // Setting new flag for "AndWait"
			// Refresh works on the forwarded page (different (and unknown)
			// URL than url); guarantees a working waitForPageToLoad
			refreshAndWait();
		}
	}

	/**
	 * This method is used if you want to open a link and wait for the page to
	 * be loaded before continuing with the test. If this link opens a new
	 * window (target=_blank or = kwiki-dialog) the method openWindowBlank is
	 * called. If pageLoadTime expires the test will quit by error.
	 * 
	 * @param linkName This is the string/locator Selenium uses to search for
	 *        the link.
	 */
	protected void loadAndWait(String locator) {
		boolean isNewTab = false;
		try {
			// Try to find an ancestor being a link (<a...) for locator
			String aParent = getAncestor(locator, "a");
			String target = selenium.getAttribute(aParent + "@target").toString();
			if (target.equals("_blank") || target.equals("kwiki-dialog")) {
				isNewTab = true;
				String aHref = selenium.getAttribute(aParent + "@href");
				// opens a new Session with an URL specified by the homepage's
				// URL
				// and the location given by link's attribute href. If a dialog
				// is
				// opened forwarding has to be true.
				openWindowBlank(rb.getString("KnowWE.SeleniumTest.url") + aHref, aHref,
						target.equals("kwiki-dialog"));
			}
		}
		catch (Exception e) {
			// locator has no "a" parent or it has no attribute "target"
			// no need of using openWindowBlank
		}
		if (!isNewTab) {
			// Normal procedure using a link ('in-frame' load)
			doSelActionAndWait(locator, "click");
			selenium.waitForPageToLoad(pageLoadTime);
		}
	}

	/**
	 * This method simulates the user's action clicking on the browser's refresh
	 * and waits until the page finished loading.
	 */
	protected void refreshAndWait() {
		selenium.refresh();
		selenium.waitForPageToLoad(pageLoadTime);
	}

	/**
	 * @see doSelActionAndWait/3
	 */
	protected void doSelActionAndWait(String locator, String selActionName) {
		doSelActionAndWait(locator, selActionName, "");
	}

	/**
	 * Executes a Selenium command on a certain element and stops for a fixed
	 * time. If the element is not present yet, this method waits on its arrival
	 * until "RetryTime" expires.
	 * 
	 * @param locator Locator to find the element
	 * @param selMethodName The name of the Selenium method to be run (only
	 *        limited number of Selenium commands available; they have to be
	 *        added manually to this method). If a command, not being added, is
	 *        used an IllegalSeleniumMethod Exception is thrown.
	 * @param value Some additional parameter if being needed (e.g. for command
	 *        select: optionLocator)
	 */
	protected void doSelActionAndWait(String locator, String selMethodName, String value) {
		// wait until Element appears (if not, try once a refresh)
		if (!waitForElement(locator)) {
			refreshAndWait();
			waitForElement(locator);
		}

		if (selMethodName.equals("click")) selenium.click(locator);
		else if (selMethodName.equals("select")) selenium.select(locator, value);
		else if (selMethodName.equals("type")) selenium.type(locator, value);
		else if (selMethodName.equals("check")) selenium.check(locator);
		else if (selMethodName.equals("uncheck")) selenium.uncheck(locator);
		else {
			try {
				throw new IllegalSeleniumMethod(
						selMethodName
								+ " is not an accepted Selenium method (in case it exists in Selenese you can add it).");
			}
			catch (IllegalSeleniumMethod ism) {
				ism.printStackTrace();
				KnowWETestCase.fail(ism.getMessage());
			}
		}
		// threadSleep(sleepTime);
	}

	/**
	 * This is a little help method if you want to access any element with a
	 * Selenium method on its xpath locator. It's kind of a upgrade of the
	 * standard method isElementPresent: If the element isn't present yet, this
	 * method will retry to find it after a refresh until "RetryTime" expires.
	 * 
	 * @param locator xpath locator of the element
	 * @return True if the element was found in the specified period, else
	 *         false.
	 */
	protected boolean waitForElement(String locator) {
		Long startTime = System.currentTimeMillis();
		boolean elemPresent = selenium.isElementPresent(locator);
		while (!elemPresent
				&& System.currentTimeMillis() - startTime <
				Long.parseLong(rb.getString("KnowWE.SeleniumTest.RetryTime"))) {
			// refreshAndWait();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			elemPresent = selenium.isElementPresent(locator);
		}
		return elemPresent;
	}

	/**
	 * This method sets a little break. Thread is temporary paused.
	 * 
	 * @param sleepTime Break duration (specified in the property file)
	 */
	protected void threadSleep(Long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * This method opens the current wiki page in edit mode, gets the editorarea
	 * content and returns it. This content is used to compare the wiki page
	 * before the editing and after some changes.
	 * 
	 * @created 30.09.2010
	 */
	protected String getWikiPageContent() {
		loadAndWait(B_EDIT);
		String str = selenium.getText(EA);
		loadAndWait(B_SAVE);
		return str;
	}

	/**
	 * This method opens the current wiki page in edit mode, and sets the wiki
	 * page content stored in the Selenium test properties file. Use this method
	 * to ensure the wiki page text is the expected text before running the test
	 * case.
	 * 
	 * @created 30.09.2010
	 */
	protected void resetWikiPageContent(String page) {
		loadAndWait(B_EDIT);
		selenium.type(EA, "");
		selenium.type(EA, page);
		loadAndWait(B_SAVE);
	}

	/**
	 * A little help if you have a locator but want to access an ancestor of a
	 * special type of the given element.
	 * 
	 * @param locator xpath specifying the element (child)
	 * @param type specifies the ancestor's type (i.e. "a" or "div")
	 * @return the ancestor's xpath
	 */
	private String getAncestor(String locator, String type) {
		// You have to get one level higher than the one you expect the
		// ancestor being in, to receive all children with the given type
		String parent = locator + "/../..";
		while (selenium.isElementPresent(parent)) {
			// iterating over all elements at this level with the given type
			// searching for one having the element as a sibling
			for (int i = (Integer) selenium.getXpathCount(parent + "/" + type); i > 0; i--) {
				if (selenium.isElementPresent(parent + "/" + type + "[" + i + "]" + locator)) {
					return parent + "/" + type + "[" + i + "]";
				}
			}
			// get one level higher (parent of the actual element)
			parent += "/..";
		}
		return "Not Found";
	}

	/**
	 * Own Exception for doSelActionAndWait: if it's tried to call a selenium
	 * method which is not specified in doSelActionAndWait, this Exception is
	 * thrown.
	 * 
	 * @author Max Diez
	 * 
	 */
	private class IllegalSeleniumMethod extends Exception {

		private static final long serialVersionUID = 1L;

		private IllegalSeleniumMethod(String errorMessage) {
			super(errorMessage);
		}
	}
}
