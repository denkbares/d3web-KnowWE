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

package de.d3web.we.action;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.xml.XMLTail;
import de.d3web.we.plugin.forum.ForumRenderer;

public class ForumBoxAction implements KnowWEAction {

	
	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String text = parameterMap.get("ForumBoxText");
		HttpServletRequest r = parameterMap.getRequest();
		
		String topic = parameterMap.get("ForumArticleTopic");
		String web = parameterMap.getWeb();
		
		if(KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(topic, r)) {
			
			String save = "\n</forum>";
			
			if(text != null && text.length() > 0) { // don't add an empty box
			
				// ISO 8859-1 --> UTF-8
				Charset iso = Charset.forName("ISO-8859-1");
				ByteBuffer bb = iso.encode(text);
				Charset utf8 = Charset.forName("UTF-8");
				text = utf8.decode(bb).toString();
				
				text = text.replace("\n", "\\\\ ");
					
				save = "<box name=\"" + parameterMap.getUser() + "\"; date=\"" + ForumRenderer.getDate() + "\">" + text + "</box>\n</forum>";
		
			}
			
			Section sec = KnowWEEnvironment.getInstance().getArticle(web, topic).getSection();
			
			List<Section> found = new ArrayList<Section>();
			sec.findSuccessorsOfType(XMLTail.class, found);
			
			if (found.size() != 0) {
				Section changeSec = found.get(found.size() - 1);
				changeSec.findChildOfType(PlainText.class).setOriginalText(save);
			}
			
			StringBuilder buffi = new StringBuilder();
			sec.collectTextsFromLeaves(buffi);
			KnowWEEnvironment.getInstance().saveArticle(web, topic, buffi.toString(), parameterMap);
		
		}
		
		return null;
	}
	
	
}
