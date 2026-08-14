/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Provides the values available for filtering a single column of a {@link GroupedFilterListSectionsRenderer}, to be
 * offered in the column's filter popup. The action rebuilds the list definition of the requested section via
 * {@link ListSectionsProvider#buildList(Section, de.knowwe.core.user.UserContext)} and collects the distinct values of
 * the requested column.
 * <p>
 * The request parameters and the response format are the same as for the other filter providers of the pagination
 * tables, see {@code Pagination.js}: the column is denoted by {@code column-name}, an optional text the values have to
 * contain by {@code filter-text-query}. The response contains the {@code filter-texts} as an array of arrays, whose
 * first entry is the text to be displayed and whose remaining entries are the texts to be filtered for.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 2026-08-14
 */
public class ListSectionsFilterProviderAction extends AbstractAction {

	private static final String COLUMN_NAME = "column-name";
	private static final String FILTER_TEXT_QUERY = "filter-text-query";
	private static final String FILTER_TEXTS = "filter-texts";
	private static final int MAX_FILTER_COUNT = 200;

	/**
	 * The text displayed for the value used to filter for cells without content.
	 */
	public static final String EMPTY = "<Empty>";

	// show cells without content first, sort the remaining values naturally
	private static final Comparator<String> COMPARATOR = (o1, o2) -> {
		boolean blank1 = Strings.isBlank(o1);
		boolean blank2 = Strings.isBlank(o2);
		if (blank1 != blank2) return blank1 ? -1 : 1;
		return NumberAwareComparator.CASE_INSENSITIVE.compare(o1, o2);
	};

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (context.getWriter() == null) return;

		Section<?> section = getSection(context);
		if (!(section.get() instanceof ListSectionsProvider provider)) {
			failUnexpected(context, "The referenced section does not provide a filterable list.");
			return;
		}
		String columnName = context.getParameter(COLUMN_NAME);
		if (Strings.isBlank(columnName)) {
			failUnexpected(context, "The request did not specify the column to provide filter values for.");
			return;
		}
		String filterTextQuery = context.getParameter(FILTER_TEXT_QUERY);
		// the client compares the echoed query to detect outdated responses, so it must never be null
		if (filterTextQuery == null) filterTextQuery = "";

		List<String> values = new ArrayList<>(provider.buildList(section, context)
				.collectFilterValues(columnName, filterTextQuery, MAX_FILTER_COUNT));
		values.sort(COMPARATOR);

		JSONArray filterTexts = new JSONArray();
		for (String value : values) {
			filterTexts.put(new JSONArray(List.of(Strings.isBlank(value) ? EMPTY : value, value)));
		}
		JSONObject response = new JSONObject();
		response.put(FILTER_TEXTS, filterTexts);
		response.put(FILTER_TEXT_QUERY, filterTextQuery);
		context.setContentType(JSON);
		response.write(context.getWriter());
	}
}
