/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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
package de.d3web.we.kdom.defaultMarkup;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.core.packaging.PackageRenderUtils;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMNotice;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultMarkupRenderer<T extends DefaultMarkupType> extends KnowWEDomRenderer<T> {

	private final String iconPath;

	public DefaultMarkupRenderer() {
		this(null);
	}

	public DefaultMarkupRenderer(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public void render(KnowWEArticle article, Section<T> section, KnowWEUserContext user, StringBuilder string) {

		String id = section.getID();
		string.append(KnowWEUtils.maskHTML("<div id=\"" + id + "\" class='defaultMarkup'>\n"));

		String name = "<span>" + section.getObjectType().getName() + "</span>";
		String icon = "";
		if (this.iconPath != null) {
			icon = "<img class='markupIcon' src='" + this.iconPath + "'></img> ";
		}
		Tool[] tools = ToolUtils.getTools(article, section, user);
		boolean hasTools = tools != null && tools.length > 0;
		boolean hasMenu = hasTools;
		boolean hasToolbar = false;

		String toolbarHtml = "";
		if (hasToolbar) {
			toolbarHtml += " | <div class='markupTools'> ";
			for (Tool tool : tools) {
				toolbarHtml +=
						" <a href=\"javascript:" + tool.getJSAction() + ";undefined;\">" +
								"<img " +
								"title=\"" + tool.getDescription() + "\" " +
								"src=\"" + tool.getIconPath() + "\"></img>" +
								"</a>";

			}
			toolbarHtml += "</div>";
		}

		string.append(KnowWEUtils.maskHTML(
				"<div id='header_" + id + "' " +
						"class='markupHeader " + (hasMenu ? "markupMenuIndicator" : "") + "'>" +
						icon +
						name +
						toolbarHtml +
						"\n"));
		if (hasMenu) {
			String menuHtml = "<div id='menu_" + id + "' class=markupMenu>";
			for (Tool tool : tools) {
				menuHtml += "<div class='markupMenuItem'>" +
						"<a class='markupMenuItem'" +
						" href=\"javascript:" + tool.getJSAction() + ";undefined;\"" +
						" title=\"" + tool.getDescription() + "\">" +
						"<img src=\"" + tool.getIconPath() + "\"></img>" +
						" " + tool.getTitle() +
						"</a>" +
						"</div>";
			}
			menuHtml += "</div>";
			string.append(KnowWEUtils.maskHTML(menuHtml));
		}

		string.append(KnowWEUtils.maskHTML("</div>"));

		if (hasMenu) {
			// string.append(KnowWEUtils.maskHTML("<script>Wiki.makeMenuFx('parent_"+id+"', 'menu_"+id+"');</script>"));
			string.append(KnowWEUtils.maskHTML("\n<script>\n" +
					"var makeMenuFx = function() {" +
					"var a=$('header_" + id + "'),c=$('menu_" + id + "');" +
					"if(!a||!c){}\n" +
					"var b=c.effect(\"opacity\",{wait:false}).set(0);" +
					"a.adopt(c).set({href:\"#\",events:{" +
					"mouseout:function(){b.start(0)}," +
					"mouseover:function(){b.start(0.9)}}});" +
					"};" +
					"makeMenuFx();" +
					"</script>\n"));
		}

		// render pre-formatted box
		// string.append("{{{\n");
		string.append(KnowWEUtils.maskHTML("<div id=\"" + id + "\" class='markupText'>"));
		article = PackageRenderUtils.checkArticlesCompiling(article,
				section, string);

		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(section);
		string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));

		// render messages and content
		renderMessages(article, section, string);
		renderContents(article, section, user, string);

		// and close the box
		// string.append("}}}\n");
		string.append(KnowWEUtils.maskHTML("</div>\n")); // class=markupText
		string.append(KnowWEUtils.maskHTML("</div>\n")); // class=defaultMarkup

	}

	protected void renderContents(KnowWEArticle article, Section<T> section, KnowWEUserContext user, StringBuilder string) {
		List<Section<?>> subsecs = section.getChildren();
		Section<?> first = subsecs.get(0);
		Section<?> last = subsecs.get(subsecs.size() - 1);
		for (Section<?> subsec : subsecs) {
			if (subsec == first && subsec.getObjectType() instanceof PlainText) continue;
			if (subsec == last && subsec.getObjectType() instanceof PlainText) continue;
			subsec.getObjectType().getRenderer().render(article, subsec, user, string);
		}
	}

	protected void renderMessages(KnowWEArticle article, Section<? extends DefaultMarkupType> section, KnowWEUserContext user, StringBuilder string) {
		renderMessages(article, section, string);
	}

	public static void renderMessages(KnowWEArticle article, Section<? extends DefaultMarkupType> section, StringBuilder string) {
		Collection<Message> messages = AbstractKnowWEObjectType.getMessagesFromSubtree(article,
				section);
		renderMessageBlock(getMessagesOfType(messages, Message.ERROR), string);
		renderMessageBlock(getMessagesOfType(messages, Message.WARNING), string);
		renderMessageBlock(getMessagesOfType(messages, Message.NOTE), string);
		renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article, section,
				KDOMError.class), string);
		renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article, section,
				KDOMWarning.class), string);
		// renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article,
		// section,
		// KDOMNotice.class), string);
	}

	private static Message[] getMessagesOfType(Collection<Message> allMessages, String messageType) {
		if (allMessages == null) return null;
		Collection<Message> result = new LinkedList<Message>();
		for (Message message : allMessages) {
			if (messageType.equals(message.getMessageType())) {
				result.add(message);
			}
		}
		return result.toArray(new Message[result.size()]);
	}

	private static void renderKDOMReportMessageBlock(Collection<? extends KDOMReportMessage> messages, StringBuilder string) {
		if (messages == null) return;
		if (messages.size() == 0) return;

		Class<? extends KDOMReportMessage> type = messages.iterator().next().getClass();
		String className = "";
		if (KDOMNotice.class.isAssignableFrom(type)) {
			className = "information";
		}
		else if (KDOMWarning.class.isAssignableFrom(type)) {
			className = "warning";
		}
		else if (KDOMError.class.isAssignableFrom(type)) {
			className = "error";
		}

		string.append(KnowWEUtils.maskHTML("<span class='" + className + "'>"));
		for (KDOMReportMessage error : messages) {
			string.append(error.getVerbalization());
			string.append("\n");
		}
		string.append(KnowWEUtils.maskHTML("</span>"));
	}

	private static void renderMessageBlock(Message[] messages, StringBuilder string) {
		if (messages == null) return;
		if (messages.length == 0) return;

		String type = messages[0].getMessageType();
		String className = "";
		boolean displayLineNo = true;
		if (Message.NOTE.equals(type)) {
			className = "information";
			displayLineNo = false;
		}
		else if (Message.WARNING.equals(type)) {
			className = "warning";
		}
		else if (Message.ERROR.equals(type)) {
			className = "error";
		}

		string.append(KnowWEUtils.maskHTML("<span class='" + className + "'>"));
		for (Message error : messages) {
			string.append(error.getMessageText());
			int lineNo = error.getLineNo();
			if (displayLineNo && lineNo > 0) string.append(" Line: " + lineNo);
			string.append("\n");
		}
		string.append(KnowWEUtils.maskHTML("</span>"));
	}
}