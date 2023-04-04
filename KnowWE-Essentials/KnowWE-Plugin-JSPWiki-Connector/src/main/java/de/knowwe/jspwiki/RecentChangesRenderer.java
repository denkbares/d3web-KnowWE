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
import java.util.Set;

import org.apache.wiki.api.core.Page;

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
		PaginationRenderer.setResultSize(user, recentChanges.size());
		int startRow = PaginationRenderer.getStartRow(sec, user);
		int count = PaginationRenderer.getCount(sec, user);

		string.appendHtml("<table>");
		string.appendHtml("<tr>");
		string.appendHtml("<th>Page</th>");
		string.appendHtml("<th>Last Modified</th>");
		string.appendHtml("<th>Author</th>");
		string.appendHtml("</tr>");

		int counter = 1;
		int pagesCount = startRow + count - 1;
		for (Page page : recentChanges) {
			if (counter > pagesCount) {
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
			LocalDate today = LocalDate.now();
			LocalDate date = page.getLastModified().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
			SimpleDateFormat formatter;
			if (date.equals(today)) {
				formatter = new SimpleDateFormat("HH:mm:ss");
			}
			else {
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}

			String formattedDate = formatter.format(page.getLastModified());
			string.appendHtml("<tr>");
			string.appendHtml("<td>" + page.getName() + "</td>");
			string.appendHtml("<td>" + formattedDate + "</td>");
			string.appendHtml("<td>" + author + "</td>");
			string.appendHtml("</tr>");
			counter++;
		}
		string.appendHtml("</table>");
	}
}

