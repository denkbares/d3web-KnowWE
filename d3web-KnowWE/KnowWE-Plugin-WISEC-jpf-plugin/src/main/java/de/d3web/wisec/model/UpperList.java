package de.d3web.wisec.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class stores the information about an upper list.
 * 
 * @author joba
 *
 */
public class UpperList {
	Map<String, String> attributes = new LinkedHashMap<String, String>();
	private final Collection<SubstanceList> children = new LinkedList<SubstanceList>();
	public String filename = "";
	
	public void add(String attribute, String value) {
		this.attributes.put(attribute, value);
	}
	
	public String get(String attribute) {
		String value = this.attributes.get(attribute);
		if (value == null) {
			return "";
		}
		else {
			return value;
		}
	}
	
	public String getName() {
		return get("Name");
	}

	public Collection<String> getAttributes() {
		return this.attributes.keySet();
	}
	
	public void addChild(SubstanceList list) {
		this.children.add(list);
	}
	
	public Collection<SubstanceList> getChildren() {
		return this.children;
	}
	
}
