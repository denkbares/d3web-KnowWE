package com.thoughtworks.selenium;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestBase;

/**
 * Class extending SeleneseTestBase to support setting a RC host
 * 
 * @author Alex Legler
 */
public class KnowWESeleneseTestBase extends SeleneseTestBase {

	public KnowWESeleneseTestBase() {
		super();
	}

	/**
	 * Sets up the SeleneseTestBase, allowing to set a RC server address and
	 * port.
	 * 
	 * @param url The starting URL
	 * @param browserString The browser to use
	 * @param server Selenium Grid/RC server address
	 * @param port Selenium Grid/RC server port
	 */
	public void setUp(String url, String browserString, String server, int port) {
		if (url == null) {
			url = "http://localhost:" + port;
		}
		selenium = new DefaultSelenium(server, port, browserString, url);
		selenium.start();
	}
}
