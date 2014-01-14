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
		return data;
	}

	private final Map<Node, Set<TableRow>> data = new LinkedHashMap<Node, Set<TableRow>>();
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

	public boolean contains(TableRow row) {
		Node subjectValue = row.getValue(variables.get(0));
		Set<TableRow> subjectRows = data.get(subjectValue);
		if (subjectRows == null) return false;
		for (TableRow tableRow : subjectRows) {
			if(row.equals(tableRow)) return true;
		}
		return false;
	}
	


	public Iterator<TableRow> iterator() {
		List<TableRow> result = new ArrayList<TableRow>();
		for (Set<TableRow> list : data.values()) {
			result.addAll(list);
		}
		return result.iterator();
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
		Set<TableRow> nodeRows = data.get(firstNode);
		if (nodeRows == null) {
			nodeRows = new HashSet<TableRow>();
			data.put(firstNode, nodeRows);
		}
		nodeRows.add(row);

	}

	@Override
	public String toString() {
		Set<Node> keySet = data.keySet();
		StringBuffer buffy = new StringBuffer();
		buffy.append("Variables: " + variables.toString() + "\n");
		for (Node node : keySet) {
			Set<TableRow> set = data.get(node);
			for (TableRow tableRow : set) {
				buffy.append(tableRow.toString() + "\n");
			}
		}
		return buffy.toString();
	}

	private void importRow(QueryRow queryRow) {

		Node firstNode = queryRow.getValue(variables.get(0));
		Set<TableRow> nodeRows = data.get(firstNode);
		if (nodeRows == null) {
			nodeRows = new HashSet<TableRow>();
			data.put(firstNode, nodeRows);
		}
		nodeRows.add(new QueryRowTableRow(queryRow, variables));

	}



	public List<String> getVariables() {
		return variables;
	}

	public Collection<TableRow> findRowFor(Node ascendorParent) {
		return data.get(ascendorParent);
	}

	public void addTableRow(TableRow artificialTopLevelRow) {
		importRow(artificialTopLevelRow);
	}

	public static List<Message> checkEquality(ResultTableModel expectedResultTable, ResultTableModel actualResultTable) {
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

