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
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;

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
			Collection<String> filterTexts = getFilterTexts(context, filterTextQuery);
			context.setContentType(JSON);
			JSONArray filterTextsArray = new JSONArray();
			for (String text : filterTexts) {
				filterTextsArray.put(text);
			}
			JSONObject response = new JSONObject();
			response.put(FILTER_TEXTS, filterTextsArray);
			response.put(FILTER_TEXT_QUERY, filterTextQuery);
			response.write(context.getWriter());
		}
	}

	@NotNull
	protected Collection<String> getFilterTexts(UserActionContext context, String filterTextQuery) throws IOException {
		Set<String> filterTexts = new HashSet<>();
		Section<SparqlContentType> section = getSection(context, SparqlContentType.class);
		String columnName = context.getParameter(COLUMN_NAME);
		if (columnName == null) return filterTexts;
		String sparqlQuery = section.get().getSparqlQuery(section, context);
		if (sparqlQuery == null) return filterTexts;

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) return filterTexts;

		CachedTupleQueryResult bindingSets = compiler.getRdf2GoCore().sparqlSelect(sparqlQuery);
		for (BindingSet bindingSet : bindingSets) {
			Value value = bindingSet.getValue(columnName);
			if (value == null) continue;
			String valueText = value.stringValue();
			if (!Strings.isBlank(filterTextQuery) && !Strings.containsIgnoreCase(valueText, filterTextQuery)) continue;
			filterTexts.add(valueText);
			if (filterTexts.size() >= MAX_FILTER_COUNT) break;
		}

		ArrayList<String> sorted = new ArrayList<>(filterTexts);
		sorted.sort(NumberAwareComparator.CASE_INSENSITIVE);
		return sorted;
	}
}
