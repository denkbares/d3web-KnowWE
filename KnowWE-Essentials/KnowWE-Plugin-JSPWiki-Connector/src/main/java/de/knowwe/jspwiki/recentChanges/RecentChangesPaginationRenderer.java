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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.jspwiki.PageComparator;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;
import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.CHANGE_NOTES;

@SuppressWarnings("rawtypes")
public class RecentChangesPaginationRenderer extends PaginationRenderer {
	public RecentChangesPaginationRenderer(Renderer decoratedRenderer, SortingMode sorting, boolean supportFiltering) {
		super(decoratedRenderer, sorting, supportFiltering);
	}

	static boolean getCheckbox(UserContext userContext, String checkBox) {
		JSONObject localSectionStorage = AbstractAction.getLocalSectionStorage(userContext);
		Boolean show = null;
		try {
			show = Boolean.valueOf(String.valueOf(localSectionStorage.get(checkBox)));
		}
		catch (Exception ignored) {
		}
		if ((localSectionStorage.isEmpty() || show == null) && checkBox.equals("page")) {
			return true;
		}
		if ((localSectionStorage.isEmpty() || show == null) && checkBox.equals("attachment")) {
			return false;
		}
		return (boolean) localSectionStorage.get(checkBox);
	}

	static boolean checkboxSet(UserContext userContext) {
		JSONObject localSectionStorage = AbstractAction.getLocalSectionStorage(userContext);
		Boolean showPage = null;
		Boolean showAtt = null;

		try {
			showPage = Boolean.valueOf(String.valueOf(localSectionStorage.get("page")));
			showAtt = Boolean.valueOf(String.valueOf(localSectionStorage.get("attachment")));
		}
		catch (Exception ignored) {
		}
		return showPage != null || showAtt != null;
	}

	boolean isChecked(UserContext userContext, String checkBox) {
		JSONObject localSectionStorage = AbstractAction.getLocalSectionStorage(userContext);
		Boolean show = null;
		try {
			show = Boolean.valueOf(String.valueOf(localSectionStorage.get(checkBox)));
		}
		catch (Exception ignored) {
		}
		if ((localSectionStorage.isEmpty() || show == null) && checkBox.equals("page")) {
			return true;
		}
		if ((localSectionStorage.isEmpty() || show == null) && checkBox.equals("attachment")) {
			return false;
		}
		return (boolean) localSectionStorage.get(checkBox);
	}

