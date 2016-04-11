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
package de.knowwe.kdom.export;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 06.12.2012
 */
public class Edge {

	private final String subject;
	private final String predicate;
	private final String object;

	/**
	 * 
	 */
	public Edge(String s, String p, String o) {
		this.subject = s;
		this.predicate = p;
		this.object = o;
	}

	@Override
	public int hashCode() {

		return subject.hashCode() + predicate.hashCode() + object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Edge) {
			Edge other = ((Edge) obj);
			return other.subject.equals(this.subject) && other.predicate.equals(this.predicate)
					&& other.object.equals(this.object);
		}
		return super.equals(obj);
	}

	public String getSubject() {
		return subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public String getObject() {
		return object;
	}

}
