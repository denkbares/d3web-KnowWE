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
import java.util.List;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.util.Icon;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

public class RecentChangesRenderer implements Renderer {

	private static final RecentChangesUtils util = new RecentChangesUtils();

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		if (!(user instanceof ActionContext)) {
			string.appendHtmlElement("table", "");
			string.appendHtmlTag("div");
			string.appendHtml(Icon.LOADING.addStyle("font-size: 2.5em").toHtml());
			string.appendHtmlTag("/div");
			return;
		}
		List<Page> sortedFilteredRecentChanges = new RecentChangesPaginationRenderer(this, PaginationRenderer.SortingMode.multi, true).getRecentChanges(sec, user);
		PaginationRenderer.setResultSize(user, sortedFilteredRecentChanges.size());
		string.appendHtml("<table>");
		addTableHead(string);
		int counter = 1;
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);
		int pagesCount = startRow + count - 1;
		for (Page page : sortedFilteredRecentChanges) {
			if (counter > pagesCount || startRow > sortedFilteredRecentChanges.size()) {
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
			if (changeNotes == null || "undefined".equals(changeNotes)) {
				changeNotes = "-";
			}
			if (counter % 2 == 0) {
				string.appendHtml("<tr class='odd-row'>");
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
				List<Page> versionHistory = getPageHistory(page);
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
				if (pageVersion != totalVersionCount) {
					string.appendHtmlElement("a", label, "href", KnowWEUtils.getURLLink(page.getName()) + "&version=" + page.getVersion());
				}
				else {
					string.appendHtmlElement("a", label, "href", KnowWEUtils.getURLLink(page.getName()));
				}
				string.appendHtml("</td>");
			}
			string.appendHtml("<td class='column-last-modified'>").append(formattedDate).appendHtml("</td>");
			string.appendHtml("<td>").append(author).appendHtml("</td>");
			string.appendHtml("<td>").append(changeNotes).appendHtml("</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
	}

	private static List<Page> getPageHistory(Page page) {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		return wikiConnector.getPageManager().getVersionHistory(page.getName());
	}

	private String addTableHead(RenderResult string) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add(PAGE);
		columnNames.add(LAST_MODIFIED);
		columnNames.add(AUTHOR);
		columnNames.add(CHANGE_NOTES);
		string.appendHtml("<tr class='odd-row'>");
		for (String columnName : columnNames) {
			String varNoWhitespace = columnName.replace(" ", "-");
			List<String> attributes = new ArrayList<>(Arrays.asList(
					"column-name", columnName, "filter-provider-action", RecentChangesFilterProviderAction.class.getSimpleName(), "class", "column-" + varNoWhitespace));
			string.appendHtmlTag("th", attributes.toArray(new String[0]));
			string.append(columnName);
			string.appendHtml("</th>");
		}
		string.appendHtml("</tr>");
		return string.toString();
	}
}

