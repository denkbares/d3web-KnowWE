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

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.rightpanel.RightPanel;
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
		// we need the rerender wrapper-div if we are not using the user action or if we render asynchron, or the
		// content of a lazy tab of the right panel, which is new on the page like the result of an asynchronous rendering
		return !(user instanceof UserActionContext) || AsynchronousRenderer.isAsynchronousRerenderingRequest(user)
				|| RightPanel.isTabContentRequest(user);
	}

	public static void renderOpen(Section<?> sec, RenderResult result) {
		renderOpen(sec.getID(), AbstractAction.getLocalSectionStorageKey(sec), result);
	}

	private static void renderOpen(String secId, String storageKey, RenderResult result) {
		result.appendHtml("<div class='ReRenderSectionMarker' style='display: inline;' sectionId='")
				.append(secId).appendHtml("' rel='{id:\"").append(secId).appendHtml("\"}' ")
				.appendHtml(AbstractAction.LOCAL_SECTION_STORAGE_KEY_ATTRIBUTE).appendHtml("='")
				.appendHtml(Strings.encodeHtml(storageKey)).appendHtml("'>");
	}

	/**
	 * Creates a rerender marker containing the supplied elements.
	 *
	 * @param environment the environment the section belongs to
	 * @param sectionId   stable ID of the section to rerender
	 * @param children    marker content
	 * @return element representing the complete rerender marker
	 */
	public static HtmlElement createMarker(@NotNull Environment environment, String sectionId, HtmlProvider... children) {
		return new Div()
				.clazz("ReRenderSectionMarker")
				.style("display: inline;")
				.attributes("sectionId", sectionId, "rel", "{id:\"" + sectionId + "\"}",
						AbstractAction.LOCAL_SECTION_STORAGE_KEY_ATTRIBUTE, AbstractAction.getLocalSectionStorageKey(environment, sectionId))
				.children(children);
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
