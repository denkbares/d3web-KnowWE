/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.recentChanges;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

public class RecentChangesFilterProviderAction extends AbstractAction {

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
			Comparator<Map.Entry<String, Set<String>>> keyComparator = Map.Entry.comparingByKey(getKeyComparator(getColumnName(context)));
			filterTexts.entrySet()
					.stream()
					.sorted(keyComparator)
					.forEach((Map.Entry<String, Set<String>> e) -> {
						JSONArray textPair = new JSONArray();
						String keyString = e.getKey();
						boolean alreadyIn = false;
						for (int i = 0; i < filterTextsArray.length(); i++) {
							try {
								String text = filterTextsArray.getJSONArray(i).getString(0);
								if (text.equals(keyString)) {
									alreadyIn = true;
									break;
								}
							}
							catch (JSONException ignored) {
							}
						}
						if (!alreadyIn) {
							textPair.put(keyString);
							e.getValue().forEach(textPair::put);
							filterTextsArray.put(textPair);
						}
					});
			JSONObject response = new JSONObject();
			response.put(FILTER_TEXTS, filterTextsArray);
			response.put(FILTER_TEXT_QUERY, filterTextQuery);
			response.write(context.getWriter());
		}
	}

	private String getColumnName(UserActionContext context) {
		return context.getParameter(COLUMN_NAME);
	}

	protected Map<String, Set<String>> getFilterTexts(UserActionContext context, String filterTextQuery) throws IOException {
		String columnName = context.getParameter(COLUMN_NAME);
		Set<String> filteredOut = new HashSet<>();
		Set<String> valueTexts = new LinkedHashSet<>();
		Section<?> section = getSection(context);
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(section, context);
		filter.put(columnName, Collections.emptySet());
		Set<Page> filteredRecentChanges = RecentChangesUtils.getRecentChangesFiltered(context, filter);
		for (Page page : filteredRecentChanges) {
			String text = RecentChangesUtils.getColumnValueByName(columnName, page);
			if (valueTexts.contains(text) || filteredOut.contains(text)) continue;
			if (isFilteredOut(filterTextQuery, text)) {
				filteredOut.add(text);
				continue;
			}
			valueTexts.add(text);
		}
		// Collect all values first and only truncate after sorting: the pages are not ordered by column value
		// (with intermediate versions shown, they are grouped by page instead of by date), so cutting off during
		// collection would produce an arbitrary subset with gaps instead of the first MAX_FILTER_COUNT values.
		boolean truncated = valueTexts.size() > MAX_FILTER_COUNT;
		LinkedHashMap<String, Set<String>> filterTexts = new LinkedHashMap<>();
		valueTexts.stream()
				.sorted(getKeyComparator(columnName))
				.limit(MAX_FILTER_COUNT)
				.forEach(text -> filterTexts.computeIfAbsent(text, k -> new HashSet<>()).add(text));
		if (truncated) {
			addEmptyIfNotFiltered(filterTexts, filterTextQuery);
		}
		return filterTexts;
	}

	private static Comparator<String> getKeyComparator(String columnName) {
		return LAST_MODIFIED.equals(columnName) ? COMPARATOR.reversed() : COMPARATOR;
	}

	private void addEmptyIfNotFiltered(Map<String, Set<String>> filterTexts, String filterTextQuery) {
		if (isFilteredOut(filterTextQuery, EMPTY)) return;
		filterTexts.computeIfAbsent(EMPTY, k -> new HashSet<>()).add(""); // allow filtering for empty string
	}

	private boolean isFilteredOut(String filterQuery, String value) {
		return !Strings.isBlank(value) && !Strings.containsIgnoreCase(value, filterQuery);
	}
}
