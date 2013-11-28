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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;


public class ResultTableModel {

	private final Map<Node, TableRow> data = new LinkedHashMap<Node, TableRow>();
	private final List<String> variables;

	public int getSize() {
		return data.size();
	}

	public ResultTableModel(QueryResultTable result) {
		this.variables = result.getVariables();
		populateTable(result);
	}

	public ResultTableModel(List<TableRow> rows, List<String> variables) {
		this.variables = variables;
		for (TableRow row : rows) {
			importRow(row);
		}
	}

	public ResultTableModel(List<String> variables) {
		this.variables = variables;
	}



	public Iterator<TableRow> iterator() {
		Iterator<TableRow> iterator = data.values().iterator();
		return iterator;
	}

	private void populateTable(QueryResultTable result) {
		ClosableIterator<QueryRow> iterator = result.iterator();
		while (iterator.hasNext()) {
			QueryRow queryRow = iterator.next();

			importRow(queryRow);

		}
		iterator.close();

	}

	private void importRow(TableRow row) {
		Node firstNode = row.getValue(variables.get(0));
		this.data.put(firstNode, row);

	}

	private void importRow(QueryRow queryRow) {

		Node firstNode = queryRow.getValue(variables.get(0));
		this.data.put(firstNode, new QueryRowTableRow(queryRow));

	}



	public List<String> getVariables() {
		return variables;
	}

	public TableRow findRowFor(Node ascendorParent) {
		return data.get(ascendorParent);
	}

	public void addTableRow(TableRow artificialTopLevelRow) {
		importRow(artificialTopLevelRow);
	}

}

