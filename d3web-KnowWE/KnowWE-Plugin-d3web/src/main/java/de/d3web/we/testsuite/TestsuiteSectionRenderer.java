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

package de.d3web.we.testsuite;

import java.util.Collection;

import de.d3web.report.Message;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.ErrorRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TestsuiteSectionRenderer extends KnowWEDomRenderer {
	
	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		StringBuilder b = new StringBuilder();
		
		// Render Title and Download Buttons
		String title = "Testsuite "	+ generateLinkIcons(article.getTitle(), article.getWeb(), sec.getId());		
		
		// Render anker
		b.append(KnowWEUtils.maskHTML("<a name=\"" + sec.getId() + "\" />"));
		
		// Render Errors / Tooltip
		String tooltip = null;
		if (sec.getObjectType() instanceof AbstractKnowWEObjectType) {
			Collection<Message> messages = AbstractKnowWEObjectType.getMessages(article, sec);
			if (messages != null && !messages.isEmpty()) {
				boolean visible = false;
				for (Message msg : messages) {
					if (msg.getMessageType() == Message.ERROR || msg.getMessageType() == Message.WARNING) {
						visible = true;
						break;
					}
				}
				String notes = generateMessages(messages, Message.NOTE, sec, user);
				String warns = generateMessages(messages, Message.WARNING, sec, user);
				String errors = generateMessages(messages, Message.ERROR, sec, user);
				if (visible) {
					b.append(wrappSpan(errors, "error"));
					b.append(wrappSpan(warns, "warning"));
					b.append(wrappSpan(notes, "info"));
				}
				else {
					tooltip = notes;
				}
			}
		}
			
		// Render Testsuite-Section 
		DelegateRenderer.getInstance().render(article, sec, user, b);
		string.append(wrapCollapsebox(title, wrapContent(b.toString(), tooltip)));
	}

	private String wrapCollapsebox(String title, String render) {
		StringBuilder result = new StringBuilder();
		result.append("%%collapsebox-closed \n");
		result.append("! " +title + " \n");
		result.append(render);
		result.append("/%\n");
		return result.toString();
	}
	
	protected String wrapContent(String string, String tooltip) {
		if (tooltip != null) {
			string = KnowWEUtils.maskHTML("<div title='"+tooltip+"'>")
					+ string
					+ KnowWEUtils.maskHTML("</div>");
		}
		return "\n{{{"+string+"}}}\n";
	}
	
	private String wrappSpan(String messages, String className) {
		if (messages == null) return null;
		if (messages.isEmpty()) return "";
		return KnowWEUtils.maskHTML("<span class='"+className+"'>")
				+ messages 
				+ KnowWEUtils.maskHTML("</span>");
	}
	
	private String generateMessages(Collection<Message> messages, String messageType, Section sec, KnowWEUserContext user) {
		StringBuilder result = new StringBuilder();
		for (Message m : messages) {
			if (m.getMessageType() != messageType) 
				continue;
			result.append(m.getMessageText());
			result.append(KnowWEUtils.maskHTML("\n"));
			//TODO don't highlight all nodes which have the same type as the errorNode!
//			if(m.getMessageType().equals(Message.ERROR))
//				insertErrorRenderer(sec, m, user.getUsername());
		}
		return result.toString();
	}
	
	private String generateLinkIcons(String topic, String web, String nodeID) {
		StringBuilder result = new StringBuilder();
		result.append(generateTXTLink(topic, web, nodeID));
		result.append(generateXMLLink(topic, web, nodeID));
		return result.toString();
	}
	
	private String generateXMLLink(String topic, String web, String nodeID) {
		String icon = "<img src=KnowWEExtension/images/drive_disk.png title='Download XML File' /></img>";
		String result = "<a href='testsuitedownload?type=case&KWiki_Topic="
			+ topic + "&web=" + web + "&nodeID=" + nodeID + "&filename=" + topic
			+ "_testsuite.xml' >" + icon + "</a>";
		return KnowWEUtils.maskHTML(result);
	}

	private String generateTXTLink(String topic, String web, String nodeID) {
		
		String icon = "<img src=KnowWEExtension/images/disk.png title='Download TXT File' /></img>";
		String result = "<a href='testsuitedownload?type=case&KWiki_Topic="
			+ topic + "&web=" + web + "&nodeID=" + nodeID + "&filename=" + topic
			+ "_testsuite.txt' >" + icon + "</a>";
		return KnowWEUtils.maskHTML(result);
	}
	
	protected void insertErrorRenderer(Section sec, Message m, String user) {
		String text = m.getLine();
		if(text == null || text.length() == 0) return;
		Section errorSec = sec.findSmallestNodeContaining(text);
		((AbstractKnowWEObjectType) errorSec.getObjectType()).setCustomRenderer(ErrorRenderer.getInstance());
	}

}