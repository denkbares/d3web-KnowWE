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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connector.DummyConnector;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;

import com.denkbares.semanticcore.utils.ResultTableModel;
import com.denkbares.semanticcore.utils.TableRow;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class ExpectedSparqlResultTable extends Table {

	public ExpectedSparqlResultTable() {
		this.injectTableCellContentChildtype(new ExpectedSparqlResultTableCellContent());
		this.setRenderer(new TableRenderer() {
			@Override
			public void render(Section<?> sec, UserContext user, RenderResult string) {
				List<Section<TableCellContent>> sections = $(sec).successor(TableCellContent.class).asList();
				if (sections.isEmpty() || sections.size() == 1 && Strings.isBlank(sections.get(0).getText())) {
					string.appendHtmlElement("span", "Expecting no results for the linked SPARQL query", "class", "emptySparqlResult");
				}
				super.render(sec, user, string);
			}
		});
	}

	public static ResultTableModel getResultTableModel(OntologyCompiler c, Section<ExpectedSparqlResultTable> table, List<String> variables) {
		if (table == null) {
			return new ResultTableModel(Collections.emptyList(), variables);
		}
		List<TableRow> rows = new ArrayList<>();
		List<Section<TableLine>> lines = Sections.successors(table, TableLine.class);

		ResultTableModel.Builder resultTableModelBuilder = ResultTableModel.builder(variables);
		for (Section<TableLine> line : lines) {
			Map<String, Value> row = createResultRow(c, line, variables);
			if (!row.isEmpty()) {
				resultTableModelBuilder.addRow(row);
			}
		}

		return resultTableModelBuilder.build();
	}

	private static boolean isCompatibilityMode() {
		return !Environment.getInstance().getWikiConnector().getBaseUrl().equals(DummyConnector.BASE_URL);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	private static Map<String, Value> createResultRow(OntologyCompiler core, Section<TableLine> line, List<String> variables) {
		List<Section<TableCellContent>> cells = Sections.successors(line, TableCellContent.class);
		Map<String, Value> row = new HashMap<>();
		boolean compatibilityMode = isCompatibilityMode();
		String currentBaseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
		int column = 0;
		for (Section<TableCellContent> cell : cells) {
			String variable = variables.get(column);
			final Section<NodeProvider> nodeProvider = Sections.successor(cell, NodeProvider.class);
			if (nodeProvider == null) {
				row.put(variable, null);
			}
			if (nodeProvider != null) {
				Value value = nodeProvider.get().getNode(core, nodeProvider);
				if (value instanceof IRI && compatibilityMode) {
					String valueString = value.stringValue();
					if (valueString.startsWith(DummyConnector.BASE_URL)) {
						value = core.getRdf2GoCore()
								.createIRI(currentBaseUrl + valueString.substring(DummyConnector.BASE_URL.length()));
					}
				}
				if (value != null) {
					row.put(variable, value);
				}
			}
			column++;
		}
		return row;
	}
}
