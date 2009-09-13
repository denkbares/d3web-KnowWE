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

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Adds functionality to render a specific Background Color.
 * Used especially in Rendering XCLRelations.
 * Needed because RelationWeight shouldnt be highlighted with Relation.
 * 
 * Note 1: Adds an id in the span. So Highlighting the text is still possible.
 * KDomSectionHighlightingRenderer offers an id too. Consider using this one.
 * If you want correct highlighting look up "highlightingNode"-method in KnowWE.js.
 * There you can specify in which depth you can find the id tag for the marker.
 * For Example for XCLRelations it is the 3. span.
 * 
 * Note 2: Can be used instead of FontColorRenderer. Just set the background
 * argument to null.
 * 
 * @author Johannes Dienst
 */
public class FontColorBackgroundRenderer extends StyleRenderer {
	String background = "";
	
	/**
	 * Use static Method getRenderer() instead!
	 * 
	 * @param s
	 */	
	public FontColorBackgroundRenderer(String color, String background) {
		super(color);
		this.background = background;
	}
	
	/**
	 * When normal functionality as in FontColorRenderer: Set background null;
	 * 
	 * @param color
	 * @return
	 */
	public static FontColorBackgroundRenderer getRenderer(String color, String background) {
		return new FontColorBackgroundRenderer(color, background);
	}
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(sec, user, b);
		
		if (this.background != null) {
			string.append(KnowWEEnvironment.maskHTML(
							"<span style='"+this.getStyle()
							+ ";background-color:"
							+ this.background
							+ "'><span id=''>"
							+ b.toString()
							+"</span></span>"));
			return;
		}
		
		string.append(KnowWEEnvironment.maskHTML(
						"<span style='"
						+ this.getStyle()
						+ "'><span id=''>"
						+ b.toString()
						+ "</span></span>"));
	}
}
