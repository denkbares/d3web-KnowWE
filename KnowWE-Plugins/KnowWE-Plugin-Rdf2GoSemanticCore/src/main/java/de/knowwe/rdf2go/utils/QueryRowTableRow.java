/*
 * Copyright (C) 2013 Chair of Artificial Intelligence and Applied Informatics
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
package de.knowwe.rdf2go.utils;

import java.util.List;

import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.utils.EqualsUtils;

public class QueryRowTableRow implements TableRow {

	private final QueryRow row;
	private final List<String> variables;

	public QueryRowTableRow(QueryRow r, List<String> variables) {
		row = r;
		this.variables = variables;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		for (String variable : variables) {
			Node value = row.getValue(variable);
			if (value instanceof BlankNode) {
				buffer.append("BlankNode");
			}
			else {
				buffer.append(value);
			}
			buffer.append("  \t");
		}
		return buffer.toString();
	}

	@Override
	public Node getValue(String variable) {
		return row.getValue(variable);
	}

	@Override
	public List<String> getVariables() {
		return variables;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof TableRow)) return false;
		TableRow other = (TableRow) obj;

		if (getVariables().size() != other.getVariables().size()) return false;

		for (String variable : variables) {
			if (!EqualsUtils.equals(other.getValue(variable), row.getValue(variable))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (String variable : variables) {
			Node value = row.getValue(variable);
			int valueHash = value == null ? 0 : value.hashCode();
			result = prime * result + valueHash;
		}
		return result;
	}

}
