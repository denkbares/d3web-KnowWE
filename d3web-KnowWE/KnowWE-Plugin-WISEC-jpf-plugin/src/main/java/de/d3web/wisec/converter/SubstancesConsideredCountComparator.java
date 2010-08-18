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

import java.util.Comparator;
import java.util.Map;

/**
 * Sorts according to the specified count of considered substances in a list.
 * 
 * @author joba
 * 
 */
public class SubstancesConsideredCountComparator implements
		Comparator<String> {

	private final Map<String, Integer> substanceListConsideredSubstances;

	public SubstancesConsideredCountComparator(
			Map<String, Integer> substanceListConsideredSubstances) {
		this.substanceListConsideredSubstances = substanceListConsideredSubstances;
	}

	@Override
	public int compare(String listname1, String listname2) {
		int count1 = substanceListConsideredSubstances.get(listname1);
		int count2 = substanceListConsideredSubstances.get(listname2);
		return count2 - count1;
	}

}
