/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.kdom.renderer;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Renders a marker as <b>div</b> around the section, so the content is automatically updated when answers are set.
 *
 * @author Johannes Dienst
 */
public class ReRenderSectionMarkerRenderer implements AsyncPreviewRenderer  {

	/**
	 * Holds the renderer of the inner text.
	 */
	private final Renderer renderer;

	public ReRenderSectionMarkerRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		boolean renderWrapper = requiresWrapper(user);
		if (renderWrapper) {
			KnowWEUtils.renderAnchor(sec, string);
			renderOpen(sec, string);
		}
		renderer.render(sec, user, string);
		if (renderWrapper) {
			renderClose(string);
		}
	}

	public static boolean requiresWrapper(UserContext user) {
		// we need the rerender wrapper-div if we are not using the user action or if we render asynchron
		return !(user instanceof UserActionContext) || "asynchronRenderer".equals(user.getParameter("reason"));
	}

	public static void renderOpen(Section<?> sec, RenderResult result) {
		renderOpen(sec.getID(), result);
	}

	public static void renderOpen(String secId, RenderResult result) {
		result.appendHtml("<div class='ReRenderSectionMarker' style='display: inline;' sectionId='")
				.append(secId).appendHtml("' rel='{id:\"").append(secId).appendHtml("\"}'>");
	}

	public static void renderClose(RenderResult result) {
		result.appendHtml("</div>");
	}

	@Override
	public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
		if (renderer instanceof AsyncPreviewRenderer) {
			((AsyncPreviewRenderer) renderer).renderAsyncPreview(section, user, result);
		}
	}

	@Override
	public boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
		return AsyncPreviewRenderer.shouldRenderAsynchronous(renderer, section, user);
	}
}
