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
package de.d3web.we.tools;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author volker_belli
 * @created 30.11.2010
 */
public class ToolMenuDecoratingRenderer<T extends KnowWEObjectType> extends KnowWEDomRenderer<T> {

	private final KnowWEDomRenderer<?> decoratedRenderer;

	public ToolMenuDecoratingRenderer(KnowWEDomRenderer<T> decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		// prepare tools
		Tool[] tools = ToolUtils.getTools(article, sec, user);
		boolean hasTools = tools != null && tools.length > 0;
		String headerID = "header_" + sec.getID();

		if (hasTools) {
			string.append(KnowWEUtils.maskHTML("<span " +
					"style='position:relative;'" +
					">"));
			string.append(KnowWEUtils.maskHTML("<div " +
					"style='position:absolute;' " +
					"class='toolsMenuDecorator' " +
					"id='" + headerID + "' " +
					">" +
					"</div>"));
		}
		decoratedRenderer.render(article, sec, user, string);
		if (hasTools) {
			string.append(KnowWEUtils.maskHTML("</span>"));
			String menuHTML = DefaultMarkupRenderer.renderMenu(tools, sec.getID());
			string.append(KnowWEUtils.maskHTML(
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
							menuHTML.replace("'", "\\'") + "');" +
							"};" +
							"};" +
							"makeMenuFx();" +
							"</script>"
					));
		}
	}
}
