/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Adds a tool menu to the element rendered by the given delegate renderer.
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
		ToolSet tools = ToolUtils.getTools(sec, user);
		final boolean hasTools = tools.hasTools();

		RenderResult subResult = new RenderResult(string);
		decoratedRenderer.render(sec, user, subResult);
		renderToolMenuDecorator(subResult, sec.getID(), null, hasTools, string);
	}

	public Renderer getDecoratedRenderer() {
		return decoratedRenderer;
	}

	public static void renderToolMenuDecorator(String innerText, String toolMenuID, boolean hasTools, RenderResult string) {
		RenderResult innerTextResult = new RenderResult(string);
		innerTextResult.append(innerText);
		renderToolMenuDecorator(innerTextResult, toolMenuID, null, hasTools, string);
	}

	public static void renderToolMenuDecorator(RenderResult innerText, String toolMenuID, boolean hasTools, RenderResult string) {
		renderToolMenuDecorator(innerText, toolMenuID, null, hasTools, string);
	}

	public static void renderToolMenuDecorator(String innerText, String toolMenuID, String toolMenuAction, boolean hasTools, RenderResult string) {
		RenderResult innerTextResult = new RenderResult(string);
		innerTextResult.append(innerText);
		renderToolMenuDecorator(innerTextResult, toolMenuID, toolMenuAction, hasTools, string);
	}

	public static void renderToolMenuDecorator(RenderResult innerText, String toolMenuID, String toolMenuAction, boolean hasTools, RenderResult string) {

		if (hasTools) {
			String headerID = UUID.randomUUID().toString();
			string.appendHtmlTag("span", "class", "toolMenuDecorated");

			List<String> attributes = new ArrayList<>();
			attributes.add("class");
			attributes.add("toolsMenuDecorator2");
			attributes.add("id");
			attributes.add(headerID);
			attributes.add("toolMenuIdentifier");
			attributes.add(toolMenuID);
			if (toolMenuAction != null) {
				attributes.add("toolMenuAction");
				attributes.add(toolMenuAction);
			}
			string.appendHtmlTag("span", attributes.toArray(new String[0]));
			string.append(innerText);
			string.appendHtmlTag("/span");
		}
		if (hasTools) {
			string.appendHtmlTag("/span");
		}
	}
}
