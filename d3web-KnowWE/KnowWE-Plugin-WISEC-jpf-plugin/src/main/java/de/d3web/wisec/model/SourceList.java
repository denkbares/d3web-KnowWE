package de.d3web.wisec.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class stores the information about an upper list.
 * 
 * @author joba
 *
 */
public class SourceList {


	Map<String, String> attributes = new LinkedHashMap<String, String>();
	// private final Collection<SubstanceList> children = new
	// LinkedList<SubstanceList>();
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

	public String getId() {
		return get("ID");
	}

	public Collection<String> getAttributes() {
		return this.attributes.keySet();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SourceList other = (SourceList) obj;
		if (attributes == null) {
			if (other.attributes != null) return false;
		}
		else if (!attributes.equals(other.attributes)) return false;
		return true;
	}

}
