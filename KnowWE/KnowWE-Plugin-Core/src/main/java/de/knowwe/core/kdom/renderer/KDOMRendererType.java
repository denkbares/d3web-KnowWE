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
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
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
		protected void renderContents(Section<?> section, UserContext user, StringBuilder string) {
			String html = "";
			html += "<div class='zebra-table'>";
			html += "<table class='renderKDOMTable wikitable' id='tree'>";
			html += "<th>Type</th>";
			html += "<th>ID</th>";
			html += "<th>Length</th>";
			html += "<th>Offset</th>";
			html += "<th>Children</th>";
			html += "<th>Text</th>";
			string.append(Strings.maskHTML(html));
			StringBuilder temp = new StringBuilder();
			renderSubtree(section.getArticle().getRootSection(), temp, 1);
			string.append(temp);
			string.append(Strings.maskHTML("</table></div>"));

			string.append(Strings.maskHTML("<script type='text/javascript'>jq$('#tree').treeTable();</script>"));

		}

		protected void renderSubtree(Section<?> s, StringBuilder string, int count) {
			string.append(Strings.maskHTML("<tr id='" + s.getID() + "'"));
			string.append(" class='");
			if (s.getFather() != null) {
				string.append(" child-of-" + s.getFather().getID());
			}
			if (count % 2 != 0) {
				string.append(" odd' ");
			}
			else {
				string.append("'");
			}
			string.append(Strings.maskHTML(">"));
			string.append(Strings.maskHTML("<td> " + s.get().getClass().getSimpleName() + "</td>"));
			string.append(Strings.maskHTML("<td> " + s.getID() + "</td>"));
			string.append(Strings.maskHTML("<td> ") + s.getText().length()
					+ Strings.maskHTML("</td>"));
			string.append(Strings.maskHTML("<td> " + s.getOffSetFromFatherText() + "</td>"));
			string.append(Strings.maskHTML("<td> " + s.getChildren().size() + "</td>"));
			String text = s.getText().length() < 50 ? s.getText() : s.getText().substring(0,
					50) + "...";
			string.append(Strings.maskHTML("<td><div class='table_text'> ")
					+ Strings.maskJSPWikiMarkup(text)
					+ Strings.maskHTML("</div></td>"));
			string.append(Strings.maskHTML("</tr>"));
			if (s.getChildren().size() > 0) {
				for (Section<?> child : s.getChildren()) {
					renderSubtree(child, string, (count + 1));
				}
			}
		}
	}
}
