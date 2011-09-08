/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.instantedit.tools;

import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolMenuDecoratingRenderer;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Adapted the {@link ToolMenuDecoratingRenderer} to support nested tool menus.
 * 
 * @author Stefan Mark
 * @created 20.06.2011
 * @param <T>
 */
public class ToolOuterMenuDecoratingRenderer<T extends Type> extends KnowWEDomRenderer<T> {

	private final KnowWEDomRenderer<?> decoratedRenderer;

	public ToolOuterMenuDecoratingRenderer(KnowWEDomRenderer<T> decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(KnowWEArticle article, Section sec, UserContext user, StringBuilder string) {

		// remove page locks by current user (user hits refresh button or f5 in
		// the browser)
		boolean pageLocked = KnowWEEnvironment.getInstance().getWikiConnector().isPageLockedCurrentUser(
				article.getTitle(), user.getUserName());

		if (pageLocked) {
			KnowWEEnvironment.getInstance().getWikiConnector().undoPageLocked(article.getTitle());
		}

		this.renderContent(article, sec, user, string);
	}

	/**
	 * 
	 * 
	 * @created 22.06.2011
	 * @param article
	 * @param sec
	 * @param user
	 * @param string
	 */
	private void renderContent(KnowWEArticle article, Section sec, UserContext user, StringBuilder string) {

		Tool[] tools = ToolUtils.getTools(article, sec, user);
		boolean hasTools = tools != null && tools.length > 0;

		String headerID = "" + sec.getID();
		Map<String, Map<String, List<Tool>>> toolMap = ToolUtils.groupTools(tools);

		if (hasTools) {
			StringBuilder controls = new StringBuilder();
			controls.append("<div id=\"" + headerID
					+ "\" class=\"nested-tool-menu\" style=\"display: inline-block;\">");
			string.append(KnowWEUtils.maskHTML(controls.toString()));
		}
		decoratedRenderer.render(article, sec, user, string);

		if (hasTools) {

			String menuHTML = DefaultMarkupRenderer.renderMenu(toolMap, sec.getID());
			menuHTML = menuHTML.replace("'", "\\'").replace("</div>", "</div>' + \n '");

			StringBuilder controls = new StringBuilder();
			controls.append("<div class=\"controls\" style=\"padding-left:20px;\">");
			controls.append("<img src=\"KnowWEExtension/d3web/icon/dropdown-opaque.png\" alt=\"DropDown arrow\" class=\"drop-down\"/>");
			controls.append("</div>");

			controls.append("<script>" +
							"var makeMenuFx = function() {\n" +
							"var a=$('" + headerID + "');\n" +
							"var img = $('" + headerID + "').getElements('img.drop-down')[0];\n" +
							"a.parentNode.onmouseover = function(e){\n" +
							"  if (!e) var e = window.event;\n" +
							"  e.cancelBubble = true;\n" +
							"  if (e.stopPropagation) e.stopPropagation();\n" +
							"  a.style.visibility='visible';\n" +
							"};\n" +
							"a.parentNode.onmouseout = function(e){" +
							// "  a.style.visibility='hidden';" +
					// TODO fix ugly hidden/ visible bug
					"  if (!e) var e = window.event;\n" +
							"  e.cancelBubble = true;\n" +
							"  if (e.stopPropagation) e.stopPropagation();\n" +
							"};\n" +
							"img.onclick = function(e){" +
							"  if (!e) var e = window.event;\n" +
							"  e.cancelBubble = true;\n" +
							"  if (e.stopPropagation) e.stopPropagation();\n" +
							"  showToolsPopupMenu(a, '" +
							menuHTML + "');\n" +
							"};\n" +
							"};\n" +
							"makeMenuFx();\n" +
							"</script>"
					);
			controls.append("</div>");

			string.append(KnowWEUtils.maskHTML(controls.toString()));
		}
	}
}
