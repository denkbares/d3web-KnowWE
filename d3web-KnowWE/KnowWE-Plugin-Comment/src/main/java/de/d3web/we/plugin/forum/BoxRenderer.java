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

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLHead;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class BoxRenderer extends KnowWEDomRenderer {
	
	private static BoxRenderer instance = null;
	
	public static BoxRenderer getInstance() {
		if(instance == null) instance = new BoxRenderer();
		return instance;
	}

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		String name;
		String date;
		
		try {
		
			Map<String, String> boxMap = AbstractXMLObjectType.getAttributeMapFor(sec);
	
			name = boxMap.get("name");
			date = boxMap.get("date");
			
		} catch (NullPointerException n) {
			
			name = "System";
			date = "-"; 
		}
		
		Section contentSec = ForumBox.getInstance().getContentChild(sec);
		
		if (contentSec == null || contentSec.getOriginalText().length() < 1)
			return; // no empty posts

		if(name == null || date == null) {
		
			if (name == null) name = user.getUsername();	
			
			if (date == null) date = ForumRenderer.getDate();
			
			sec
					.findChildOfType(XMLHead.class)
					.findChildOfType(PlainText.class)
					.setOriginalText(
							"<box name=\"" + name + "\" date=\"" + date + "\">");

			//save article:
			try {
				
				StringBuilder buffi = new StringBuilder();
				String topic = sec.getTitle();
				String web = sec.getWeb();
				
				KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
				instance.getArticle(web, topic).getSection().collectTextsFromLeaves(buffi);
				KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.WEB, web);
				map.put(KnowWEAttributes.TOPIC, topic);
				map.put(KnowWEAttributes.USER, user.toString());
				instance.saveArticle(web, topic, buffi.toString(), map);
				
			} catch (Exception e) {
				
				// Do nothing if WikiEngine is not properly started yet
				
			}
		} 

		string.append(KnowWEUtils.maskHTML("<table class=wikitable width=95% border=0>\n"));
		string.append(KnowWEUtils.maskHTML("<tr><th align=left>" + name
				+ "</th><th align=right width=150>" + date + "</th></tr>\n"));
		string.append(KnowWEUtils.maskHTML("<tr><td colspan=2>"));
		
		DelegateRenderer.getInstance().render(article, contentSec, user, string);
		
		string.append(KnowWEUtils.maskHTML("</td></tr>\n</table>\n"));
	}

}
