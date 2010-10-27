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

	public static String renderToolbar(Tool[] tools) {
		String toolbarHtml = " | <div class='markupTools'> ";
		for (Tool tool : tools) {
			toolbarHtml +=
					" <a href=\"javascript:" + tool.getJSAction() + ";undefined;\">" +
							"<img " +
							"title=\"" + tool.getDescription() + "\" " +
							"src=\"" + tool.getIconPath() + "\"></img>" +
							"</a>";

		}
		toolbarHtml += "</div>";
		return toolbarHtml;
	}

	public static String renderMenu(Tool[] tools, String id) {
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
		return menuHtml;
	}

	public static String renderMenuAnimation(String id) {
		return "\n<script>\n" +
				"var makeMenuFx = function() {" +
				"var a=$('header_" + id + "'),c=$('menu_" + id + "');" +
				"if(!a||!c){}\n" +
				"var b=c.effect(\"opacity\",{wait:false}).set(0);" +
				"a.adopt(c).set({href:\"#\",events:{" +
				"mouseout:function(){b.start(0)}," +
				"click:function(){b.start(0.9)}," +
				"mouseover:function(){b.start(0.9)}}});" +
				"};" +
				"makeMenuFx();" +
				"</script>\n";
	}

	public static String renderHeader(String iconPath, String title) {
		String result = "";
		// render icon
		if (iconPath != null) {
			result = "<img class='markupIcon' src='" + iconPath + "'></img> ";
		}

		// render heading
		result += "<span>" + title + "</span>";
		return KnowWEUtils.maskHTML(result);
	}

	public static void renderDefaultMarkupStyled(String header, String content, String sectionID, Tool[] tools, StringBuilder string) {

		string.append(KnowWEUtils.maskHTML("<div id=\"" + sectionID + "\" class='defaultMarkup'>\n"));

		boolean hasTools = tools != null && tools.length > 0;
		boolean hasMenu = hasTools;
		boolean hasToolbar = false;

		String toolbarHtml = "";
		if (hasToolbar) {
			toolbarHtml = renderToolbar(tools);
		}

		string.append(KnowWEUtils.maskHTML(
				"<div id='header_" + sectionID + "' " +
						"class='markupHeader" + (hasMenu ? " markupMenuIndicator" : "") + "'>"));
		string.append(header);
		string.append(KnowWEUtils.maskHTML(toolbarHtml));
		string.append("\n");

		if (hasMenu) {
			String menuHtml = renderMenu(tools, sectionID);
			string.append(KnowWEUtils.maskHTML(menuHtml));
		}

		string.append(KnowWEUtils.maskHTML("</div>"));

		if (hasMenu) {
			string.append(KnowWEUtils.maskHTML(renderMenuAnimation(sectionID)));
		}

		// render pre-formatted box
		string.append(KnowWEUtils.maskHTML("<div id=\"content_" + sectionID
				+ "\" class='markupText'>"));

		// render content
		string.append(content);

		// and close the box(es)
		string.append(KnowWEUtils.maskHTML("</div>\n")); // class=markupText
		string.append(KnowWEUtils.maskHTML("</div>\n")); // class=defaultMarkup
	}

	@Override
	public void render(KnowWEArticle article, Section<T> section, KnowWEUserContext user, StringBuilder buffer) {
		String id = section.getID();
		Tool[] tools = ToolUtils.getTools(article, section, user);

		// render Header
		StringBuilder header = new StringBuilder();
		renderHeader(article, section, user, header);

		// create content
		StringBuilder content = new StringBuilder();
		article = PackageRenderUtils.checkArticlesCompiling(article, section, content);

		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(section);
		content.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));

		// render messages and content
		renderMessages(article, section, content);
		renderContents(article, section, user, content);

		renderDefaultMarkupStyled(header.toString(), content.toString(), id, tools, buffer);
	}

	protected void renderHeader(KnowWEArticle article, Section<T> section, KnowWEUserContext user, StringBuilder string) {
		String icon = getHeaderIcon(article, section, user);
		String title = getHeaderName(article, section, user);
		string.append(renderHeader(icon, title));
	}

	protected String getHeaderName(KnowWEArticle article, Section<T> section, KnowWEUserContext user) {
		return section.getObjectType().getName();
	}

	protected String getHeaderIcon(KnowWEArticle article, Section<T> section, KnowWEUserContext user) {
		return this.iconPath;
	}

	protected void renderContents(KnowWEArticle article, Section<T> section, KnowWEUserContext user, StringBuilder string) {
		List<Section<?>> subsecs = section.getChildren();
		Section<?> first = subsecs.get(0);
		Section<?> last = subsecs.get(subsecs.size() - 1);
		for (Section<?> subsec : subsecs) {
			if (subsec == first && subsec.getObjectType() instanceof PlainText) {
				continue;
			}
			if (subsec == last && subsec.getObjectType() instanceof PlainText) {
				continue;
			}
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
			if (displayLineNo && lineNo > 0) {
				string.append(" Line: " + lineNo);
			}
			string.append("\n");
		}
		string.append(KnowWEUtils.maskHTML("</span>"));
	}
}