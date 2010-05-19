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

	public String getName() {
		for (String key : values.keySet()) {
			if (key.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
				return values.get(key);
			}
		}
		return "NONAME";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ getName().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Substance other = (Substance) obj;
		return other.getName().equals(getName());
	}

	public String getCAS() {
		for (String key : values.keySet()) {
			if (key.equalsIgnoreCase("CAS")) {
				return values.get(key);
			}
		}
		return "NONAME";

	}
	
}
