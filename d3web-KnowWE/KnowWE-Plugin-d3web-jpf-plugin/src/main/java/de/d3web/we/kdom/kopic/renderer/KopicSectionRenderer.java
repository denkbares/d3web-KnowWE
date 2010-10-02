/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.kopic.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.packaging.PackageRenderUtils;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KopicSectionRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		string.append(KnowWEUtils.maskHTML("<a name=\"" + sec.getID() + "\" ></a>"));
		// string.append("%%collapsebox-closed \n");

		StringBuilder builder = new StringBuilder();
		String tooltip = null;
		article = PackageRenderUtils.checkArticlesCompiling(article, sec, builder);
		if (sec.getObjectType() instanceof AbstractKnowWEObjectType) {
			Collection<Message> messages = AbstractKnowWEObjectType.getMessages(article, sec);
			if (messages != null && !messages.isEmpty()) {
				boolean visible = false;
				for (Message msg : messages) {
					if (msg.getMessageType() == Message.ERROR
							|| msg.getMessageType() == Message.WARNING) {
						visible = true;
						break;
					}
				}
				String notes = generateMessages(messages, Message.NOTE, sec, user);
				String warns = generateMessages(messages, Message.WARNING, sec, user);
				String errors = generateMessages(messages, Message.ERROR, sec, user);
				if (visible) {
					builder.append(wrappSpan(errors, "error"));
					builder.append(wrappSpan(warns, "warning"));
					builder.append(wrappSpan(notes, "info"));
				}
				else {
					tooltip = notes;
				}
			}
			// string.append(generateTitle(sec, user, errors));
			// string.append(messagesBuilder);
		}

		DelegateRenderer.getInstance().render(article, sec, user, builder);
		string.append(wrappContent(builder.toString(), tooltip));
		// string.append("/%\n");
	}

	protected void insertErrorRenderer(List<Section> lines, Message m, String user) {
		int line = m.getLineNo();
		if (line - 1 >= 0 && line - 1 < lines.size()) {
			((AbstractKnowWEObjectType) lines.get(line - 1).getObjectType()).setCustomRenderer(ErrorRenderer.getInstance());
		}
	}

	protected String wrappContent(String string, String tooltip) {
		if (tooltip != null) {
			string = KnowWEUtils.maskHTML("<div title='" + tooltip + "'>")
					+ string
					+ KnowWEUtils.maskHTML("</div>");
		}
		return "\n{{{" + string + "}}}\n";
	}

	protected String wrappSpan(String messages, String className) {
		if (messages == null) return null;
		if (messages.isEmpty()) return "";
		return KnowWEUtils.maskHTML("<span class='" + className + "'>")
				+ messages
				+ KnowWEUtils.maskHTML("</span>");
	}

	protected String generateMessages(Collection<Message> messages, String messageType, Section sec, KnowWEUserContext user) {
		StringBuilder result = new StringBuilder();
		List<Section> lines = new ArrayList<Section>();
		sec.findSuccessorsOfType(TextLine.class, lines);
		for (Message m : messages) {
			if (m.getMessageType() != messageType) continue;
			result.append(
					m.getMessageText()
							+ (m.getMessageType().equals(Message.NOTE) ? "" : " Line: "
									+ m.getLineNo())
							+ KnowWEUtils.maskHTML("\n"));
			if (m.getMessageType().equals(Message.ERROR)) {
				insertErrorRenderer(lines, m, user.getUsername());
			}
		}
		return result.toString();
	}

	protected String generateTitle(Section sec, KnowWEUserContext user, boolean errors) {
		String title = "";
		if (errors) {
			title += KnowWEUtils.maskHTML("<img src=KnowWEExtension/images/statisticsError.gif title='"
					+ D3webModule.getKwikiBundle_d3web().getString(
							"KnowWE.KopicRenderer.errorTooltip")
					+ "'></img> ");
		}
		title += ((AbstractXMLObjectType) sec.getObjectType()).getXMLTagName() + " ";
		if (errors) {
			title = KnowWEUtils.maskHTML("<a style='color:rgb(255, 0, 0)' title='"
					+ D3webModule.getKwikiBundle_d3web().getString(
							"KnowWE.KopicRenderer.errorTooltip") + "'>"
					+ title
					+ "</a>");
		}
		title = "! " + title + " \n";

		return title;
	}

}
