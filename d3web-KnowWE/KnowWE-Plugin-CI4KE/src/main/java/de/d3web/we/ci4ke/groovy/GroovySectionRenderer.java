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

package de.d3web.we.ci4ke.groovy;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class GroovySectionRenderer extends KnowWEDomRenderer<GroovyCITestType> {

	@Override
	public void render(KnowWEArticle article, Section<GroovyCITestType> sec,
			KnowWEUserContext user, StringBuilder string) {

		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/shCore.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/shThemeDefault.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);

		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/shCore.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/shBrushGroovy.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("SyntaxHighlighter.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

		string.append(KnowWEUtils.maskHTML("<div id=\"" + sec.getId() + "\">\n"));
		string.append("{{{\n");
		
//		if (!user.getUrlParameterMap().containsKey("action")) { 
//			string.append("{{{");
//		}
		
		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(sec);
		string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));
		
		string.append(KnowWEUtils.maskHTML("<h3>"));
		string.append(KnowWEUtils.maskHTML(DefaultMarkupType.getAnnotation(sec,"name")));
		string.append(KnowWEUtils.maskHTML("</h3>"));

		DefaultMarkupRenderer.renderMessages(article, sec, string);
//		DelegateRenderer.getInstance().render(article, section, user, string);
		
		// string.append(KnowWEUtils.maskHTML("<span style=\"font-size:1.3em;\">"));
		string.append(KnowWEUtils.maskHTML("<span>"));
		string.append(KnowWEUtils.maskHTML("<script type=\"syntaxhighlighter\" class=\"brush: groovy\"><![CDATA["));

		// string.append(KnowWEUtils.maskNewline(sec.getOriginalText()));
		string.append(sec.getOriginalText());

		string.append(KnowWEUtils.maskHTML("]]></script>\n"));
		string.append(KnowWEUtils.maskHTML("</span>\n"));

		// and close the box
		string.append("}}}\n");
		string.append(KnowWEUtils.maskHTML("</div>\n"));		
	}

}
