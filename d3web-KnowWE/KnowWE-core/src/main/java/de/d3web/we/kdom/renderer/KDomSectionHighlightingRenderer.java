/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This renderer adds the kdomid to the span in which a
 * Section is rendered. Use it to enable highlighting.
 * It Uses the SpecialDelegateRender.
 * 
 * Note: If you need more functionality then just highlighting a section.
 * For Example this section has some background before or style information
 * You have to write your own Renderer.
 * @see "highlightingNode"-method in KnowWE.js. There you can read how to use this
 * renderer!
 * 
 * @author Johannes Dienst
 */
public class KDomSectionHighlightingRenderer extends KnowWEDomRenderer {

	private static KDomSectionHighlightingRenderer instance;

	public static synchronized KDomSectionHighlightingRenderer getInstance() {
		if (instance == null)
			instance = new KDomSectionHighlightingRenderer();
		return instance;
	}

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {	
		// First span is for kdom id. Second for an uniqueMarker
		// that is needed for Highlighting the section
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(article, sec, user, b);
		string.append(KnowWEUtils.maskHTML("<span id='"+sec.getId()
						+ "'><span id=''>"
						+ b.toString()
						+ "</span></span>"));
	}
}
