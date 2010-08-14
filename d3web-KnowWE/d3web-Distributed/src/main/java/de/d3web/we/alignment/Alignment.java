/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.alignment;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;

public abstract class Alignment implements Comparable<Alignment> {

	protected final IdentifiableInstance object;
	protected final AbstractAlignType type;

	private Map<String, Boolean> properties = new HashMap<String, Boolean>();

	public Alignment(IdentifiableInstance object, AbstractAlignType type) {
		super();
		this.object = object;
		this.type = type;
	}

	public abstract IdentifiableInstance getAligned(IdentifiableInstance ii);

	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return getType().getAlignedValue(input, output);
	}

	public IdentifiableInstance getObject() {
		return object;
	}

	public AbstractAlignType getType() {
		return type;
	}

	public int compareTo(Alignment o) {
		// [TODO]:Peter: implement!
		return 0;
	}

	public void setProperty(String key, Boolean value) {
		properties.put(key, value);
	}

	public Boolean getProperty(String key) {
		return properties.get(key);
	}

	public Map<String, Boolean> getPropertiesMap() {
		return properties;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Alignment)) return false;
		if (o == this) return true;
		Alignment alignment = (Alignment) o;
		if (!getType().equals(alignment.getType())) return false;
		return alignment.getObject().equals(object);
	}

	public int hashCode() {
		return type.hashCode()
				+ 37 * object.hashCode();
	}

	public String toString() {
		return object.toString() + " " + getType().toString();
	}

}
