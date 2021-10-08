/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kdom.table;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * This is a renderer for the Table. It wraps the <code>Table</code> tag into an
 * own DIV and delegates the rendering of each <code>TableCellContent</code> to
 * its own renderer.
 *
 * @author smark
 */
public class TableRenderer implements Renderer {

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {

		boolean sortable = TableUtils.sortOption(sec);

		string.appendHtml(getOpeningTag(sec));

		if (sortable) {
			string.appendHtml("<div class=\"sortable scroll-parent\" style='overflow:auto;white-space:normal;'>");
		}
		else {
			string.appendHtml("<div class\"scroll-parent\" style='overflow:auto;white-space:normal;'>");
		}
		string.appendHtml("<table style='border:1px solid #999999;' id='table_" + sec.getID()
				+ "' class='wikitable knowwetable sticky-header' border='1'>");
		@NotNull List<Section<TableLine>> lines = $(sec).successor(TableLine.class).asList();
		if (!lines.isEmpty()) {
			string.appendHtml("<thead>");
			string.append(lines.get(0), user);
			string.appendHtml("</thead>");
		}
		string.appendHtml("<tbody>");
		for (Section<TableLine> line : lines.subList(1, lines.size())) {
			string.append(line, user);
		}
		string.appendHtml("</tbody></table>");
		string.appendHtml("</div>");

		string.appendHtml(getClosingTag());
	}

	protected String getOpeningTag(Section<?> sec) {
		return "<div class=\"table-edit\" id=\"" + sec.getID() + "\">";
	}

	protected String getClosingTag() {
		return "</div>";
	}
}
