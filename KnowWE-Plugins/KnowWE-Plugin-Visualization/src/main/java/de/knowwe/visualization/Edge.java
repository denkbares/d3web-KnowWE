/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.visualization;

/**
 * An edge in a named graph comprising a source node (predicate), a named
 * relation (predicate), and a target node (object).
 *
 * @author Jochen Reutelshöfer
 * @created 06.12.2012
 */
public class Edge {

	private final ConceptNode subject;
	private final ConceptNode object;
	private String predicate;
	private boolean isBidirectionalEdge;
	private boolean isSuperProperty;
	private String relationURI;

	public Edge(ConceptNode s, String p, ConceptNode o) {
		this.subject = s;
		this.setPredicate(p);
		this.setRelationURI(null);
		this.object = o;
		this.setBidirectionalEdge(false);
		this.setSuperProperty(false);
	}

	public Edge(ConceptNode s, String p, String r, ConceptNode o) {
		this.subject = s;
		this.setPredicate(p);
		this.setRelationURI(r);
		this.object = o;
		this.setBidirectionalEdge(false);
		this.setSuperProperty(false);
	}

	public Edge(ConceptNode s, String p, String r, ConceptNode o, boolean superProperty) {
		this.subject = s;
		this.setPredicate(p);
		this.setRelationURI(r);
		this.object = o;
		this.setBidirectionalEdge(false);
		this.setSuperProperty(superProperty);
	}

	public boolean isOuter() {
		return subject.isOuter() || object.isOuter();
	}

	@Override
	public String toString() {
		return subject + " " + getPredicate() + ": " + object;
	}

	@Override
	public int hashCode() {

		return subject.hashCode() + getPredicate().hashCode() + object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Edge) {
			Edge other = ((Edge) obj);
			return other.subject.equals(this.subject) && other.getPredicate().equals(this.getPredicate())
					&& other.object.equals(this.object);
		}
		return super.equals(obj);
	}

	public ConceptNode getSubject() {
		return subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public ConceptNode getObject() {
		return object;
	}

	public boolean isBidirectionalEdge() {
		return isBidirectionalEdge;
	}

	public void setBidirectionalEdge(boolean bidirectionalEdge) {
		isBidirectionalEdge = bidirectionalEdge;
	}

	public boolean isSuperProperty() {
		return isSuperProperty;
	}

	public void setSuperProperty(boolean superProperty) {
		isSuperProperty = superProperty;
	}

	public String getRelationURI() {
		return relationURI;
	}

	public void setRelationURI(String relationURI) {
		this.relationURI = relationURI;
	}
}
