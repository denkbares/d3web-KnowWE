/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.core.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author danielzugner
 * @created Nov 5, 2012
 */
public class KDOMRendererType extends DefaultMarkupType {

	/**
	 * @param markup
	 */
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("RenderKDOM");
	}

	public KDOMRendererType() {
		super(MARKUP);
		setRenderer(new KDOMRender());
		setIgnorePackageCompile(true);

	}

	private class KDOMRender extends DefaultMarkupRenderer {

		@Override
		protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
			String html = "";
			// html += "<div class='zebra-table'>";
			html += "<table class='renderKDOMTable wikitable' id='tree'>";
			html += "<th>Type</th>";
			html += "<th>ID</th>";
			html += "<th>Length</th>";
			html += "<th>Offset</th>";
			html += "<th>Children</th>";
			html += "<th>Text</th>";
			string.appendHTML(html);
			renderSubtree(section.getArticle().getRootSection(), string, 1);

			string.appendHTML("</table>"/* </div>" */);

			string.appendHTML("<script type='text/javascript'>jq$('#tree').treeTable();</script>");

		}

		protected void renderSubtree(Section<?> s, RenderResult string, int count) {
			string.appendHTML("<tr id='" + s.getID()
					+ "'");
			string.append(" class='treetr");
			if (s.getFather() != null) {
				string.append(" child-of-" + s.getFather().getID());
			}
			if (count % 2 != 0) {
				string.append(" odd' ");
			}
			else {
				string.append("'");
			}
			string.appendHTML(">");
			string.appendHTML("<td>" + s.get().getClass().getSimpleName() + "</td>");
			string.appendHTML("<td>" + s.getID() + "</td>");
			string.appendHTML("<td>" + s.getText().length() + "</td>");
			string.appendHTML("<td>" + s.getOffSetFromFatherText() + "</td>");
			string.appendHTML("<td>" + s.getChildren().size() + "</td>");

			string.appendHTML("<td><div class='table_text'><div>");
			string.append("~|");
			string.appendJSPWikiMarkup(s.getText());
			string.append("~|");
			string.appendHTML("</div></div></td>");
			string.appendHTML("</tr>");
			if (s.getChildren().size() > 0) {
				for (Section<?> child : s.getChildren()) {
					renderSubtree(child, string, (count + 1));
				}
			}
		}
	}
}
