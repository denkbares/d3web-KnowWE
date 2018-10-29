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

import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
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

	private static final String ANNOTATION_HIDE_BLANK = "hideBlank";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("RenderKDOM");
		MARKUP.addAnnotation(ANNOTATION_HIDE_BLANK, false, "yes", "no", "plain");
		MARKUP.setTemplate("%%RenderKDOM @hideBlank: plain\n");
	}

	public RenderKDOMType() {
		super(MARKUP);
		setRenderer(new KDOMRenderer());
	}

	/**
	 * Returns a section filter that only accepts the sections that should be displayed by the specified KDOM renderer
	 * markuo.
	 */
	public static Predicate<Section<?>> getSectionFilter(Section<RenderKDOMType> section) {
		String hide = getAnnotation(section, ANNOTATION_HIDE_BLANK);
		if (hide == null || "no".equalsIgnoreCase(hide)) {
			return s -> true;
		}

		Pattern blank = Pattern.compile("[\\h\\s\\v]*");
		Predicate<Section<?>> nonBlank = s -> !blank.matcher(s.getText()).matches();
		if ("plain".equalsIgnoreCase(hide)) {
			Predicate<Section<?>> nonPlain = s -> !(s.get() instanceof PlainText);
			return nonPlain.or(nonBlank);
		}
		return nonBlank;
	}

	private static class KDOMRenderer extends DefaultMarkupRenderer {

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
			html += "<th>Renderer</th>";
			html += "<th>Text</th>";
			html += "</tr>";

			// prepare filter predicate for the displayed sections
			Predicate<Section<?>> filter = getSectionFilter(Sections.cast(section, RenderKDOMType.class));

			// and render the subtree with the filter
			string.appendHtml(html);
			renderSubtree(section.getArticle().getRootSection(), string, filter);

			string.appendHtml("</table>");
		}

		private void renderSubtree(Section<?> s, RenderResult string, Predicate<Section<?>> filter) {
			if (!filter.test(s)) return;
			string.appendHtml("<tr data-tt-id='kdom-row-" + s.getID() + "'");
			if (s.getParent() != null) {
				string.append(" data-tt-parent-id='kdom-row-" + s.getParent().getID() + "'");
			}
			string.append(" class='treetr");
			if (s.getParent() != null) {
				string.append(" child-of-kdom-row-" + s.getParent().getID());
			}
			string.append("'").appendHtml(">");
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
			Class renderer = s.get().getRenderer().getClass();
			String rendererEntry = (renderer.equals(DelegateRenderer.class))?"":renderer.getSimpleName();
			string.appendHtml("<td>" + rendererEntry + "</td>");

			string.appendHtml("<td><div class='table_text'><div class='kdom_source'>");
			string.append(Strings.encodeHtml(s.getText()) + "&#8203;");
			string.appendHtml("</div></div></td>");
			string.appendHtml("</tr>");
			if (!s.getChildren().isEmpty()) {
				for (Section<?> child : s.getChildren()) {
					renderSubtree(child, string, filter);
				}
			}
		}
	}
}
