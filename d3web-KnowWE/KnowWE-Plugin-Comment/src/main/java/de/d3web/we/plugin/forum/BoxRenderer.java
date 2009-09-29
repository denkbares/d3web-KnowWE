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

package de.d3web.we.plugin.forum;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLHead;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class BoxRenderer extends KnowWEDomRenderer {
	
	private static BoxRenderer instance = null;
	
	public static BoxRenderer getInstance() {
		if(instance == null) instance = new BoxRenderer();
		return instance;
	}
	
	private static String maskHTML(String s) {
		return KnowWEEnvironment.maskHTML(s);
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		StringBuffer box = new StringBuffer();

		Map<String, String> boxMap = AbstractXMLObjectType
				.getAttributeMapFor(sec);

		Section contentSec = ForumBox.getInstance().getContentChild(sec);

		if (contentSec == null || contentSec.getOriginalText().length() < 1)
			return; // no empty posts

		//String content = contentSec.getOriginalText();
		
		String name = boxMap.get("name");
		String date = boxMap.get("date");

		if (name == null && date == null) {
			name = user.getUsername();
			date = ForumRenderer.getDate();

			ForumRenderer.change = true;
			sec
					.findChildOfType(XMLHead.class)
					.findChildOfType(PlainText.class)
					.setOriginalText(
							"<box name=\"" + name + "\" date=\"" + date + "\">");

		} else if (name == null) {
			name = user.getUsername();

			ForumRenderer.change = true;
			sec
					.findChildOfType(XMLHead.class)
					.findChildOfType(PlainText.class)
					.setOriginalText(
							"<box name=\"" + name + "\" date=\"" + date + "\">");

		} else if (date == null) {
			date = ForumRenderer.getDate();

			ForumRenderer.change = true;
			sec
					.findChildOfType(XMLHead.class)
					.findChildOfType(PlainText.class)
					.setOriginalText(
							"<box name=\"" + name + "\" date=\"" + date + "\">");

		}

		box.append(maskHTML("<table class=wikitable width=95% border=0>\n"));
		box.append(maskHTML("<tr><th align=left>" + name
				+ "</th><th align=right width=150>" + date + "</th></tr>\n"));
		box.append(maskHTML("<tr><td colspan=2>"));
		
		string.append(box);
		DelegateRenderer.getInstance().render(contentSec, user, string);
		string.append(maskHTML("</td></tr>\n</table>\n"));
	}

}
