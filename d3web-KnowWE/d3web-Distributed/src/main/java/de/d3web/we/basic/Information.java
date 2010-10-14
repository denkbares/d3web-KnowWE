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

package de.d3web.we.basic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Information implements Comparable<Information> {

	private final String namespace;
	private final String objectID;
	private final List values;
	private final TerminologyType termType;
	private final InformationType infoType;
	private Date creationDate;

	public Information(String namespaceID, String objectID, List values, TerminologyType termType, InformationType infoType) {
		super();
		creationDate = new Date();
		if (namespaceID == null) {
			namespaceID = "";
		}
		if (objectID == null) {
			objectID = "";
		}
		if (values == null) {
			values = new ArrayList();
		}
		this.namespace = namespaceID;
		this.objectID = objectID;
		this.values = values;
		this.termType = termType;
		this.infoType = infoType;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getObjectID() {
		return objectID;
	}

	public List getValues() {
		return values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Information)) return false;
		Information i = (Information) o;
		if (equalsNamespaces(i)
				&& equalsTypes(i)
				&& values.equals(i.getValues())) return true;
		return false;
	}

	public boolean equalsNamespaces(Information i) {
		return namespace.equals(i.getNamespace())
				&& objectID.equals(i.getObjectID());
	}

	public boolean equalsTypes(Information i) {
		return termType.equals(i.getTerminologyType())
				&& infoType.equals(i.getInformationType());
	}

	@Override
	public int hashCode() {
		return getNamespace().hashCode()
				+ 37 * getObjectID().hashCode()
				+ 17 * getValues().hashCode();
	}

	@Override
	public String toString() {
		return namespace + ":" + objectID + "->" + values + " (" + termType + ", " + infoType + ")";
	}

	public TerminologyType getTerminologyType() {
		return termType;
	}

	public InformationType getInformationType() {
		return infoType;
	}

	@Override
	public int compareTo(Information o) {
		return o.getCreationDate().compareTo(getCreationDate());
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}
