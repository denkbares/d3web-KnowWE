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
package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Johannes Dienst
 * 
 *         Renders a marker as <b>div</b> around the CoveringList, so the
 *         content is automatically updated when answers are set.
 * 
 */
public class ReRenderSectionMarkerRenderer<T extends KnowWEObjectType> extends KnowWEDomRenderer<T> {

	/**
	 * Holds the renderer of the inner text.
	 */
	private final KnowWEDomRenderer<T> renderer;

	public ReRenderSectionMarkerRenderer(KnowWEDomRenderer<T> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(KnowWEArticle article, Section<T> sec,
			KnowWEUserContext user, StringBuilder string) {
		Boolean ajaxAction = user.getUrlParameterMap().containsKey("action");
		if (!ajaxAction) {
			string.append(KnowWEUtils
					.maskHTML("<div class=\"ReRenderSectionMarker\" rel=\"{id:'" + sec.getID()
							+ "'}\">"));
		}
		renderer.render(article, sec, user, string);
		if (!ajaxAction) {
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
	}

}
