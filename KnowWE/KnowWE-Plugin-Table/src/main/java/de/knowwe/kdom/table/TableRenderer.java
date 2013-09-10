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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

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
			string.appendHtml("<div class=\"sortable\" style='overflow:auto;white-space:normal;'>");
		}
		else {
			string.appendHtml("<div style='overflow:auto;white-space:normal;'>");
		}
		string.appendHtml("<table style='border:1px solid #999999;' id='table_" + sec.getID()
				+ "' class='wikitable knowwetable' border='1'><tbody>");
		string.appendHtml(getHeader());
		DelegateRenderer.getInstance().render(sec, user, string);
		string.appendHtml("</tbody></table>");
		string.appendHtml("</div>");

		string.appendHtml(getClosingTag());

	}

	protected String getHeader() {
		return "";
	}

	protected String getOpeningTag(Section<?> sec) {
		return "<div class=\"table-edit\" id=\"" + sec.getID() + "\">";
	}

	protected String getClosingTag() {
		return "</div>";
	}

}
