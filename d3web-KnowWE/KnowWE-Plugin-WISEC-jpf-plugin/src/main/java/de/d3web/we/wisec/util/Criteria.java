/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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
package de.d3web.we.wisec.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Criteria {

	private static final String[] hazardous = { "CMR",
												"Persistence",
												"Bioaccumulation_Potential",
												"Aqua_Tox",
												"EDC",
												"Further_Tox",
												"Climatic_Change"
												};
	
	private static final String[] mobility = {
												"LRT",
												"Water_solubility",
												"Adsorption",
												"Vapor_pressure"
												};

	private static final String[] exposure = {
												"Air",
												"Soil",
												"Sediment",
												"Surface_water",
												"Sea",
												"Groundwater",
												"Drinking_water",
												"Biota"
												};

	private static final String[] regulationRelevance = {
															"Market_Volume",
															"Wide_d_use",
															"Political_concern"
															};

	private static final String[] regulationNeed = { "Need_for_regulation" };

	private static Collection<String> allCriterias = new HashSet<String>();

	// Necessary because UBA wants a special order...
	private static Collection<String> criteriaGroups = new LinkedList<String>();

	private static HashMap<String, String[]> criterias = new HashMap<String, String[]>();

	/*
	 * Put all criterias with their group name in a HashMap and add the
	 * groupnames to a list for sorted output
	 */
	static {
		criterias.put("Need for regulation", regulationNeed);
		criterias.put("Relevance for regulation", regulationRelevance);
		criterias.put("Exposure / Monitoring", exposure);
		criterias.put("Mobility", mobility);
		criterias.put("Hazardous Properties", hazardous);

		criteriaGroups.add("Hazardous Properties");
		criteriaGroups.add("Mobility");
		criteriaGroups.add("Exposure / Monitoring");
		criteriaGroups.add("Relevance for regulation");
		criteriaGroups.add("Need for regulation");
	}

	/*
	 * Put all criterias in a HashSet for easy Access
	 */
	static {
		for (String[] group : criterias.values()) {
			for (String criteria : group) {
				allCriterias.add(criteria);
			}
		}
	}

	/**
	 * Returns all Criterias as Strings e.g. "CMR", "Persistence", etc.
	 * 
	 * @created 15/09/2010
	 * @return
	 */
	public static Collection<String> getAllCriterias() {
		return allCriterias;
	}

	/**
	 * Returns all Criteria groups as Strings e.g. "Mobility",
	 * "Hazardous Properties", etc.
	 * 
	 * @created 15/09/2010
	 * @return
	 */
	public static Collection<String> getCriteriaGroups() {
		return criteriaGroups;
	}

	/**
	 * Returns the Criterias for a given criteria group e.g. "Mobility" ->
	 * "LRT", "Water_solubility", "Adsorption", "Vapor_pressure"
	 * 
	 * @created 15/09/2010
	 * @param groupName
	 * @return
	 */
	public static String[] getCriteriasFor(String groupName) {
		return criterias.get(groupName);
	}

}
