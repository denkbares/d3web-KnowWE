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

package de.d3web.we.kdom.kopic.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.rendering.DefaultEditSectionRender;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SetCoveringListSectionRenderer extends DefaultEditSectionRender {
	
	@Override
	public void renderContent(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		string.append("%%collapsebox-closed \n");
		
		String title = "";
		if(sec.getObjectType() instanceof AbstractXMLObjectType) {
			title = ((AbstractXMLObjectType)sec.getObjectType()).getXMLTagName()+" ";
		}

		string.append("! " +title + " \n");
		if (sec.getObjectType() instanceof AbstractKnowWEObjectType) {
			KnowWEObjectType type = sec.getObjectType();
			Collection<Message> messages = ((AbstractKnowWEObjectType) type)
												.getMessages(article, sec);
			if (messages != null && !messages.isEmpty()) {
				string.append("{{{");
				for (Message m : messages) {
					string.append(m.getMessageType()+": "+m.getMessageText()+" Line: "+m.getLineNo()+KnowWEUtils.maskHTML("<br>"));
					if(m.getMessageType().equals(Message.ERROR)) {
						insertErrorRenderer(sec, m, user.getUsername());
					}
				}
				string.append("}}}");
			}
		}
		
		string.append(KnowWEUtils.maskHTML(
				"<pre class=\"ReRenderSectionMarker\" id=\"" + sec.getId() + "-pre\" rel=\"{id:'"+sec.getId()+"'}\">"));
		
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(article, sec, user, b);
		string.append(b.toString());
		
		string.append(KnowWEUtils.maskHTML("</pre>"));
		
		string.append("/%\n");
	}
		
	protected void insertErrorRenderer(Section sec, Message m, String user) {
		String text = m.getLine();
		if(text == null || text.length() == 0) return;
		Section errorSec = sec.findSmallestNodeContaining(text);
		errorSec.setRenderer(ErrorRenderer.getInstance());
		
	}

}