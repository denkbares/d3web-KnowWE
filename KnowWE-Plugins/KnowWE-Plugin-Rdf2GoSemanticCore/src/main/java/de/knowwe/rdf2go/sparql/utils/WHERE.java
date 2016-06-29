/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.rdf2go.sparql.utils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.11.2011
 */
public class WHERE extends SparqlQuery {

	public ORDER_BY ORDER_BY(String orderBy) {
		return (ORDER_BY) addNext(new ORDER_BY(), orderBy);
	}

	public WHERE AND_WHERE(String... where) {
		return (WHERE) addNext(new WHERE(), where);
	}
	
	public GROUP_BY GROUP_BY(String groupBy) {
		return (GROUP_BY) addNext(new GROUP_BY(), groupBy);
	}

	@Override
	public String verbalize() {
		boolean isFirst = !(this.previous instanceof WHERE);
		boolean isLast = !(this.next instanceof WHERE);
		return (isFirst ? "WHERE {\n" : "") + content + (isLast ? "}\n" : "");
	}

	@Override
	public String createContent(String... content) {
		StringBuilder contentBuilder = new StringBuilder();
		for (String part : content) {
			contentBuilder.append(part).append(" .\n");
		}
		return contentBuilder.toString();
	}

}
