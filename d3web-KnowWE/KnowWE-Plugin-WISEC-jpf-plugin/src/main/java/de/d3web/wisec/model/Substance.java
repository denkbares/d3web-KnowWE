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
import java.util.List;
import java.util.Map;

import de.d3web.wisec.converter.WISECExcelConverter;

public class Substance {

	public Map<String, String> values = new HashMap<String, String>();

	// enumerate all substance lists that contain this instance
	public List<SubstanceList> usesInLists = new ArrayList<SubstanceList>();

	public Substance() {
	}

	public void add(String attribute, String value) {
		this.values.put(attribute, value);
	}

	public String get(String attribute) {
		String val = this.values.get(attribute);
		if (val == null) {
			val = "";
		}
		return val;
	}

	@Override
	public String toString() {
		return this.values.toString();
	}

	public String getCAS() {
		for (String key : values.keySet()) {
			if (key.equalsIgnoreCase(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
				return values.get(key);
			}
		}
		return "NOCAS";
	}

	public String getChemicalName() {
		for (String key : values.keySet()) {
			if (key.equalsIgnoreCase("Chemical_name")) {
				return values.get(key);
			}
		}
		return "NOCHEMNAME";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ getCAS().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Substance other = (Substance) obj;
		return other.getCAS().equals(getCAS());
	}

}
