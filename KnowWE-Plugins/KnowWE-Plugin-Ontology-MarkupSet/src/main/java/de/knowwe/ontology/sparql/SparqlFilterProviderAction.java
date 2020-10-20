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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		if (context.getWriter() != null) {
			String filterTextQuery = context.getParameter(FILTER_TEXT_QUERY);
			Collection<Pair<String, String>> filterTexts = getFilterTexts(context, filterTextQuery);
			context.setContentType(JSON);
			JSONArray filterTextsArray = new JSONArray();
			for (Pair<String, String> text : filterTexts) {
				JSONArray textPair = new JSONArray();
				textPair.put(text.getA());
				textPair.put(text.getB());
				filterTextsArray.put(textPair);
			}
			JSONObject response = new JSONObject();
			response.put(FILTER_TEXTS, filterTextsArray);
			response.put(FILTER_TEXT_QUERY, filterTextQuery);
			response.write(context.getWriter());
		}
	}

	/**
	 * Generate a collection of pairs to be shown in the filter tooltip/dialog when filtering sparql tables.
	 * The first or A value of the pair is the actual text of the sparql value, the second or B  value is the rendered
	 * label to be displayed besides the check box.
	 *
	 * @param context         context of the action
	 * @param filterTextQuery the query for which we are currently filtering the returned list
	 * @return a collections of pairs: value text, rendered text
	 */
	@NotNull
	protected Collection<Pair<String, String>> getFilterTexts(UserActionContext context, String filterTextQuery) throws IOException {
		Set<Pair<String, String>> filterTexts = new HashSet<>();
		Section<SparqlContentType> section = getSection(context, SparqlContentType.class);
		String columnName = context.getParameter(COLUMN_NAME);
		if (columnName == null) return filterTexts;
		String sparqlQuery = section.get().getSparqlQuery(section, context);
		if (sparqlQuery == null) return filterTexts;
		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) return filterTexts;

		RenderOptions renderOptions = section.get().getRenderOptions(section, context);

		CachedTupleQueryResult bindingSets = compiler.getRdf2GoCore().sparqlSelect(sparqlQuery);
		for (BindingSet bindingSet : bindingSets) {
			Value value = bindingSet.getValue(columnName);
			if (value == null) {
				addEmptyIfNotFiltered(filterTexts, filterTextQuery);
				continue;
			}
			String rendered = getRenderedValue(compiler, columnName, value, context, renderOptions);

			if (isFilteredOut(filterTextQuery, rendered)) continue;

			filterTexts.add(new Pair<>(value.stringValue(), rendered));
			if (filterTexts.size() >= MAX_FILTER_COUNT) {
				addEmptyIfNotFiltered(filterTexts, filterTextQuery);
				break;
			}
		}

		ArrayList<Pair<String, String>> sorted = new ArrayList<>(filterTexts);
		sorted.sort(Comparator.comparing(Pair::getA, NumberAwareComparator.CASE_INSENSITIVE));
		return sorted;
	}

	private void addEmptyIfNotFiltered(Set<Pair<String, String>> filterTexts, String filterTextQuery) {
		String rendered = "<Empty>";
		if (isFilteredOut(filterTextQuery, rendered)) return;
		filterTexts.add(new Pair<>("", rendered)); // allow filtering for empty string
	}

	private boolean isFilteredOut(String filterQuery, String value) {
		return !Strings.isBlank(value) && !Strings.containsIgnoreCase(value, filterQuery);
	}

	private String getRenderedValue(Rdf2GoCompiler compiler, String columnName, Value value, UserActionContext context, RenderOptions renderOptions) {
		String sparqlRendered = SparqlResultRenderer.getInstance()
				.renderNode(value, columnName, renderOptions.isRawOutput(), context, compiler.getRdf2GoCore(), RenderMode.HTML);
		String plain;
		if (renderOptions.isAllowJSPWikiMarkup()) {
			RenderResult renderResult = new RenderResult(context);
			renderResult.appendHtml(sparqlRendered);
			RenderResult renderResult2 = new RenderResult(context);
			renderResult2.appendHtml(Environment.getInstance()
					.getWikiConnector()
					.renderWikiSyntax(renderResult.toStringRaw()));
			plain = Strings.htmlToPlain(renderResult2.toString());
		}
		else {
			plain = Strings.htmlToPlain(sparqlRendered);
		}
		return plain;
	}
}
