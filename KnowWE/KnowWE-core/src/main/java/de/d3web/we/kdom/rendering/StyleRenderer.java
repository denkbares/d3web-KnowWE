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

package de.d3web.we.kdom.rendering;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StyleRenderer extends KnowWEDomRenderer {

	private final String cssClass;
	private final String cssStyle;

	public StyleRenderer(String cssStyle) {
		this(null, cssStyle);
	}

	public StyleRenderer(String cssClass, String cssStyle) {
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
	}

	@Override
	public void render(KnowWEArticle article, Section section, KnowWEUserContext user, StringBuilder string) {
		string.append(KnowWEUtils.maskHTML("<span"));
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}
		string.append(KnowWEUtils.maskHTML(">"));
		renderContent(article, section, user, string);
		string.append(KnowWEUtils.maskHTML("</span>"));
	}

	/**
	 * Renders the content that will automatically be styled in the correct way.
	 * You may overwrite it for special purposes.
	 *
	 * @created 06.10.2010
	 * @param article the article to render for
	 * @param section the section to be rendered
	 * @param user the user to render for
	 * @param string the buffer to render into
	 */
	protected void renderContent(KnowWEArticle article, Section section, KnowWEUserContext user, StringBuilder string) {
		DelegateRenderer.getInstance().render(article, section, user, string);
	}

	public String getCssStyle() {
		return this.cssStyle;
	}

	public String getCssClass() {
		return this.cssClass;
	}

}
