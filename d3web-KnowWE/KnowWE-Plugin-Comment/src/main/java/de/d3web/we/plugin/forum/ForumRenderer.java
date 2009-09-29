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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ForumRenderer extends KnowWEDomRenderer {
	
	static boolean change = false;
	private static boolean firstStart = true;
	private static boolean sortUpwards = ResourceBundle.getBundle("Forum_config").getString("upwards").equals("true");
	
	public static String getDate() {

		Date d = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat(ResourceBundle.getBundle("Forum_config").getString("timeformat"));
		return fmt.format(d);	
	}
	
	public static void setSortUpwards(boolean sortUp) {
		sortUpwards = sortUp;
	}
	
	public static void setSortUpwards(String sortParameter) {
		if (sortParameter == null) { //sort-parameter doesn't exist
			
			sortUpwards = ResourceBundle.getBundle("Forum_config").getString("upwards").equals("true");
		
		} else if (sortParameter.equals("up")) { 
			sortUpwards = true;
		} else if(sortParameter.equals("down")){
			sortUpwards = false;
			
		} else { // sort-parameter is set in the wrong way
			
			sortUpwards = ResourceBundle.getBundle("Forum_config").getString("upwards").equals("true");
		}
	}

	public static boolean getSortUpwards() {
		return sortUpwards;
	}
	
	public static void sortUpwards() {
		setSortUpwards(true);
	}
	
	public static void sortDownwards() {
		setSortUpwards(false);
	}
	

	private static String maskHTML(String s) {
		return KnowWEEnvironment.maskHTML(s);
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		ResourceBundle rb = ForumModule.getForumBundle(user);
		
		String topic = sec.getTitle();
		
		String web = sec.getWeb();
		
		StringBuffer toHTML = new StringBuffer();
		
		Map<String, String> forumMap = AbstractXMLObjectType.getAttributeMapFor(sec);
		
		// load sort-parameter from URL:
		setSortUpwards(user.getUrlParameterMap().get("sort"));
		
		//load javascript-file:
		toHTML.append(maskHTML("<script type=text/javascript src=KnowWEExtension/scripts/ForumPlugin.js></script>\n"));
		
		boolean canEditPage = false;
		if(user.getUsername() != "Guest") { // causes endless loop
			
			//check edit-permission
			canEditPage = KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(topic, user.getHttpRequest());
		
			//load action once after server restart (javascript-failure)
			if(canEditPage && firstStart) {
				toHTML.append(maskHTML("<script>loadAction('" + topic + "')</script>\n"));
				firstStart = false;
			}	
		}
		
		// back link:
		String link = forumMap.get("ref");
		if(link != null) toHTML.append(maskHTML("<a href=" + link + "><< back</a><br><br>\n"));
		
		// title:
		String title = forumMap.get("name");
		if(title != null) toHTML.append(maskHTML("<h2>" + title + "</h2><br><hr>\n"));
		
		List<Section> contentSectionList = new ArrayList<Section>();
		sec.findSuccessorsOfType(ForumBox.class, contentSectionList);
		
		Section section0 = contentSectionList.get(0);
		string.append(toHTML);
		toHTML.setLength(0);
		section0.getObjectType().getRenderer().render(section0, user, string);
		
		//if user is logged in: tool to add easily a post:
		if (canEditPage) {
			toHTML.append(maskHTML("<br><form name=answer method=post action=Wiki.jsp?page=" + topic + "&sort=" + (sortUpwards?"up":"down") + " accept-charset=UTF-8>"));
		}
		
		toHTML.append(maskHTML("<table width=95% border=0>\n"));
		toHTML.append(maskHTML("<tr><th align=left></th><th align=right width=150>\n"));
		
		//sort posts up- or downwards
		toHTML.append(maskHTML("<a href=Wiki.jsp?page=" + topic + "&sort=up title='" + rb.getString("Forum.sort.up") + "'><img src=KnowWEExtension/images/ct_up.gif title='" + rb.getString("Forum.sort.up") + "'></a>\n"));
		toHTML.append(maskHTML("<a href=Wiki.jsp?page=" + topic + "&sort=down title='" + rb.getString("Forum.sort.down") + "'><img src=KnowWEExtension/images/ct_down.gif title='" + rb.getString("Forum.sort.down") + "'></a>\n</th></tr>\n"));
		
		if(canEditPage) {
			toHTML.append(maskHTML("<tr><td colspan=2><p align=right><textarea name=text cols=68 rows=8></textarea><br>\n"));
			toHTML.append(maskHTML("<input type=hidden name=topic value='" + topic + "'>"));
			toHTML.append(maskHTML("<input type=submit name=Submit value='" + rb.getString("Forum.button.postMessage") + "' onclick=saveForumBox()></p></td></tr>"));
			toHTML.append(maskHTML("</table></form><hr><br>\n"));
		} else {
			toHTML.append(maskHTML("</table><hr><br>\n"));
		}
	    
		string.append(toHTML);
		
	    if(sortUpwards) {
			for(int i = 1; i < contentSectionList.size(); i++) {	
				Section sectionI = contentSectionList.get(i);
				sectionI.getObjectType().getRenderer().render(sectionI, user, string);
			}
		} else {
			for(int i = contentSectionList.size() - 1; i > 0; i--) {	
				Section sectionI = contentSectionList.get(i);
				sectionI.getObjectType().getRenderer().render(sectionI, user, string);
			}
		}
	    
		if (change) {
			StringBuilder buffi = new StringBuilder();
			
			KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
			instance.getArticle(web, topic).getSection().collectTextsFromLeaves(buffi);
			KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.WEB, web);
			map.put(KnowWEAttributes.TOPIC, topic);
			map.put(KnowWEAttributes.USER, user.toString());
			instance.saveArticle(web, topic, buffi.toString(), map);
			
			change = false;
		}
		
		string.append(toHTML);
	}
}
