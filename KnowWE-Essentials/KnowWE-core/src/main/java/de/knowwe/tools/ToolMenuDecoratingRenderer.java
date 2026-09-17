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
		ToolSet tools = ToolUtils.getTools(sec, user);
		if (tools.hasTools()) {
			// decorate in place, avoiding an intermediate buffer and copy of the rendered subtree
			appendToolMenuDecoratorStart(sec.getID(), null, string);
			decoratedRenderer.render(sec, user, string);
			appendToolMenuDecoratorEnd(string);
		}
		else {
			decoratedRenderer.render(sec, user, string);
		}
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
			appendToolMenuDecoratorStart(toolMenuID, toolMenuAction, string);
			string.append(innerText);
			appendToolMenuDecoratorEnd(string);
		}
		else {
			string.append(innerText);
		}
	}

	/**
	 * Opens the tool menu decoration. The decorated content and {@link #appendToolMenuDecoratorEnd(RenderResult)} must
	 * be appended afterward. Use this pair to decorate in place, without buffering the content first.
	 */
	public static void appendToolMenuDecoratorStart(String toolMenuID, String toolMenuAction, RenderResult string) {
		String headerID = UUID.randomUUID().toString();
		string.appendHtmlTag("span", "class", "toolMenuDecorated");
		// appendHtmlTag skips attributes with null values, covering the optional toolMenuAction
		string.appendHtmlTag("span",
				"class", "toolsMenuDecorator2",
				"id", headerID,
				"toolMenuIdentifier", toolMenuID,
				"toolMenuAction", toolMenuAction);
	}

	/**
	 * Closes the decoration opened by {@link #appendToolMenuDecoratorStart(String, String, RenderResult)}.
	 */
	public static void appendToolMenuDecoratorEnd(RenderResult string) {
		string.appendHtmlTag("/span");
		string.appendHtmlTag("/span");
	}
}
