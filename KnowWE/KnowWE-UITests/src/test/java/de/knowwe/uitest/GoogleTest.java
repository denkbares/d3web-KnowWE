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
 * Created by Veronika Sehne (denkbares GmbH) on 12.01.15.
 */
public class GoogleTest {

	@Test
	public void testSearchReturnsResults() {
		//Create instance of PhantomJS driver
		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		capabilities.setCapability("phantomjs.binary.path", "/Users/veronikasehne/Downloads/phantomjs-1.9.8-macosx/bin/phantomjs");
		PhantomJSDriver driver = new PhantomJSDriver(capabilities);
		//Navigate to the page
		driver.get("https://www.google.de/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8");
		driver.manage().window().setSize(new Dimension(1124, 850));

		//Input
//		driver.findElement(By.id("gbqfq")).click();
//		String search = "d3web";
//		driver.findElement(By.id("gbqfq")).sendKeys(search);


		driver.findElement(By.cssSelector("div input.gbqfif")).click();
		String search = "d3web";
		driver.findElement(By.cssSelector("div input.gbqfif")).sendKeys(search);

		driver.findElement(By.cssSelector("h3.r a")).click();
	}
}
