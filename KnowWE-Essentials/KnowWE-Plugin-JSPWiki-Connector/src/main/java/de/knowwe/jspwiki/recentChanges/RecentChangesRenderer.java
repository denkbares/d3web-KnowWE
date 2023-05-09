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
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;

import static de.knowwe.jspwiki.recentChanges.RecentChangesUtils.*;

public class RecentChangesRenderer extends DefaultMarkupRenderer {

	private static final RecentChangesUtils util = new RecentChangesUtils();

	@Override
	public void renderContentsAndAnnotations(Section<?> sec, UserContext user, RenderResult string) {
		JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		List<Page> sortedFilteredRecentChanges = new RecentChangesPaginationRenderer(new RecentChangesRenderer(), PaginationRenderer.SortingMode.multi, true).getRecentChanges(sec, user);
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);
		PaginationRenderer.setResultSize(user, sortedFilteredRecentChanges.size());
		string.appendHtml("<table>");
		addTableHead(string);
		int counter = 1;
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
			string.appendHtml("<td class='column-last-modified'>").append(formattedDate).appendHtml("</td>");
			string.appendHtml("<td>").append(author).appendHtml("</td>");
			string.appendHtml("<td>").append(changeNotes).appendHtml("</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
	}

	private String addTableHead(RenderResult string) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add(PAGE);
		columnNames.add(LAST_MODIFIED);
		columnNames.add(AUTHOR);
		columnNames.add(CHANGE_NOTES);
		string.appendHtml("<tr class='odd-row'>");
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
}

