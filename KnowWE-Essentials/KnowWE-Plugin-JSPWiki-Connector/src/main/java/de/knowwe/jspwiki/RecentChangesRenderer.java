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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;

public class RecentChangesRenderer extends DefaultMarkupRenderer {
	@Override
	public void renderContentsAndAnnotations(Section<?> sec, UserContext user, RenderResult string) {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		Set<Page> recentChanges = wikiConnector.getPageManager().getRecentChanges();
		Map<String, Set<Pattern>> filter = PaginationRenderer.getFilter(sec, user);
		Set<Page> filteredRecentChanges = filter(filter, recentChanges);
		PaginationRenderer.setResultSize(user, filteredRecentChanges.size());
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);

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
			String formattedDate = getFormattedDate(page);
			string.appendHtml("<tr>");
			string.appendHtml("<td>" + page.getName() + "</td>");
			string.appendHtml("<td>" + formattedDate + "</td>");
			string.appendHtml("<td>" + author + "</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
	}

	@NotNull
	private static String getFormattedDate(Page page) {
		LocalDate today = LocalDate.now();
		LocalDate date = page.getLastModified().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		SimpleDateFormat formatter;
		if (date.equals(today)) {
			formatter = new SimpleDateFormat("HH:mm:ss");
		}
		else {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return formatter.format(page.getLastModified());
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
					case "Last Modified" -> text = page.getLastModified().toString();
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
		}
		return filteredPages;
	}
}

