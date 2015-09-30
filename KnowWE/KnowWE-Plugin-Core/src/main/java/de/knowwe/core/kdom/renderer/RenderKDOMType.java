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

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * @author danielzugner
 * @created Nov 5, 2012
 */
public class RenderKDOMType extends DefaultMarkupType {

	/**
	 * @param markup
	 */
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("RenderKDOM");
	}

	public RenderKDOMType() {
		super(MARKUP);
		setRenderer(new KDOMRenderer());
	}

	private class KDOMRenderer extends DefaultMarkupRenderer {

		@Override
		protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
			String html = "";
			html += "<table class='renderKDOMTable wikitable' article='" + section.getTitle() + "'>";
			html += "<tr>";
			html += "<th>Type</th>";
			html += "<th>ID</th>";
			html += "<th>Length</th>";
			html += "<th>Offset</th>";
			html += "<th>Children</th>";
			html += "<th>Text</th>";
			html += "</tr>";

			string.appendHtml(html);
			renderSubtree(section.getArticle().getRootSection(), string, 1);

			string.appendHtml("</table>");
		}

		protected void renderSubtree(Section<?> s, RenderResult string, int count) {
			string.appendHtml("<tr data-tt-id='kdom-row-" + s.getID()
					+ "'");
			if (s.getParent() != null) {
				string.append(" data-tt-parent-id='kdom-row-" + s.getParent().getID() + "'");
			}
			string.append(" class='treetr");
			if (s.getParent() != null) {
				string.append(" child-of-kdom-row-" + s.getParent().getID());
			}
			if (count % 2 != 0) {
				string.append(" odd' ");
			}
			else {
				string.append("'");
			}
			string.appendHtml(">");
			String typeName = s.get().getClass().getSimpleName();
			if (Strings.isBlank(typeName)) {
				typeName = s.get().getClass().getName();
				typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
			}
			string.appendHtml("<td>" + typeName + "</td>");
			string.appendHtml("<td>" + s.getID() + "</td>");
			string.appendHtml("<td>" + s.getText().length() + "</td>");
			string.appendHtml("<td>" + s.getOffsetInParent() + "</td>");
			string.appendHtml("<td>" + s.getChildren().size() + "</td>");

			string.appendHtml("<td><div class='table_text'><div class='kdom_source'>");
			string.append(Strings.encodeHtml(s.getText()) + "&#8203;");
			string.appendHtml("</div></div></td>");
			string.appendHtml("</tr>");
			if (s.getChildren().size() > 0) {
				for (Section<?> child : s.getChildren()) {
					renderSubtree(child, string, (count + 1));
				}
			}
		}
	}

}
