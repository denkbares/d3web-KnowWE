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

public class CarDiagnosisDialogTest extends KnowWETestCase{
	
	boolean isDialog = true;
	Map<String, Integer[]> map = new HashMap();
	boolean result;
	
		
	public void testCDDialog2(){
		initializeCDDTest();
		map.put("Engine noises", new Integer[] {2});
		result = checkSolutions(new String[] {"Bad ignition timing"}, map, isDialog);
		assertEquals(comment, true, result);
	}
	
	
	public void testCDDialog3(){
		initializeCDDTest();	
		map.put("Engine start", new Integer[] {1});
		map.put("Battery o.k.?", new Integer[] {1});
		result = checkAndUncheckSolutions(new String[] {"Damaged idle speed system"},
				new String[] {"Battery empty"}, map, isDialog);
		assertEquals(comment, true, result);
	}

	public void testCDDialog4(){
		initializeCDDTest();
		map.put("What is the color of the exhaust pipe?", new Integer[] {2});
		map.put("Driving", new Integer[] {1, 2, 3});
		result = checkAndUncheckSolutions(new String[] {"Leaking air intake system"},
				new String[] {"Clogged air filter"}, map, isDialog);
		assertEquals(comment, true, result);
	}
	
	public void testCDDialog1(){
		initializeCDDTest();
		map.put("Battery o.k.?", new Integer[] {2});
		map.put("Ignition timing o.k.?", new Integer[] {2});
		map.put("Air filter o.k.?", new Integer[] {2});	
		map.put("Air intake system o.k.?", new Integer[] {2});
		map.put("Idle speed system o.k.?", new Integer[] {2});
		result = checkSolutions(new String[] {"Empty battery", "Bad ignition timing",
				"Clogged air filter", "Leaking air intake system",
				"Damaged idle speed system"}, map, isDialog);
		assertEquals(comment, true, result);		
	}
		
	public void testCDDialog5(){
		initializeCDDTest();
		map.put("Exhaust fumes", new Integer[] {1});
		map.put("Fuel", new Integer[] {2});
		result = checkSolutions(new String[] {"Clogged air filter"}, map, isDialog);
		assertEquals(comment, true, result);
	}
	
	public void testCDDialog6(){
		initializeCDDTest();
		map.put("Exhaust fumes", new Integer[] {1});
		map.put("Fuel", new Integer[] {2});
		map.put("Battery o.k.?", new Integer[] {2});
		result = checkSolutions(new String[] {"Clogged air filter", "Empty battery"}, map, isDialog);
		assertEquals(comment, true, result);
		
	}
	
	private void initializeCDDTest() {
		open("Wiki.jsp?page=Car-Diagnosis-Test");
		assertEquals("KnowWE: Car-Diagnosis-Test", selenium.getTitle());
		assertTrue("Solutionstates nicht eingebunden",
				selenium.isElementPresent("//div[@id='sstate-panel']/h3"));		
		map.clear();
	}
}
