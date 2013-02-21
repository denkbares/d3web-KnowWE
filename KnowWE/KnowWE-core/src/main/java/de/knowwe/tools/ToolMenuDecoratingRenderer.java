/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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
package de.knowwe.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * 
 * @author volker_belli
 * @created 30.11.2010
 */
public class ToolMenuDecoratingRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	private static DefaultMarkupRenderer defaultMarkupRenderer =
			new DefaultMarkupRenderer();

	public ToolMenuDecoratingRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		// prepare tools
		Tool[] tools = ToolUtils.getTools(sec, user);
		boolean hasTools = tools != null && tools.length > 0;

		String headerID = "header_" + sec.getID();

		if (hasTools) {
			string.appendHtml("<span " +
					"style='position:relative;'" +
					">");
			string.appendHtml("<div " +
					"style='position:absolute;' " +
					"class='toolsMenuDecorator' " +
					"id='" + headerID + "' " +
					">" +
					"</div>");
		}
		decoratedRenderer.render(sec, user, string);
		if (hasTools) {
			string.appendHtml("</span>");
			RenderResult menuHTMLResult = new RenderResult(string);
			defaultMarkupRenderer.appendMenu(tools, sec.getID(), user, menuHTMLResult);
			String menuHTML = menuHTMLResult.toStringRaw();
			menuHTML = menuHTML.replace("'", "\\'").replace("</div>", "</div>' + \n '");
			string.appendHtml(
					"<script>" +
							"var makeMenuFx = function() {" +
							"var a=$('" + headerID + "');" +
							"a.parentNode.onmouseover = function(){" +
							"  a.style.visibility='visible';" +
							"};" +
							"a.parentNode.onmouseout = function(){" +
							"  a.style.visibility='hidden';" +
							"};" +
							"a.onclick = function(){" +
							// "  requestToolsPopupMenu(a, '" + sec.getID() +
							// "');" +
							"  showToolsPopupMenu(a, '" +
							menuHTML + "');" +
							"};" +
							"};" +
							"makeMenuFx();" +
							"</script>"
					);
		}
	}
}
