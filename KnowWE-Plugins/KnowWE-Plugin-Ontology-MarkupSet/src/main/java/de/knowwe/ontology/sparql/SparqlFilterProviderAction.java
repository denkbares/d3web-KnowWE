/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.knowwe.ontology.sparql;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.utils.IndexedResultTableModel;
import com.denkbares.semanticcore.utils.ResultTableModel;
import com.denkbares.semanticcore.utils.TableRow;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;

/**
 * Provides a list of filter texts for a specific SPARQL table column, to be used in filter feature for sparql tables
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.10.2020
 */
public class SparqlFilterProviderAction extends AbstractAction {

	private static final String COLUMN_NAME = "column-name";
	private static final String FILTER_TEXT_QUERY = "filter-text-query";
	private static final String FILTER_TEXTS = "filter-texts";
	private static final int MAX_FILTER_COUNT = 200;
	public static final String EMPTY = "<Empty>";
	private static final Comparator<String> COMPARATOR = (o1, o2) -> {
		if (EMPTY.equals(o1) && EMPTY.equals(o2)) {
			return 0;
		}
		else if (EMPTY.equals(o1)) {
			return -1;
		}
		else if (EMPTY.equals(o2)) {
			return 1;
		}
		else {
			return NumberAwareComparator.CASE_INSENSITIVE.compare(o1, o2);
		}
	};

	@Override
	public void execute(UserActionContext context) throws IOException {

		if (context.getWriter() != null) {
			String filterTextQuery = context.getParameter(FILTER_TEXT_QUERY);
			@NotNull Map<String, Set<String>> filterTexts = getFilterTexts(context, filterTextQuery);
			context.setContentType(JSON);
			JSONArray filterTextsArray = new JSONArray();
			filterTexts.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey(COMPARATOR))
					.forEach(e -> {
						JSONArray textPair = new JSONArray();
						textPair.put(e.getKey());
						e.getValue().forEach(textPair::put);
						filterTextsArray.put(textPair);
					});
			JSONObject response = new JSONObject();
			response.put(FILTER_TEXTS, filterTextsArray);
			response.put(FILTER_TEXT_QUERY, filterTextQuery);
			response.write(context.getWriter());
		}
	}

	/**
	 * Generate a map of filter texts (rendered text to set of value text) to be shown filtering a sparql table
	 *
	 * @param context         context of the action
	 * @param filterTextQuery the query for which we are currently filtering the returned list
	 * @return a map with the filter texts
	 */
	@NotNull
	protected Map<String, Set<String>> getFilterTexts(UserActionContext context, String filterTextQuery) throws IOException {
		Map<String, Set<String>> filterTexts = new HashMap<>();
		Set<String> filteredOut = new HashSet<>();
		Section<SparqlContentType> section = getSection(context, SparqlContentType.class);
		String columnName = context.getParameter(COLUMN_NAME);
		if (columnName == null) return filterTexts;
		String sparqlQuery = section.get().getSparqlQuery(section, context);
		if (sparqlQuery == null) return filterTexts;
		Rdf2GoCompiler compiler = Compilers.getCompiler(context, section, Rdf2GoCompiler.class);
		if (compiler == null) return filterTexts;

		RenderOptions renderOptions = section.get().getRenderOptions(section, context);

		Set<String> addedFilterValueTexts = new HashSet<>();

		CachedTupleQueryResult bindingSets = compiler.getRdf2GoCore().sparqlSelect(sparqlQuery);
		ResultTableModel table = IndexedResultTableModel.create(bindingSets);

		// pre-apply filters of the other columns
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(section, context);
		// but not the filters of the current columns, so we see which values can be allowed back in
		filter.put(columnName, Collections.emptySet());
		table = table.filter(filter);

		for (TableRow row : table) {
			Value value = row.getValue(columnName);
			if (value == null) {
				addEmptyIfNotFiltered(filterTexts, filterTextQuery);
				continue;
			}

			String valueText = value.stringValue();
			// no need to do expensive rendering if already contained
			if (addedFilterValueTexts.contains(valueText) || filteredOut.contains(valueText)) continue;

			String rendered = getRenderedValue(compiler, columnName, row, context, renderOptions);
			if (isFilteredOut(filterTextQuery, rendered)) {
				filteredOut.add(valueText);
				continue;
			}

			filterTexts.computeIfAbsent(rendered, k -> new HashSet<>()).add(valueText);
			addedFilterValueTexts.add(valueText);
			if (filterTexts.size() >= MAX_FILTER_COUNT) {
				break;
			}
		}

		if (filterTexts.size() >= MAX_FILTER_COUNT) {
			// we cannot be sure yet if empty cells exist, add <empty> just to be sure
			addEmptyIfNotFiltered(filterTexts, filterTextQuery);
		}

		return filterTexts;
	}

	private void addEmptyIfNotFiltered(Map<String, Set<String>> filterTexts, String filterTextQuery) {
		if (isFilteredOut(filterTextQuery, EMPTY)) return;
		filterTexts.computeIfAbsent(EMPTY, k -> new HashSet<>()).add(""); // allow filtering for empty string
	}

	private boolean isFilteredOut(String filterQuery, String value) {
		return !Strings.isBlank(value) && !Strings.containsIgnoreCase(value, filterQuery);
	}

	private String getRenderedValue(Rdf2GoCompiler compiler, String columnName, TableRow tableRow, UserActionContext context, RenderOptions renderOptions) {
		RenderResult renderResult = new RenderResult(context);
		SparqlResultRenderer.getInstance().renderNode(tableRow, columnName, context, renderOptions, renderResult);
		String plain;
		if (renderOptions.isAllowJSPWikiMarkup()) {
			String renderedNode = Strings.htmlToPlain(renderResult.toString());
			String wikiRendered = Environment.getInstance().getWikiConnector()
					.renderWikiSyntax(renderedNode);
			plain = Strings.htmlToPlain(wikiRendered);
		}
		else {
			plain = Strings.htmlToPlain(renderResult.toString());
		}
		return plain;
	}
}
