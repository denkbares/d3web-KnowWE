/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.wisec.converter;

import java.util.HashMap;
import java.util.Map;

import de.d3web.wisec.model.SubstanceList;

public class WISECStatistics {

	// how often was a specified substance used in the lists
	Map<String, Integer> substanceCount = new HashMap<String, Integer>();

	Map<String, String> substanceInFile = new HashMap<String, String>();

	// how many substances were considered for knowledge generation in the
	// specified substance list (id per name)
	public Map<String, Integer> substanceListConsideredSubstances = new HashMap<String, Integer>();
	// how many substances were NOT considered for knowledge generation in the
	// specified substance list (id per name)
	public Map<String, Integer> substanceListNotConsideredSubstances = new HashMap<String, Integer>();
	// Consider only substances for knowledge generation, that occur in the
	// lists more than the specified threshold
	int occurenceThreshold = 0;
	// maps the name of a list to the actual instance of the parsed list
	public HashMap<String, SubstanceList> listName2listInstance = new HashMap<String, SubstanceList>();

	public int totalUseOfSubstances() {
		int totalUse = 0;
		for (String key : substanceCount.keySet()) {
			totalUse += Integer.valueOf(substanceCount.get(key));
		}
		return totalUse;
	}

	@Override
	public String toString() {
		String result = "";
		for (String key : substanceCount.keySet()) {
			result += key + " = " + substanceCount.get(key) + "\n";
		}
		result += "\n\n " + substanceCount.keySet().size() +
				" substances with total use of " + totalUseOfSubstances() + "\n";
		return result;
	}

	public void increment(String name) {
		Integer value = substanceCount.get(name);
		if (value == null) {
			value = new Integer(1);
		}
		else {
			value = value + 1;
		}
		substanceCount.put(name, value);
	}
}
