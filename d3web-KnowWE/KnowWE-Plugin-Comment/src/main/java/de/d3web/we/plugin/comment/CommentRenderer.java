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

package de.d3web.we.plugin.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.XMLTail;
import de.d3web.we.plugin.forum.ForumRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CommentRenderer extends KnowWEDomRenderer{
	
	public static String clean(String s) {

		String toClean = s;
		
		// characters which have to be replaced in the page-name 
		toClean = toClean.replace("ä", "ae");
		toClean = toClean.replace("Ä", "Ae");
		toClean = toClean.replace("ö", "oe");
		toClean = toClean.replace("Ö", "Oe");
		toClean = toClean.replace("ü", "ue");
		toClean = toClean.replace("Ü", "Ue");
		toClean = toClean.replace("ß", "ss");
		toClean = toClean.replace("!", ".");
		toClean = toClean.replace("?", ".");
		toClean = toClean.replace("#", " ");
		toClean = toClean.replace("<", " ");
		toClean = toClean.replace(">", " ");
		
		return toClean;
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		
		Map<String, String> commentTypes = CommentModule.getCommentTypes();
		
		StringBuffer toHTML = new StringBuffer();
		
		try { // check whether WikiEngine is properly started yet
			
			KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
			KnowWEWikiConnector wikiConnector = instance.getWikiConnector();
			
			String commentTag = sec.findChildOfType(CommentTypeTag.class).findChildOfType(CommentTypeTagName.class).getOriginalText();
			String pageName;
			
			String commentContent = sec.findChildOfType(CommentTypeContent.class).getOriginalText();
			
			// split title and content:
			String title = "";
			int i = commentContent.indexOf(":");
			if(i >= 0) {
				title = commentContent.substring(0, i);
				commentContent = commentContent.substring(i + 1);
			}

			Map<String, Integer> ids = CommentModule.getIDs();
			
			Section idSec = sec.findChildOfType(CommentTypeTag.class).findChildOfType(CommentTypeTagID.class);
			String id = idSec.getOriginalText().trim();
			
			// add ID if not done before:
			if(id.isEmpty()) {
	
				int newID = ids.get(commentTag);
				idSec.findChildOfType(PlainText.class).setOriginalText(newID + " ");
				
				// save id:
				StringBuilder buffy = new StringBuilder();
				sec.getArticle().getSection().collectTextsFromLeaves(buffy);
				
				if (title.length() > 0) {
					pageName = clean(title);
				} else pageName = commentTag + newID;
				
				// create new wikipage:
				if(!wikiConnector.doesPageExist(pageName)) {
					
					StringBuffer saveContent = new StringBuffer();
					saveContent.append("<forum" + (title==""?"":" name=\"" + title + "\" ") + "ref=\"Wiki.jsp?page=" + sec.getTitle() + "#" + pageName.replace(" ", "+") + "\">\n");
					saveContent.append("<box name=" + user.getUsername() + "; date=" + ForumRenderer.getDate() + ">" + commentContent + "</box>\n</forum>");
					
					instance.getWikiConnector().createWikiPage(pageName, saveContent.toString(), user.getUsername());
				
				} else { //page exists ==> add a new box:
			
					String save = "<box name=\"" + user.getUsername() + "\"; date=\""
									+ ForumRenderer.getDate() + "\">--> A new comment to this topic on page ["
									+ sec.getTitle() + "]:\\\\ \\\\" + commentContent + "</box>\n</forum>";
					
					Section forumSec =instance.getArticle(sec.getWeb(), pageName).getSection();
					
					List<Section> found = new ArrayList<Section>();
					forumSec.findSuccessorsOfType(XMLTail.class, found);
					
					if (found.size() != 0) {
						Section changeSec = found.get(found.size() - 1);
						changeSec.findChildOfType(PlainText.class).setOriginalText(save);
					}
					
					StringBuilder buffi = new StringBuilder();
					forumSec.collectTextsFromLeaves(buffi);
					KnowWEParameterMap parameterMap = new KnowWEParameterMap(KnowWEAttributes.WEB, forumSec.getWeb());
					instance.saveArticle(forumSec.getWeb(), pageName, buffi.toString(), parameterMap);
								
				}
						
				KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
				instance.saveArticle(sec.getWeb(), sec.getTitle(), buffy.toString(), map);
				
			} else {
				int newID = Integer.valueOf(id);
				
				if(newID >= ids.get(commentTag)) ids.put(commentTag, (newID + 1));
					
				if (title.length() > 0) {
					pageName = clean(title);
				} else pageName = commentTag + newID;
				
				// create new wikipage with failure message caused by changing manually the id
				if(!wikiConnector.doesPageExist(pageName)) {
					StringBuffer saveContent = new StringBuffer();
					
					// if you want only one Error-page activate this:
					//pageName="ID_ERROR";
					
					saveContent.append("<CSS style=\"color:red; font-size:19px\">ATTENTION: Someone changed the id manually!</CSS>\\\\\n\\\\\n");
					saveContent.append("<forum" + (title==""?"":" name=\"" + title + "\" ") + "ref=\"Wiki.jsp?page=" + sec.getTitle() + "#" + pageName.replace(" ", "+") + "\">\n");
					saveContent.append("<box name=System; date=" + ForumRenderer.getDate() + ">" + commentContent + "</box>\n</forum>");
					instance.getWikiConnector().createWikiPage(pageName, saveContent.toString(), user.getUsername());
				}

			}

			String imageDir = "KnowWEExtension/" + commentTypes.get(commentTag);
			toHTML.append("<a name='" + pageName.replace(" ", "+") + "'></a><a target=_blank href=Wiki.jsp?page=" + pageName.replace(" ", "+") + "><img src=" + imageDir + " title='" + commentContent + "'></a>");
		
		} catch (Exception e) {
			
			// do nothing if WikiEngine is not properly started yet 
		}
		
		string.append(KnowWEEnvironment.maskHTML(toHTML.toString()));	
	}
}