	@Override
	protected void renderFilterTools(Section<?> section, UserContext user, RenderResult result) {
//		renderToolBarElement(section, result, () -> {
//			boolean tickedPage = isChecked(user, "page");
//			boolean tickedAtt = isChecked(user, "attachment");
//			// generate unique id in case the filter is added multiple times
//			String id = "filter-activator-" + section.getID();
//			Integer activators = (Integer) user.getRequest().getAttribute(id);
//			if (activators == null) activators = 0;
//			String uniqueId = id + "-" + activators;
//			user.getRequest().setAttribute(id, activators + 1);
//			result.appendHtmlTag("input", "class", "filter-activator filter-style", "type", "checkbox", "id", uniqueId, "name", uniqueId);
//			result.appendHtmlElement("label", "Filter", "class", "fillText", "for", uniqueId);
//			result.appendHtml("<div class='filter-tools'>");
		boolean tickedPage = isChecked(user, "page");
		boolean tickedAtt = isChecked(user, "attachment");
		if (tickedPage) {
			result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-pages", "id", "showPages", "name", "showPages", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'page')", "checked", "checked");
		}
		else {
			result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-pages", "id", "showPages", "name", "showPages", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'page')");
		}
		result.appendHtmlElement("label", "Pages", "class", "fillText", "for", "showPages");
		if (tickedAtt) {
			result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-attachments", "id", "showAttachments", "name", "showAttachments", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'attachment')", "checked", "checked");
		}
		else {
			result.appendHtmlTag("input", "type", "checkbox", "class", "filter-style show-attachments", "id", "showAttachments", "name", "showAttachments", "onclick", "KNOWWE.plugin.jspwikiConnector.setPageFilter(this, 'attachment')");
		}
		result.appendHtmlElement("label", "Attachments", "class", "fillText", "for", "showAttachments");
		result.appendHtmlElement("button", "Clear Filter", "class", "clear-filter");
		//});
	}

	@Override
	protected void renderTableSizeSelector(Section<?> sec, UserContext user, RenderResult result) {
		int count = getCount(sec, user);
		renderToolBarElement(sec, result, () -> {
			Integer[] sizeArray = getSizeChoices(user);
			result.appendHtml("<span class='fillText'>Show </span><select class='count'>");

			boolean foundSelected = false;
			for (Integer size : sizeArray) {
				boolean selected = count == size;
				if (selected) foundSelected = true;
				boolean setSelected = selected || size == Integer.MAX_VALUE && !foundSelected;
				result.appendHtml("<option "
						+ (setSelected ? "selected='selected' " : "")
						+ "value='" + size + "'>"
						+ (size == Integer.MAX_VALUE ? "All" : String.valueOf(size))
						+ "</option>");
			}
			result.appendHtml("</select>");
			result.appendHtml(getResultSizeTag(sec, user));
			result.appendHtml("<div class='toolSeparator'>");
			result.appendHtml("</div>");
		});
	}

	private String getResultSizeTag(Section<?> sec, UserContext user) {
		String resultSize = String.valueOf(getRecentChanges(sec, user).size());
		String tag = "";
		if (resultSize.equals(UNKNOWN_RESULT_SIZE)) {
			tag += "<span class=fillText> rows (overall number unknown)</span>";
		}
		else {
			int resultSizeInt = Integer.parseInt(resultSize);
			tag += "<input class='resultSize' style='display:none' value='" + resultSize + "'/>";
			tag += "<span class=fillText>" + (getCount(sec, user) == Integer.MAX_VALUE ? "" : " of");
			tag += " " + resultSize + " " + Strings.pluralOf(resultSizeInt, "row", false) + "</span>";
		}
		return tag;
	}

	private static final RecentChangesUtils util = new RecentChangesUtils();
	private static final Logger LOGGER = LoggerFactory.getLogger(RecentChangesRenderer.class);

	public List<Page> getRecentChanges(Section<?> sec, UserContext user) {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		Set<Page> recentChanges = wikiConnector.getPageManager().getRecentChanges();
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(sec, user);
		Set<Page> filteredRecentChanges = filter(filter, recentChanges);
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);
		PaginationRenderer.setResultSize(user, filteredRecentChanges.size());
		List<Page> sortedFilteredRecentChanges = sortPages(sec, user, filteredRecentChanges);
		return getRecentChangesWithShowFilter(sortedFilteredRecentChanges, user);
	}

	private List<Page> getRecentChangesWithShowFilter(List<Page> sortedFilteredRecentChanges, UserContext user) {
		List<Page> recentChangesCleaned = new ArrayList<>();
		boolean showPages = RecentChangesPaginationRenderer.getCheckbox(user, "page");
		boolean showAttachments = RecentChangesPaginationRenderer.getCheckbox(user, "attachment");
		for (Page page : sortedFilteredRecentChanges) {
			if (!(page instanceof Attachment) && showPages) {
				recentChangesCleaned.add(page);
			}
			else if (page instanceof Attachment && showAttachments) {
				recentChangesCleaned.add(page);
			}
		}
		return recentChangesCleaned;
	}

	@NotNull
	private List<Page> sortPages(Section<?> sec, UserContext user, Set<Page> filteredRecentChanges) {
		List<Pair<String, Comparator>> columnComparators = PaginationRenderer.getMultiColumnSorting(sec, user).stream()
				.map(p -> new Pair<>(p.getA(), createComparator(p.getA(), p.getB())))
				.collect(Collectors.toList());
		PageComparator pageComparator = new PageComparator(columnComparators);
		List<Page> sortedFilteredRecentChanges = new ArrayList<>(filteredRecentChanges);
		sortedFilteredRecentChanges.sort(pageComparator);
		return sortedFilteredRecentChanges;
	}

	private Comparator createComparator(String columnName, boolean ascending) {
		if (columnName.equals(LAST_MODIFIED)) {
			Comparator<Date> comparator = Date::compareTo;
			return ascending ? comparator : comparator.reversed();
		}
		else {
			Comparator<String> comparator = String::compareToIgnoreCase;
			return ascending ? comparator : comparator.reversed();
		}
	}

	public Set<Page> filter(@NotNull Map<String, Set<Pattern>> filter, Set<Page> recentChanges) {
		Set<Page> filteredPages = new LinkedHashSet<>();
		if (filter.isEmpty()) return recentChanges;
		for (Page page : recentChanges) {
			boolean pageMatches = true;
			for (String columnName : filter.keySet()) {
				String text = util.getColumnValueByName(columnName, page);
				Set<Pattern> patterns = filter.getOrDefault(columnName, Set.of());
				if (patterns.size() < 1) {
					continue;
				}
				if (patterns.stream().noneMatch(p -> p.matcher(text).matches())) {
					pageMatches = false;
					break;
				}
			}
			if (pageMatches) {
				filteredPages.add(page);
			}
			JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
			List<Page> versionHistory;
			try {
				versionHistory = wikiConnector.getPageManager().getVersionHistory(page.getName());
			}
			catch (Exception e) {
				versionHistory = List.of();
				LOGGER.error("Exception while getting version history", e);
			}
			Set<Pattern> lastModified = filter.getOrDefault(LAST_MODIFIED, Set.of());
			if (!lastModified.isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithDate(versionHistory, lastModified));
			}
			Set<Pattern> authors = filter.getOrDefault(AUTHOR, Set.of());
			if (!authors.isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithString(versionHistory, authors, AUTHOR));
			}
			Set<Pattern> changeNotes = filter.getOrDefault(CHANGE_NOTES, Set.of());
			if (!changeNotes.isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithString(versionHistory, changeNotes, CHANGE_NOTES));
			}
		}
		return filteredPages;
	}

	private List<Page> checkVersionHistoryWithDate(List<Page> versionHistory, Set<Pattern> patterns) {
		List<Page> filteredPages = new ArrayList<>();
		for (Page page : versionHistory) {
			String formattedDate = util.toDateString(page.getLastModified());
			if (patterns.stream().anyMatch(p -> p.matcher(formattedDate).matches())) {
				filteredPages.add(page);
			}
		}
		return filteredPages;
	}

	private List<Page> checkVersionHistoryWithString(List<Page> versionHistory, Set<Pattern> patterns, String type) {
		List<Page> filteredPages = new ArrayList<>();
		for (Page page : versionHistory) {
			String toMatch = util.getColumnValueByName(type, page);
			if (patterns.stream().anyMatch(p -> p.matcher(toMatch).matches())) {
				filteredPages.add(page);
			}
		}
		return filteredPages;
	}
}
