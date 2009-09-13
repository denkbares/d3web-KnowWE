/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.basic;


public class IdentifiableInstance {

	private String namespace;
	private String objectId;
	private Object value;
	
	public IdentifiableInstance(String namespace, String object, Object value) {
		super();
		if(namespace == null) {
			namespace = "";
		}
		if(objectId == null) {
			objectId = "";
		}
		if(value == null) {
			value = "";
		}
		this.namespace = namespace;
		this.objectId = object;
		this.value = value;
	}

	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof IdentifiableInstance)) return false;
		IdentifiableInstance i = (IdentifiableInstance) o;
		if(equalsNamespaces(i)
				&& (value.equals(i.getValue()) || i.getValue().equals(value))) return true;
		return false;
	}
	
	public boolean equalsNamespaces(IdentifiableInstance i) {
		return namespace.equals(i.getNamespace()) 
			&& objectId.equals(i.getObjectId());
	}
	
	public int hashCode() {
		int valueHash = 0;
		if(value instanceof String) {
			valueHash = value.hashCode();
		}
		return getNamespace().hashCode() 
			+ 37 * getObjectId().hashCode() 
			+ 17 * valueHash;
	}
	
	
	public boolean isValued() {
		return value != null && !value.equals("");
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getObjectId() {
		return objectId;
	}

	public Object getValue() {
		return value;
	}
	
	public String toString() {
		return namespace + " :" + objectId + " -> " + value;
	}

}
