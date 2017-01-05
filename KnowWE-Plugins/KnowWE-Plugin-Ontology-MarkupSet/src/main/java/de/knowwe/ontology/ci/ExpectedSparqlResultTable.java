/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.ci;

import java.util.ArrayList;
import java.util.List;

import connector.DummyConnector;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.URIImpl;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.utils.ResultTableModel;
import de.knowwe.rdf2go.utils.SimpleTableRow;
import de.knowwe.rdf2go.utils.TableRow;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class ExpectedSparqlResultTable extends Table {

	public ExpectedSparqlResultTable() {
		this.injectTableCellContentChildtype(new ExpectedSparqlResultTableCellContent());
	}

	public static ResultTableModel getResultTableModel(Section<ExpectedSparqlResultTable> table, List<String> variables, Rdf2GoCompiler c) {
		List<TableRow> rows = new ArrayList<>();
		List<Section<TableLine>> lines = Sections.successors(table, TableLine.class);
		for (Section<TableLine> line : lines) {
			SimpleTableRow row = createResultRow(line, variables, c);
			rows.add(row);
		}

		return new ResultTableModel(rows, variables);
	}

	private static boolean isCompatibilityMode() {
		return !Environment.getInstance().getWikiConnector().getBaseUrl().equals(DummyConnector.BASE_URL);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	private static SimpleTableRow createResultRow(Section<TableLine> line, List<String> variables, Rdf2GoCompiler core) {
		List<Section<NodeProvider>> nodeProviders = Sections.successors(line, NodeProvider.class);
		SimpleTableRow row = new SimpleTableRow();
		boolean compatibilityMode = isCompatibilityMode();
		String currentBaseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
		int column = 0;
		for (Section<NodeProvider> section : nodeProviders) {
			String variable = variables.get(column);
			Value value = section.get().getNode(section, core);
			if (value instanceof URI && compatibilityMode) {
				String valueString = value.stringValue();
				if (valueString.startsWith(DummyConnector.BASE_URL)) {
					value = new URIImpl(currentBaseUrl + valueString.substring(DummyConnector.BASE_URL.length()));
				}
			}
			if (value != null) row.addValue(variable, value);
			column++;
		}
		return row;
	}
}
