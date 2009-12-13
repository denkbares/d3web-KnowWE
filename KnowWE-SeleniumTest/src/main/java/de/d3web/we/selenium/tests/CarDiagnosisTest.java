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

import java.util.HashMap;
import java.util.Map;


public class CarDiagnosisTest extends KnowWETestCase{
	
	@SuppressWarnings("unchecked")
	public void testQuestionsheetAndSolutions(){
		selenium.open("Wiki.jsp?page=Car-Diagnosis-Test");
		assertEquals("KnowWE: Car-Diagnosis-Test", selenium.getTitle());
		assertTrue("Solutionstates nicht eingebunden",
				selenium.isElementPresent("//div[@id='sstate-panel']/h3"));
		
		Map<String, Integer> map = new HashMap();
		map.put("Battery o.k.?", 2);
		map.put("Ignition timing o.k.?", 2);
		map.put("Air filter o.k.?", 2);	
		map.put("Air intake system o.k.?", 2);
		map.put("Idle speed system o.k.?", 2);
		verifyTrue(checkSolutions(new String[] {"Empty battery", "Bad ignition timing",
				"Clogged air filter", "Leaking air intake system",
				"Damaged idle speed system"}, map));
		
		map.clear();
		map.put("Engine noises", 1);
		verifyTrue(checkSolutions(new String[] {"Bad ignition timing"}, map));
		
		map.clear();
		map.put("Engine start", 1);
		map.put("Battery o.k.?", 1);
		verifyTrue(checkAndUncheckSolutions(new String[] {"Damaged idle speed system"},
				new String[] {"Battery empty"}, map));
		
		map.clear();
		map.put("Exhaust pipe color", 2);
		map.put("Driving", 1);
		map.put("Driving ", 2);
		map.put("Driving  ", 3);
		verifyTrue(checkAndUncheckSolutions(new String[] {"Leaking air intake system"},
				new String[] {"Clogged air filter"}, map));
		
		map.clear();
		map.put("Exhaust fumes", 1);
		map.put("Fuel", 2);
		verifyTrue(checkSolutions(new String[] {"Clogged air filter"}, map));
		
		map.clear();
		map.put("Exhaust fumes", 1);
		map.put("Fuel", 2);
		map.put("Battery o.k.?", 2);
		verifyTrue(checkSolutions(new String[] {"Clogged air filter", "Empty battery"}, map));	
	}
}
