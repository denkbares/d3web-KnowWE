package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SubstanceList {

	public String id;
	public String name;
	public List<String> attributes = new ArrayList<String>();
	public List<Substance> substances = new ArrayList<Substance>();
	public Map<String, String> criteria = new LinkedHashMap<String, String>();
	public String filename = "";
	public UpperList upperList;
	
	public SubstanceList(String id) {
		this.id = id;
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
}
