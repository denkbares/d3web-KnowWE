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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

public class RecentChangesFilterProviderAction extends AbstractAction {

	private static final String COLUMN_NAME = "column-name";
	private static final String FILTER_TEXT_QUERY = "filter-text-query";
	private static final String FILTER_TEXTS = "filter-texts";
	private static final int MAX_FILTER_COUNT = 200;
	public static final String EMPTY = "<Empty>";
	private static final RecentChangesUtils util = new RecentChangesUtils();

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
	private static final Comparator<Date> DATE_COMPARATOR = Comparator.naturalOrder();

	@Override
	public void execute(UserActionContext context) throws IOException {

		if (context.getWriter() != null) {
			String filterTextQuery = context.getParameter(FILTER_TEXT_QUERY);
			@NotNull Map<String, Set<String>> filterTexts = getFilterTexts(context, filterTextQuery);
			context.setContentType(JSON);
			JSONArray filterTextsArray = new JSONArray();
			Comparator<Map.Entry<String, Set<String>>> keyComparator = Map.Entry.comparingByKey(COMPARATOR);
			if(getColumnName(context).equals(LAST_MODIFIED)){
				keyComparator = keyComparator.reversed();
			}
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
	private String getColumnName(UserActionContext context){
		return context.getParameter(COLUMN_NAME);
	}

	protected Map<String, Set<String>> getFilterTexts(UserActionContext context, String filterTextQuery) throws IOException {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		Set<Page> recentChanges = wikiConnector.getPageManager().getRecentChanges();
		LinkedHashSet<Page> totalChangesSet = new LinkedHashSet<>(recentChanges);
		for (Page page : recentChanges) {
			if (page instanceof Attachment) continue;
			List<Page> pageHistory = wikiConnector.getPageManager().getVersionHistory(page.getName());
			totalChangesSet.addAll(pageHistory);
		}
		String columnName = context.getParameter(COLUMN_NAME);
		LinkedHashMap<String, Set<String>> filterTexts = new LinkedHashMap<>();
		Set<String> filteredOut = new LinkedHashSet<>();
		Set<String> addedFilterValueTexts = new LinkedHashSet<>();
		Section<?> section = getSection(context);
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(section, context);
		filter.put(columnName, Collections.emptySet());
		Set<Page> filteredRecentChanges = new RecentChangesRenderer().filter(filter, totalChangesSet);
		for (Page page : filteredRecentChanges) {
			String text = util.getColumnValueByName(columnName, page);
			if (addedFilterValueTexts.contains(text) || filteredOut.contains(text)) continue;
			if (isFilteredOut(filterTextQuery, text)) {
				filteredOut.add(text);
				continue;
			}
			filterTexts.computeIfAbsent(text, k -> new HashSet<>()).add(text);
			addedFilterValueTexts.add(text);
			if (filterTexts.size() >= MAX_FILTER_COUNT) {
				break;
			}
		}
		if (filterTexts.size() >= MAX_FILTER_COUNT) {
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
}
