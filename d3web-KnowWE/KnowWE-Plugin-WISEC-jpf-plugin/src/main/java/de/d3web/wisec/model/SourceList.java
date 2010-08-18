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
