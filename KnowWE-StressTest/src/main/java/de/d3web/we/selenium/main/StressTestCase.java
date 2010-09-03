/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.selenium.main;

import java.util.ResourceBundle;

/**
 * Class for StressTests on KnowWE with Selenium.
 * 
 * @author Max Diez
 * @created 21.07.2010
 */
public class StressTestCase extends KnowWETestCase {

	final protected ResourceBundle bundle = ResourceBundle.getBundle("KnowWE-Stress-Test");

	@Override
	public void setUp() throws Exception {
		setUp(bundle.getString("KnowWE.SeleniumStressTest.url"),
				bundle.getString("KnowWE.SeleniumStressTest.browser")// ,
		// bundle.getString("KnowWE.SeleniumStressTest.server"),
		// Integer.parseInt(bundle.getString("KnowWE.SeleniumStressTest.port"))
		);
	}
}
