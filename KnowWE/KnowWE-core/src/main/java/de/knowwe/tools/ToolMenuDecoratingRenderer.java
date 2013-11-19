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

import java.util.UUID;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * 
 * @author volker_belli
 * @created 30.11.2010
 */
public class ToolMenuDecoratingRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	public ToolMenuDecoratingRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		// prepare tools
		Tool[] tools = ToolUtils.getTools(sec, user);
		final boolean hasTools = tools != null && tools.length > 0;

		String headerID;

		if (hasTools) {
			headerID = "tool_menu_" + sec.getID() + "_" + UUID.randomUUID().toString();
			string.appendHtmlTag("span", "style", "position:relative;white-space: nowrap;");
			string.appendHtmlTag("span", "style", "position:absolute", "class",
					"toolsMenuDecorator", "id", headerID, "toolMenuIdentifier", sec.getID());
			string.appendHtmlTag("/span");
		}
		decoratedRenderer.render(sec, user, string);
		if (hasTools) {
			string.appendHtmlTag("/span");
		}
	}

	public static void renderText(String innerText, String toolMenuID, boolean hasTools, RenderResult string) {
		renderText(innerText, toolMenuID, "GetToolMenuAction", hasTools, string);
	}

	public static void renderText(String innerText, String toolMenuID, String toolMenuAction, boolean hasTools, RenderResult string) {
		String headerID;

		if (hasTools) {
			headerID = "tool_menu_" + toolMenuID + "_" + UUID.randomUUID().toString();
			string.appendHtmlTag("span", "style", "position:relative; white-space: nowrap;");
			string.appendHtmlTag("span", "style", "position:absolute", "class",
					"toolsMenuDecorator", "id", headerID,
					"toolMenuIdentifier", toolMenuID,
					"toolMenuAction", toolMenuAction);
			string.appendHtmlTag("/span");
		}
		string.append(innerText);
		if (hasTools) {
			string.appendHtmlTag("/span");
		}
	}
}
