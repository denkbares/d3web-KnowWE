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
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KopicSectionRenderer extends KnowWEDomRenderer {

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {

		string.append("%%collapsebox-closed \n");
		
		StringBuilder messagesBuilder = new StringBuilder();
		if (sec.getObjectType() instanceof AbstractKnowWEObjectType) {
			Collection<Message> messages = ((AbstractKnowWEObjectType) sec
					.getObjectType()).getMessages(sec);
			boolean errors = false;
			if (messages != null && !messages.isEmpty()) {
				messagesBuilder.append(wrappMessages(generateMessages(messages, sec, user)));
				for (Message msg:messages) {
					if (msg.getMessageType() == Message.ERROR) {
						errors = true;
						break;
					}
				}
			}
			string.append(generateTitle(sec, user, errors));
			string.append(messagesBuilder);
		}
		
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(sec,user, b);
		string.append(wrappContent(b.toString()));
		string.append("/%\n");
	}
	
	/*
	 * Doesn't work at the moment -> deaktivated
	 */
	protected String generateQuickEditLink(String topic, String id, String web2, String user) {
//		String icon = " <img src=KnowWEExtension/images/pencil.png title='Set QuickEdit-Mode' onclick='setQuickEditFlag(&quot;"+id+"&quot;,&quot;"+topic+"&quot;);window.event.cancelBubble=true;var mooEvent = new Event(window.event);mooEvent.stop();'  ></img>";
//
//		return KnowWEEnvironment
//				.maskHTML("<a>"+icon+"</a>");
		return "";
	}

	protected void insertErrorRenderer(List<Section> lines, Message m, String user) {
		int line = m.getLineNo();
		if(line - 1 >= 0 && line - 1 < lines.size()) {
		lines.get(line-1).setRenderer(ErrorRenderer.getInstance());
		}
	}

	protected String wrappContent(String string) {
		return "\n{{{"+string+"}}}\n";
	}
	
	protected String wrappMessages(String messages) {
		return "{{{" + messages + "}}}";
	}
	
	protected String generateMessages(Collection<Message> messages, Section sec, KnowWEUserContext user) {
		StringBuilder result = new StringBuilder();
		List<Section> lines = new ArrayList<Section>(); 
		sec.findSuccessorsOfType(TextLine.class, lines);
		for (Message m : messages) {
			result.append(m.getMessageType() + ": " + m.getMessageText()
					+ (m.getMessageType().equals(Message.NOTE) ? "" : " Line: " + m.getLineNo()) 
					+ KnowWEEnvironment.maskHTML("<br>"));
			if(m.getMessageType().equals(Message.ERROR)) {
				insertErrorRenderer(lines, m, user.getUsername());
			}
		}
		return result.toString();
	}
	
	protected String generateTitle(Section sec, KnowWEUserContext user, boolean errors) {
		String title = "";
		if (errors) {
			title += KnowWEEnvironment.maskHTML("<img src=KnowWEExtension/images/statisticsError.gif title='" 
					+ D3webModule.getKwikiBundle_d3web().getString("KnowWE.KopicRenderer.errorTooltip") 
					+ "'></img> ");
		}
		title += ((AbstractXMLObjectType)sec.getObjectType()).getXMLTagName() + " ";
		if (errors) {
			title = KnowWEEnvironment.maskHTML("<a style='color:rgb(255, 0, 0)' title='" 
					+ D3webModule.getKwikiBundle_d3web().getString("KnowWE.KopicRenderer.errorTooltip") + "'>" 
					+ title
					+ "</a>");
		}
		title += generateQuickEditLink(sec.getTitle(), sec.getId(), sec.getWeb(), user.getUsername());
		title =  "! " + title + " \n";
		
		return title;
	}

}
