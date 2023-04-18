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

package de.knowwe.jspwiki;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;

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

		string.appendHtml("<table>");
		addTableHead(string);
		int counter = 1;
		int pagesCount = startRow + count - 1;
		for (Page page : filteredRecentChanges) {
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
			string.appendHtml("<tr>");
			if (page instanceof Attachment) {
				string.appendHtml("<td>" + page.getName() + "</td>");
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
				if (pageVersion == totalVersionCount) {
					string.appendHtmlElement("td", "[" + page.getName() + "]");
				}
				else {
					string.appendHtmlElement("td", "[" + page.getName() + "Version " + pageVersion + " / " + totalVersionCount + " | " + page.getName() + "]");
				}
			}
			string.appendHtml("<td>" + formattedDate + "</td>");
			string.appendHtml("<td>" + author + "</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
	}

	private String addTableHead(RenderResult string) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("Page");
		columnNames.add("Last Modified");
		columnNames.add("Author");
		string.appendHtml("<tr>");
		for (String var : columnNames) {
			List<String> attributes = new ArrayList<>(Arrays.asList(
					"column-name", var, "filter-provider-action", RecentChangesFilterProviderAction.class.getSimpleName()));
			string.appendHtmlTag("th", attributes.toArray(new String[0]));
			string.append(var.replace("_", " "));
			string.appendHtml("</th>");
		}
		string.appendHtml("</tr>");
		return string.toString();
	}

	public Set<Page> filter(@NotNull Map<String, Set<Pattern>> filter, Set<Page> recentChanges) {
		Set<Page> filteredPages = new HashSet<>();
		if (filter.isEmpty()) return recentChanges;
		for (Page page : recentChanges) {
			boolean pageMatches = true;
			for (String columnName : filter.keySet()) {
				String text;
				switch (columnName) {
					case "Page" -> text = page.getName();
					case "Last Modified" -> {
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						text = formatter.format(page.getLastModified());
					}
					case "Author" -> {
						text = page.getAuthor();
						if (text == null) {
							text = "Unknown Author";
						}
					}
					default -> text = null;
				}
				Set<Pattern> patterns;
				try {
					patterns = filter.get(columnName);
				}
				catch (NullPointerException e) {
					continue;
				}
				if (patterns.size() < 1) {
					continue;
				}
				String finalText = text;
				if (patterns.stream().noneMatch(p -> p.matcher(finalText).matches())) {
					pageMatches = false;
					break;
				}
			}
			if (pageMatches) {
				filteredPages.add(page);
			}
			JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
			List<Page> versionHistory = wikiConnector.getPageManager().getVersionHistory(page.getName());
			if (!filter.get("Last Modified").isEmpty()) {
				filteredPages.addAll(checkVersionHistoryWithDate(versionHistory, filter.get("Last Modified")));
			}
			if (!filter.get("Author").isEmpty()) {
				try {
					filteredPages.addAll(checkVersionHistoryWithAuthor(versionHistory, filter.get("Author")));
				}
				catch (NullPointerException ignored) {
				}
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

	private List<Page> checkVersionHistoryWithAuthor(List<Page> versionHistory, Set<Pattern> patterns) {
		List<Page> filteredPages = new ArrayList<>();
		for (Page page : versionHistory) {
			if (patterns.stream().anyMatch(p -> p.matcher(page.getAuthor()).matches())) {
				filteredPages.add(page);
			}
		}
		return filteredPages;
	}
}

