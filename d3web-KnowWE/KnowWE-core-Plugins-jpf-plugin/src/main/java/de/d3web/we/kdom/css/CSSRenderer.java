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

package de.d3web.we.kdom.css;

import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>Renderer for the <code>CSS</code> KDOM element.</p>
 * <p>Renders a given <code>CSS</code> tag in the wiki article page to a <code>span
 * </code> tag and applies the given style information to it.</p>
 * 
 * @author smark
 * @see KnowWEDomRenderer
 */
public class CSSRenderer extends KnowWEDomRenderer<AbstractXMLObjectType> {

	@Override
	public void render(KnowWEArticle article, Section<AbstractXMLObjectType> sec, KnowWEUserContext user, StringBuilder string) {
		Map<String, String> mapFor = AbstractXMLObjectType.getAttributeMapFor(sec);
		String style = mapFor.get("style");
		
		StringBuilder b = new StringBuilder();
		List<Section<PlainText>> children = sec.findChildrenOfType(PlainText.class);
		//should only be one
		for (Section<PlainText> section :  children) {
				DelegateRenderer.getInstance().render(article, section, user, b);
		}
		string.append(wrapWithCSS(b.toString(), style));
	}
	
	
	/**
	 * <p>Wraps the content of the section into a span. The span has a style
	 * attribute with the specified CSS styles.</p>
	 * 
	 * @param content
	 * @param style
	 * @return
	 */
	private String wrapWithCSS(String content, String style) {
		StringBuilder result = new StringBuilder();
		result.append("<span style='" + style + "'>");
		result.append(content);
		result.append("</span>");	
		return KnowWEUtils.maskHTML( result.toString() );
	}
}
