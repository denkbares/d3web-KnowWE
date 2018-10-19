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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.query.BindingSet;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.collections.SubSpanIterator;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.utils.Pair;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ResultTableModel implements Iterable<TableRow> {

	public static final int MAX_DISPLAYED_FAILRUES = 20;

	public Map<Value, Set<TableRow>> getData() {
		if (groupedRows == null) {
			Map<Value, Set<TableRow>> data = new LinkedHashMap<>();
			for (TableRow row : rows) {
				Value firstNode = row.getValue(variables.get(0));
				Set<TableRow> nodeRows = data.computeIfAbsent(firstNode, k -> new HashSet<>());
				nodeRows.add(row);
			}
			groupedRows = data;
		}
		return groupedRows;
	}

	private final Collection<TableRow> rows = new LinkedHashSet<>();

	private final List<String> variables;

	private Map<Value, Set<TableRow>> groupedRows = null;

	public int getSize() {
		return rows.size();
	}

	private final List<Comparator<TableRow>> comparators = new LinkedList<>();

	public ResultTableModel(CachedTupleQueryResult result) {
		this.variables = result.getBindingNames();
		populateTable(result);
		result.close();
	}

	public ResultTableModel(List<TableRow> rows, List<String> variables) {
		this.variables = variables;
		rows.forEach(this::importRow);
	}

	public ResultTableModel(List<String> variables) {
		this.variables = variables;
	}

	public boolean contains(TableRow row) {
		return rows.contains(row);
	}

	@NotNull
	@Override
	public Iterator<TableRow> iterator() {
		if (comparators.isEmpty()) {
			return rows.iterator();
		}
		else {
			ArrayList<TableRow> tableRows = new ArrayList<>(rows);
			Collections.reverse(comparators);
			for (Comparator<TableRow> comparator : comparators) {
				tableRows.sort(comparator);
			}
			return tableRows.iterator();
		}
	}

	public void sortRows(List<Pair<String, Boolean>> sorting) {
		for (Pair<String, Boolean> stringBooleanPair : sorting) {
			final int factor = stringBooleanPair.getB() ? 1 : -1;
			Comparator<TableRow> comparator = (o1, o2) -> {
				Value v1 = o1.getValue(stringBooleanPair.getA());
				Value v2 = o2.getValue(stringBooleanPair.getA());
				int result = (v1 == v2) ? 0 : (v1 == null) ? -1 : (v2 == null) ? 1 : v1.stringValue()
						.compareTo(v2.stringValue());
				return factor * result;
			};
			comparators.add(comparator);
		}
	}

	/**
	 * Returns an iterator for a subset of the rows, starting from row 'start' inclusively (where 0
	 * is the first row) and end before row "end" (exclusively). If "start" is below 0, it will be
	 * assumed as 0. If "end" is above the current number of rows or end is below 0, it will be
	 * assumed to be the number of rows.
	 *
	 * @param start the first row to iterate
	 * @param end   the row to stop iteration before
	 * @return an iterator for the sub-span of rows
	 */
	public Iterator<TableRow> iterator(int start, int end) {
		return new SubSpanIterator<>(iterator(), start, end);
	}

	private void populateTable(TupleQueryResult result) {
		for (BindingSet queryRow : result.getBindingSets()) {
			importRow(queryRow);
		}
	}

	private void importRow(TableRow row) {
		rows.add(row);
		groupedRows = null;
	}

	@Override
	public String toString() {
		StringBuilder buffy = new StringBuilder();
		buffy.append("Variables: ").append(variables).append("\n");
		for (TableRow tableRow : rows) {
			buffy.append(tableRow).append("\n");
		}
		return buffy.toString();
	}

	private void importRow(BindingSet queryRow) {
		rows.add(new QueryRowTableRow(queryRow, variables));
	}

	public List<String> getVariables() {
		return variables;
	}

	@NotNull
	public Collection<TableRow> findRowFor(Value ascendorParent) {
		return getData().getOrDefault(ascendorParent, Collections.emptySet());
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
	 * @param expectedResultTable the expected data
	 * @param actualResultTable   the actual data
	 * @param atLeast             false: equality of data is required; true: expectedData SUBSET-OF actualData
	 *                            is required
	 * @return if the expected data is equal to (or a subset of) the actual data
	 * @created 20.01.2014
	 */
	public static MultiMap<String, Message> checkEquality(Rdf2GoCore core, ResultTableModel expectedResultTable, ResultTableModel actualResultTable, boolean atLeast) {
		MultiMap<String, Message> errorMessages = new DefaultMultiMap<>(MultiMaps.linkedFactory(), MultiMaps.linkedFactory());

		/*
		 * 2. Compare all result rows (except for those with blank nodes)
		 */
		for (TableRow expectedTableRow : expectedResultTable) {
			// found no easy way to retrieve and match statements with
			// blanknodes, so for now we skip them from the check...
			boolean containsBlankNode = false;
			for (String var : expectedResultTable.getVariables()) {
				if (expectedTableRow.getValue(var) instanceof BNode) {
					containsBlankNode = true;
					break;
				}
			}
			if (containsBlankNode) {
				continue;
			}

			boolean contained = actualResultTable.contains(expectedTableRow);
			if (!contained) {
				errorMessages.put("expected rows missing", new Message(Type.ERROR, expectedTableRow.toString()));
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

		Map<Value, Set<TableRow>> expectedData = expectedResultTable.getData();
		Map<Value, Set<TableRow>> actualData = actualResultTable.getData();
		Set<Value> keySet = actualData.keySet();
		for (Value node : keySet) {
			if (node != null && !(node instanceof BNode)) {
				if (!expectedData.keySet().contains(node)) {
					errorMessages.put("expected nodes missing",
							new Message(Type.ERROR, Rdf2GoUtils.reduceNamespace(core, node.toString())));
					continue;
				}

				if (!(expectedData.get(node).size() == (actualData.get(node).size()))) {
					errorMessages.put("cases where number of result columns does not match",
							new Message(Type.ERROR, Rdf2GoUtils.reduceNamespace(core, node.toString())));
				}
			}
		}
		return errorMessages;
	}

	public String toCSV() throws IOException {
		StringWriter out = new StringWriter();
		CSVPrinter printer = CSVFormat.DEFAULT.withHeader(variables.toArray(new String[variables.size()]))
				.print(out);
		for (TableRow row : rows) {
			List<Object> values = new ArrayList<>(variables.size());
			for (String variable : variables) {
				Value value = row.getValue(variable);
				values.add(value == null ? null : value.stringValue());
			}
			printer.printRecord(values);
		}
		return out.toString();
	}

	public static ResultTableModel fromCSV(String csv) throws IOException {
		try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new StringReader(csv))) {

			// read the header
			Map<String, Integer> headerMap = parser.getHeaderMap();
			List<String> variables = headerMap.entrySet().stream()
					.sorted(Comparator.comparing(Map.Entry::getValue))
					.map(Map.Entry::getKey).collect(Collectors.toList());

			// read the rows
			List<TableRow> rows = new LinkedList<>();
			for (final CSVRecord record : parser) {
				SimpleTableRow row = new SimpleTableRow();
				for (String variable : variables) {
					String value = record.get(variable);
					if (value != null) {
						row.addValue(variable, new LiteralImpl(value));
					}
				}
				rows.add(row);
			}

			//  and return the parsed table
			return new ResultTableModel(rows, variables);
		}
	}

	public static String generateErrorsText(MultiMap<String, Message> failures) {
		return generateErrorsText(failures, true);
	}

	public static String generateErrorsText(MultiMap<String, Message> failures, boolean full) {
		StringBuilder buffy = new StringBuilder();
		buffy.append("The following test failures occurred:\n");
		for (String type : failures.keySet()) {
			Set<Message> failuresOfType = failures.getValues(type);
			buffy.append(failuresOfType.size()).append(" ").append(type).append(":\n");
			int i = 0;
			for (Message message : failuresOfType) {
				if (!full && ++i > MAX_DISPLAYED_FAILRUES) {
					buffy.append("* ... see expected and actual result linked below\n");
					break;
				}
				buffy.append("* ").append(message.getText()).append("\n");
			}
		}
		return buffy.toString();
	}
}

