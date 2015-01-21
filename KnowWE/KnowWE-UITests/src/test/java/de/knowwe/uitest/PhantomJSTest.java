/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.uitest;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by Veronika Sehne (denkbares GmbH) on 07.01.15.
 */
public class PhantomJSTest {
	@Test
	public void testSearchReturnsResults() {
		//Create instance of PhantomJS driver
		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		capabilities.setCapability("phantomjs.binary.path", "/Users/veronikasehne/Downloads/phantomjs-1.9.8-macosx/bin/phantomjs");
		PhantomJSDriver driver = new PhantomJSDriver(capabilities);
		//Navigate to the page
//		driver.get("http://www.appneta.com/");
//		driver.manage().window().setSize(new Dimension(1124, 850));
		driver.get("http://www.d3web.de/Wiki.jsp?page=Body-Mass-Index");
		driver.manage().window().setSize( new Dimension( 1124, 850 ) );

		//Input height
		driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/div[3]/div[1]/div[1]"));


//		String height = "2";
//		driver.findElementByXPath("/html/body/div[1]/div[2]/div[1]/div[1]/div[6]/div[1]/div[2]/div[1]/div[1]/table/tbody/tr/td[2]/input").sendKeys(height);
//		driver.findElement(By.id("input_quicki2")).click();
//
//		driver.findElement(By.id("input_quicki2")).sendKeys(height);

//		//Input weight
//		driver.findElementById("input_quicki3").click();
//		String weight = "100";
//		driver.findElement(By.id("input_quicki3")).sendKeys(weight);
//
//		driver.findElementById("pagecontent").click();
//
//		assertEquals("25", driver.findElementById("input_quicki4").getText());
//		driver.findElement(By.linkText("Normal weight"));
//
//		//Reset
//		driver.findElement(By.cssSelector("div.reset.pointer")).click();

		//Click the Blog link
//		driver.findElement(By.linkText("Blog")).click();
//
//		//Input the search term into the search box
//		String searchTerm = "Testing";
//		driver.findElement(By.id("s")).sendKeys(searchTerm);
//
//		//Click the Search button
//		driver.findElement(By.cssSelector("input[value='Search']")).click();
//
//		//Find the results
//		List<WebElement> results = driver.findElements(By.cssSelector(".post"));
//
//		//Verify that at least one post is found
//		assertEquals(0, results.size());
//
//		//Navigate to the first post result
//		results.get(0).findElement(By.cssSelector("a[rel='bookmark']")).click();
//
//		//Verify that the search term is contained within the post
//		assertTrue(driver.getPageSource().toLowerCase().contains(searchTerm.toLowerCase()));
	}


//
//	private Selenium selenium;
//
//	@Before
//	public void setUp() throws Exception {
//		selenium = new DefaultSelenium("localhost", 4444, "*googlechrome", "http://www.d3web.de/");
//		selenium.start();
//	}
//
//	@Test
//	public void testBmi() throws Exception {
//		selenium.open("/Wiki.jsp?page=Body-Mass-Index");
//		selenium.click("css=div.reset.pointer");
//		selenium.click("//div[@id='group_quicki1']/div[2]");
//		selenium.type("id=input_quicki2", "2");
//		selenium.type("id=input_quicki3", "100");
//		selenium.click("id=input_quicki4");
//		assertEquals("25", selenium.getValue("id=input_quicki4"));
//		assertTrue(selenium.isElementPresent("link=Normal weight"));
//		selenium.click("css=div.reset.pointer");
//		assertEquals("", selenium.getText("id=input_quicki4"));
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		selenium.stop();
//	}
}
