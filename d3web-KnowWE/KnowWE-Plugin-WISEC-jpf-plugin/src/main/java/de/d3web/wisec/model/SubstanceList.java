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
package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubstanceList {

	public final static String[] CRITERIA_NAMES = new String[] {
			"CMR", "Persistence", "Bioakumulation_Potential", "Aqua_Tox", "EDC", "Further_Tox",
			"Climatic_Change", "LRT", "Water_solubility", "Adsorption", "Vapour_pressure", "Air",
			"Soil", "Sediment", "Surface_water", "Sea", "Groundwater", "Drinking_water", "Biota",
			"Market_Volume", "Wide_d_use", "Political_concern", "Need_for_regulation" };

	private final String id;
	// public String name;

	public List<String> substanceAttributes = new ArrayList<String>();

	public Map<String, String> info = new HashMap<String, String>();

	public List<Substance> substances = new ArrayList<Substance>();
	public Map<String, String> criteria = new LinkedHashMap<String, String>();

	// public String filename = "";
	// public SourceList upperList;

	public SubstanceList(String id) {
		this.id = id;
	}

	public String getId() {
		if (this.id != null) {
			return this.id;
		}
		else {
			return "NO_ID";
		}
	}

	public String getName() {
		String name = this.info.get("Name");
		if (name != null) {
			return name;
		}
		else {
			return "NO_NAME";
		}
	}

	public void add(Substance substance) {
		this.substances.add(substance);
	}

	public void addCriteria(String criteriaName, String criteriaValue) {
		criteria.put(criteriaName, criteriaValue);
	}

	public boolean hasSubstanceWithName(String name) {
		for (Substance s : substances) {
			if (s.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubstanceList other = (SubstanceList) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (getName() == null) {
			if (other.getName() != null) return false;
		}
		else if (!getName().equals(other.getName())) return false;
		return true;
	}

	public boolean contains(String substanceName) {
		for (Substance substance : this.substances) {
			if (substance.getName().equalsIgnoreCase(substanceName)) {
				return true;
			}
		}
		return false;
	}
}
