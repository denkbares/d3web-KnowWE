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

import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.11.2011
 */
public class SparqlQuery {

	protected SparqlQuery next = null;

	protected SparqlQuery previous = null;

	protected String content = "";

	public SELECT SELECT(String... select) {
		return (SELECT) addNext(new SELECT(), select);
	}

	protected SparqlQuery addNext(SparqlQuery next, String... content) {
		this.next = next;
		next.previous = this;
		next.content = next.createContent(content);
		return next;
	}

	@Override
	public String toString() {
		SparqlQuery current = this;
		StringBuilder builder = new StringBuilder();
		while (current != null) {
			builder.insert(0, current.verbalize());
			current = current.previous;
		}
		return builder.toString();
	}

	public String verbalize() {
		return this.content;
	}

	public String createContent(String... content) {
		StringBuilder contentBuilder = new StringBuilder();
		for (String part : content) {
			contentBuilder.append(part).append(" ");
		}
		return contentBuilder.toString();
	}

	public String toSparql(Rdf2GoCore core) {
		return Rdf2GoUtils.createSparqlString(core, toString());
	}

}
