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

import java.util.Map;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.plugin.forum.ForumRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CommentRenderer extends KnowWEDomRenderer{

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Map<String, String> commentTypes = CommentModule.getCommentTypes();
		
		String toHTML = "";
		
		//sections before page-save will not be rendered
		if(!commentTypes.isEmpty()) {
			KnowWEWikiConnector wikiConnector = instance.getWikiConnector();
			
			Section idSec = sec.findChildOfType(CommentTypeTag.class).findChildOfType(CommentTypeTagID.class);
			
			String commentTag = sec.findChildOfType(CommentTypeTag.class).findChildOfType(CommentTypeTagName.class).getOriginalText();
			String commentContent = sec.findChildOfType(CommentTypeContent.class).getOriginalText();
			
			// split title and content:
			String title = "";
			int i = commentContent.indexOf(":");
			if(i >= 0) {
				title = commentContent.substring(0, i);
				commentContent = commentContent.substring(i + 1);
			}
			
			String pageName = commentTag;
			
			// add ID if not done before:
			String id = idSec.getOriginalText();
			
			if(id.length() <= 1) {
				int newId = CommentTypeTagID.getID();
				idSec.findChildOfType(PlainText.class).setOriginalText(newId + " ");
				CommentTypeTagID.setID(newId + 1);
				
				pageName += newId;
				if (title.length() > 0) {
					pageName = title;
				}
				// create new wikipage:
				if(!wikiConnector.doesPageExist(pageName)) {
					
					StringBuffer saveContent = new StringBuffer();
					saveContent.append("<forum" + (title==""?"":" name=\"" + title + "\" ") + "ref=\"Wiki.jsp?page=" + sec.getTitle() + "#" + pageName + "\">\n");
					saveContent.append("<box name=" + user.getUsername() + "; date=" + ForumRenderer.getDate() + ">" + commentContent + "</box>\n</forum>");
					
					instance.getWikiConnector().createWikiPage(pageName, saveContent.toString(), user.getUsername());
				}
				
				// save id:
				StringBuilder buffy = new StringBuilder();
				sec.getArticle().getSection().collectTextsFromLeaves(buffy);
				
				KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
				instance.saveArticle(sec.getWeb(), sec.getTitle(), buffy.toString(), map);
			} else {
				int newId = Integer.valueOf(idSec.getOriginalText().trim());
				if(newId >= CommentTypeTagID.getID()) CommentTypeTagID.setID(newId + 1);
					
				pageName += id.trim();
				if (title.length() > 0) {
					pageName = title;
				}
				// create new wikipage with failure message caused by changing manually the id
				if(!wikiConnector.doesPageExist(pageName)) {
					StringBuffer saveContent = new StringBuffer();
					saveContent.append("<CSS style=\"color:red; font-size:19px\">ATTENTION: Someone changed the id manually!</CSS>\\\\\n\\\\\n");
					saveContent.append("<forum" + (title==""?"":" name=\"" + title + "\" ") + "ref=\"Wiki.jsp?page=" + sec.getTitle() + "#" + pageName + "\">\n");
					saveContent.append("<box name=" + user.getUsername() + "; date=" + ForumRenderer.getDate() + ">" + commentContent + "</box>\n</forum>");
					instance.getWikiConnector().createWikiPage(pageName, saveContent.toString(), user.getUsername());
				}
				//If you wanna have only one Error-page:
//				if(!wikiConnector.doesPageExist(pageName)) {
//					pageName="ID_ERROR";
//					if(!wikiConnector.doesPageExist(pageName)) {
//						StringBuffer saveContent = new StringBuffer();
//						saveContent.append("<CSS style=\"color:red; font-size:19px\">ATTENTION: Someone changed the id manually!</CSS>\\\\\n\\\\\n");
//						saveContent.append("<forum" + (title==""?"":" name=" + title) + "; ref=Wiki.jsp?page=" + pageName + ">\n");
//						saveContent.append("<box name=System; date=" + ForumRenderer.getDate() + ">" + "If you create a new Comment, the id is generated automatically and grants acces to the right pagelink. If someone changes the id manually the icon would link to the wrong page, so you are directed to this site. Try to find the right id. \\\\ Good luck :)" + "</box>\n</forum>");
//						KnowWEEnvironment.getInstance().getWikiConnector().createWikiPage(pageName, saveContent.toString(), user.getUsername());
//					}
//				}
			}

			String imageDir = "KnowWEExtension/" + commentTypes.get(commentTag);
			toHTML = "<a name='" + pageName + "'></a><a target=_blank href=Wiki.jsp?page=" + pageName + "><img src=" + imageDir + " title='" + commentContent + "'></a>";
		}
		
		string.append(KnowWEEnvironment.maskHTML(toHTML));	
	}
}
