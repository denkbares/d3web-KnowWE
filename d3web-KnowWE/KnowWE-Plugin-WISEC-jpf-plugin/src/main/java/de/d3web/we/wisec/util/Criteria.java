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

public class Criteria {

	private static final String[] hazardous = { "CMR",
												"Persistence",
												"Bioakumulation_Potential",
												"Aqua_Tox",
												"EDC",
												"Further_Tox",
												"Climatic_Change"
												};
	
	private static final String[] mobility = {
												"LRT",
												"Water_solubility",
												"Adsorption",
												"Vapour_pressure"
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

	public static HashMap<String, String[]> CRITERIAS = new HashMap<String, String[]>();

	/*
	 * Put all criterias with their group name in a HashMap
	 */
	static {
		CRITERIAS.put("Need for regulation", regulationNeed);
		CRITERIAS.put("Relevance for regulation", regulationRelevance);
		CRITERIAS.put("Exposure / Monitoring", exposure);
		CRITERIAS.put("Hazardous Properties", hazardous);
		CRITERIAS.put("Mobility", mobility);
	}

	/*
	 * Put all criterias in a HashSet for easy Access
	 */
	static {
		for (String[] group : CRITERIAS.values()) {
			for (String criteria : group) {
				allCriterias.add(criteria);
			}
		}
	}

	public static Collection<String> getAllCriterias() {
		return allCriterias;
	}

}
