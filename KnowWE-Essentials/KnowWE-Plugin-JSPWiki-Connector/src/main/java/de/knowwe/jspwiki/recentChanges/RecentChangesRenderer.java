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
import java.util.Arrays;
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

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.jspwiki.PageComparator;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

@SuppressWarnings("rawtypes")
public class RecentChangesRenderer extends DefaultMarkupRenderer {
	private static final RecentChangesUtils util = new RecentChangesUtils();

	@Override
	public void renderContentsAndAnnotations(Section<?> sec, UserContext user, RenderResult string) {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		Set<Page> recentChanges = wikiConnector.getPageManager().getRecentChanges();
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(sec, user);
		Set<Page> filteredRecentChanges = filter(filter, recentChanges);
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);
		PaginationRenderer.setResultSize(user, filteredRecentChanges.size());
		List<Page> sortedFilteredRecentChanges = sortPages(sec, user, filteredRecentChanges);
		string.appendHtml("<table>");
		addTableHead(string);
		int counter = 1;
		int pagesCount = startRow + count - 1;
		for (Page page : sortedFilteredRecentChanges) {
			if (counter > pagesCount || startRow > filteredRecentChanges.size()) {
				break;
			}
			if (counter < startRow) {
				counter++;
				continue;
			}
			String author = page.getAuthor();
			if (author == null) {
				author = "Unknown Author";
			}
			String formattedDate = util.toDateOrTodayTimeString(page.getLastModified());
			String changeNotes = page.getAttribute("changenote");
			if (changeNotes == null) {
				changeNotes = "-";
			}
			if (counter % 2 == 0) {
				string.appendHtml("<tr class='oddRow'>");
			}
			else {
				string.appendHtml("<tr>");
			}
			if (page instanceof Attachment attachment) {
				string.appendHtml("<td>");
				string.appendHtmlElement("a", attachment.getName(), "href", "Upload.jsp?page=" + Strings.encodeURL(attachment.getParentName()));
				string.appendHtml("</td>");
			}
			else {
				int pageVersion = page.getVersion();
				List<Page> versionHistory = wikiConnector.getPageManager().getVersionHistory(page.getName());
				int totalVersionCount = 1;
				for (Page p : versionHistory) {
					if (totalVersionCount < p.getVersion()) {
						totalVersionCount = p.getVersion();
					}
				}
				String label = page.getName();
				if (pageVersion != totalVersionCount) {
					label += " (Version " + pageVersion + "/" + totalVersionCount + ")";
				}
				string.appendHtml("<td>");
				string.appendHtmlElement("a", label, "href", KnowWEUtils.getURLLink(page.getName()));
				string.appendHtml("</td>");
			}
			string.appendHtml("<td class='column-Last-Modified'>").append(formattedDate).appendHtml("</td>");
			string.appendHtml("<td>").append(author).appendHtml("</td>");
			string.appendHtml("<td>").append(changeNotes).appendHtml("</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
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

	private String addTableHead(RenderResult string) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add(PAGE);
		columnNames.add(LAST_MODIFIED);
		columnNames.add(AUTHOR);
		columnNames.add(CHANGE_NOTES);
		string.appendHtml("<tr class='oddRow'>");
		for (String var : columnNames) {
			String varNoWhiteapace = var.replace(" ", "-");
			List<String> attributes = new ArrayList<>(Arrays.asList(
					"column-name", var, "filter-provider-action", RecentChangesFilterProviderAction.class.getSimpleName(), "class", "column-" + varNoWhiteapace));
			string.appendHtmlTag("th", attributes.toArray(new String[0]));
			string.append(var.replace("_", " "));
			string.appendHtml("</th>");
		}
		string.appendHtml("</tr>");
		return string.toString();
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
			List<Page> versionHistory = wikiConnector.getPageManager().getVersionHistory(page.getName());
			if (!filter.get(LAST_MODIFIED).isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithDate(versionHistory, filter.getOrDefault(LAST_MODIFIED, Set.of())));
			}
			if (!filter.get(AUTHOR).isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithString(versionHistory, filter.getOrDefault(AUTHOR, Set.of()), AUTHOR));
			}
			if (!filter.get(CHANGE_NOTES).isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithString(versionHistory, filter.getOrDefault(CHANGE_NOTES, Set.of()), CHANGE_NOTES));
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

