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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;

public class ResultTableModel {

	public Map<Node, Set<TableRow>> getData() {
		if (groupedRows == null) {
			Map<Node, Set<TableRow>> data = new LinkedHashMap<Node, Set<TableRow>>();
			for (TableRow row : rows) {
				Node firstNode = row.getValue(variables.get(0));
				Set<TableRow> nodeRows = data.get(firstNode);
				if (nodeRows == null) {
					nodeRows = new HashSet<TableRow>();
					data.put(firstNode, nodeRows);
				}
				nodeRows.add(row);
			}
			groupedRows = data;
		}
		return groupedRows;
	}

	private final Collection<TableRow> rows = new LinkedHashSet<TableRow>();
	private final List<String> variables;

	private Map<Node, Set<TableRow>> groupedRows = null;

	public int getSize() {
		return rows.size();
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

	public boolean contains(TableRow row) {
		return rows.contains(row);
	}

	public Iterator<TableRow> iterator() {
		return rows.iterator();
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
		rows.add(row);
		groupedRows = null;
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer();
		buffy.append("Variables: " + variables.toString() + "\n");
		for (TableRow tableRow : rows) {
			buffy.append(tableRow.toString() + "\n");
		}
		return buffy.toString();
	}

	private void importRow(QueryRow queryRow) {
		rows.add(new QueryRowTableRow(queryRow, variables));
	}

	public List<String> getVariables() {
		return variables;
	}

	public Collection<TableRow> findRowFor(Node ascendorParent) {
		return getData().get(ascendorParent);
	}

	public void addTableRow(TableRow artificialTopLevelRow) {
		importRow(artificialTopLevelRow);
	}

	/**
	 * Compares the two sparql result data tables with each other. Equality is checked if the
	 * atLeast-flag is set false. If the atLeast-flag is set to true, only the subset relation of
	 * expected to actual data is checked.
	 * <p/>
	 * CAUTION: result rows with blank nodes are ignored from consideration (graph isomorphism
	 * problem)
	 *
	 * @param expectedResultTable
	 * @param actualResultTable
	 * @param atLeast false: equality of data is required; true: expectedData SUBSET-OF actualData
	 * is required
	 * @return
	 * @created 20.01.2014
	 */
	public static List<Message> checkEquality(ResultTableModel expectedResultTable, ResultTableModel actualResultTable, boolean atLeast) {
		List<Message> errorMessages = new ArrayList<Message>();

		/*
		 * 2. Compare all result rows (except for those with blank nodes)
		 */
		Iterator<TableRow> iterator = expectedResultTable.iterator();
		while (iterator.hasNext()) {
			TableRow expectedTableRow = iterator.next();

			// found no easy way to retrieve and match statements with
			// blanknodes, so for now we skip them from the check...
			boolean containsBlankNode = false;
			for (String var : expectedResultTable.getVariables()) {
				if (expectedTableRow.getValue(var) instanceof BlankNode) {
					containsBlankNode = true;
					break;
				}
			}
			if (containsBlankNode) {
				continue;
			}

			boolean contained = actualResultTable.contains(expectedTableRow);
			if (!contained) {
				errorMessages.add(new Message(Type.ERROR, "result does not contain expected row: "
						+ expectedTableRow.toString()));
			}
		}

		if (atLeast) {
			/*
			 * if the atLeast-flag is set we stop at this point as we asserted
			 * that the actual data contains at least the rows from the expected
			 * data
			 */
			return errorMessages;
		}

		/*
		 * 3. Compare numbers for each subject node (except for blank nodes)
		 */

		Map<Node, Set<TableRow>> expectedData = expectedResultTable.getData();
		Map<Node, Set<TableRow>> actualData = actualResultTable.getData();
		Set<Node> keySet = actualData.keySet();
		for (Node node : keySet) {
			if (!(node instanceof BlankNode)) {
				if (!expectedData.keySet().contains(node)) {
					errorMessages.add(new Message(Type.ERROR, "node not contained: "
							+ node.toString()));
					continue;
				}

				if (!(expectedData.get(node).size() == (actualData.get(node).size()))) {
					errorMessages.add(new Message(Type.ERROR,
							"number of result rows not matching for: " + node.toString()));
				}
			}
		}
		return errorMessages;
	}

	public static String generateErrorsText(List<Message> errorMessages) {
		StringBuffer buffy = new StringBuffer();
		buffy.append("The following test failures occured:\n");
		for (Message message : errorMessages) {
			buffy.append(message.getText() + "\n");
		}
		return buffy.toString();
	}

}

