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
package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.ProgressRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolSet;
import de.knowwe.tools.ToolUtils;

public class DefaultMarkupRenderer implements Renderer {

	private final String iconPath;

	private ToolsRenderMode renderMode = ToolsRenderMode.MENU;
	private boolean preFormattedStyle = true;
	private boolean listAnnotations = false;

	public enum ToolsRenderMode {
		MENU, TOOLBAR
	}

	public DefaultMarkupRenderer() {
		this(null);
	}

	public DefaultMarkupRenderer(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getIconPath() {
		return iconPath;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult buffer) {
		String id = section.getID();
		ToolSet tools = getTools(section, user);

		// add an anchor to enable direct link to the section
		RenderResult markupTitle = new RenderResult(buffer);
		KnowWEUtils.renderAnchor(section, markupTitle);

		// render markup title
		renderTitle(section, user, markupTitle);

		// create content
		RenderResult content = new RenderResult(buffer);

		// render messages and content
		renderMessages(section, content);
		renderProgress(section, user, tools, content);
		renderContents(section, user, content);

		String cssClassName = "type_" + section.get().getName();

		renderDefaultMarkupStyled(
				markupTitle.toStringRaw(), content.toStringRaw(),
				id, cssClassName, tools, user, buffer);
	}

	protected ToolSet getTools(Section<?> section, UserContext user) {
		return ToolUtils.getTools(section, user);
	}

	protected void renderProgress(Section<?> section, UserContext user, ToolSet tools, RenderResult result) {
		// we have to instantiate the tools first,
		// to be sure that e.g. long operations are registered
		tools.getTools();
		ProgressRenderer.getInstance().render(section, user, result);
	}

	protected void renderTitle(Section<?> section, UserContext user, RenderResult string) {
		String icon = getTitleIcon(section, user);
		String title = getTitleName(section, user);

		// render icon
		if (icon != null) {
			string.appendHtml("<img class='markupIcon' src='" + icon + "' /> ");
		}

		// render heading
		string.appendHtml("<span>").append(title).appendHtml("</span>");
	}

	protected String getTitleIcon(Section<?> section, UserContext user) {
		return this.iconPath;
	}

	protected String getTitleName(Section<?> section, UserContext user) {
		return section.get().getName();
	}

	public void renderMessages(Section<?> section, RenderResult string) {
		renderMessageBlock(section, string, Message.Type.ERROR, Message.Type.WARNING);
	}

	private static Map<Section<?>, Map<Message, Collection<Compiler>>> getMessageSectionsOfSubtree(Section<?> rootSection, Type messageType) {
		Map<Section<?>, Map<Message, Collection<Compiler>>> collectedMessages = new LinkedHashMap<Section<?>, Map<Message, Collection<Compiler>>>();
		for (Section<?> subTreeSection : Sections.successors(rootSection)) {
			Collection<Compiler> compilers = new ArrayList<Compiler>(Compilers.getCompilers(
					subTreeSection, Compiler.class));
			compilers.add(null);
			Map<Message, Collection<Compiler>> compilersForMessage = new LinkedHashMap<Message, Collection<Compiler>>();
			for (Compiler compiler : compilers) {
				Collection<Message> messages = Messages.getMessages(compiler, subTreeSection,
						messageType);
				for (Message message : messages) {
					Collection<Compiler> messageCompilers = compilersForMessage.get(message);
					if (messageCompilers == null) {
						messageCompilers = new LinkedList<Compiler>();
						compilersForMessage.put(message, messageCompilers);
					}
					messageCompilers.add(compiler);
				}
			}
			if (!compilersForMessage.isEmpty()) {
				collectedMessages.put(subTreeSection, compilersForMessage);
			}
		}
		return collectedMessages;
	}

	public static void renderMessageBlock(Section<?> rootSection,
										  RenderResult string,
										  Message.Type... types) {

		for (Type type : types) {
			Collection<String> messages = getMessageStrings(rootSection, type);

			// only if there are such messages
			if (messages.isEmpty()) continue;
			String clazz = type.toString().toLowerCase();
			if (messages.size() == 1) {
				clazz += " singleLine";
			}
			string.appendHtml("<span class='" + clazz
					+ "' style='white-space: pre-wrap;'>");
			for (String messageString : messages) {
				string.append(messageString).append("\n");
			}
			string.appendHtml("</span>");
		}
	}

	public static Collection<String> getMessageStrings(Section<?> rootSection, Type type) {
		Map<Section<?>, Map<Message, Collection<Compiler>>> collectedMessages =
				getMessageSectionsOfSubtree(rootSection, type);

		Collection<String> messages = new LinkedHashSet<String>();
		for (Section<?> section : collectedMessages.keySet()) {
			Map<Message, Collection<Compiler>> compilerForMessage = collectedMessages.get(section);
			for (Entry<Message, Collection<Compiler>> entry : compilerForMessage.entrySet()) {
				Message msg = entry.getKey();
				String message = KnowWEUtils.maskJSPWikiMarkup(msg.getVerbalization());
				// if we have multiple other article compilers
				Collection<Compiler> compilers = entry.getValue();
				boolean multiCompiled = compilers.size() > 1;
				compilers.remove(null);
				if (multiCompiled && !compilers.isEmpty()) {
					message += " (compiled in ";
					message += Strings.concat(", ", compilers);
					message += ")";
				}
				messages.add(message);
			}
		}
		return messages;
	}

	public static void renderMessagesOfType(Message.Type type, Collection<Message> messages, RenderResult string) {
		string.appendHtml("<span class='" + type.toString().toLowerCase()
				+ "' style='white-space: pre-wrap;'>");
		for (Message msg : messages) {
			string.append(KnowWEUtils.maskJSPWikiMarkup(msg.getVerbalization()));
			string.append("\n");
		}
		string.appendHtml("</span>");
	}

	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
		List<Section<?>> subsecs = section.getChildren();
		renderContentSections(subsecs, isListAnnotations(), user, string);
	}

	public static void renderContentSections(List<Section<?>> subSections, boolean listAnnotations, UserContext user, RenderResult result) {
		if (subSections.size() == 0) return;
		// find two sections that can possibly be skipped
		Section<?> first = subSections.get(0);
		Section<?> last = subSections.get(subSections.size() - 1);
		// only skip them if they are plain text and empty
		if (!isEmptyPlainText(first)) first = null;
		if (!isEmptyPlainText(last)) last = null;
		boolean listOpen = false;
		for (Section<?> subsec : subSections) {
			if (subsec == first) continue;
			if (subsec == last) continue;
			if (listAnnotations && subsec.get() instanceof AnnotationType) {
				if (!listOpen) {
					result.appendHtml("\n\n<ul class='defaultMarkupAnnotations'>");
					listOpen = true;
				}
				result.appendHtml("<li>");
				subsec.get().getRenderer().render(subsec, user, result);
				result.appendHtml("</li>");
			}
			else {
				subsec.get().getRenderer().render(subsec, user, result);
			}
		}
		if (listOpen) {
			result.appendHtml("</ul>");
		}
	}

	private static boolean isEmptyPlainText(Section<?> section) {
		return section.get() instanceof PlainText;
	}

	public void renderDefaultMarkupStyled(String title,
										  String content,
										  String sectionID,
										  String cssClassName,
										  ToolSet tools,
										  UserContext user,
										  RenderResult string) {

		String cssClass = "defaultMarkupFrame";
		if (cssClassName != null) cssClass += " " + cssClassName;
		string.appendHtml("<div id=\"" + sectionID + "\" class='" + cssClass + "'>\n");

		appendHeader(title, sectionID, tools, user, string);

		// render pre-formatted box
		String style = "";
		if (!isPreFormattedStyle()) {
			style = " style='white-space: normal;'";
		}
		string.appendHtml("<div id=\"box_" + sectionID
				+ "\" class='defaultMarkup'>");
		string.appendHtml("<div id=\"content_" + sectionID
				+ "\" class='markupText'" + style + ">");

		// render content
		// Returns are replaced to avoid JSPWiki to render <p> </p>, do not edit
		// the following lines!
		if (isPreFormattedStyle()) {
			String newLine = "(\r?\n){2}";
			String newLineReplacement = new RenderResult(string).appendHtml(
					"<span>\n</span><span>\n</span>").toStringRaw();
			string.append(content.replaceAll(newLine, newLineReplacement));
		}
		else {
			string.append("\n").append(content);
		}

		// and close the box(es)
		string.appendHtml("</div>"); // class=markupText
		string.appendHtml("</div>"); // class=defaultMarkup
		string.appendHtml("</div>");

	}

	protected void appendHeader(String title,
								String sectionID,
								ToolSet tools,
								UserContext user,
								RenderResult temp) {

		String renderModerClass = renderMode == ToolsRenderMode.TOOLBAR
				? " headerToolbar"
				: " headerMenu";
		String openingDiv = "<div id='header_" + sectionID + "' class='markupHeaderFrame"
				+ renderModerClass + "'>";

		temp.appendHtml(openingDiv);

		temp.appendHtml("<div class='markupHeader'>");
		temp.append(title);

		if (renderMode == ToolsRenderMode.MENU) {
			temp.appendHtml("<span class='markupMenuIndicator'></span>");
		}
		else if (renderMode == ToolsRenderMode.TOOLBAR) {
			appendToolbar(tools, user, temp);
		}

		temp.appendHtml("</div>"); // class=markupHeader

		if (renderMode == ToolsRenderMode.MENU) {
			appendMenu(tools, sectionID, user, temp);
		}

		temp.appendHtml("</div>"); // class=markupHeaderFrame

	}

	protected void appendToolbar(ToolSet tools, UserContext user, RenderResult result) {
		result.appendHtmlTag("div", "class", "markupTools");
		for (Tool tool : tools) {
			result.appendHtmlTag("div", "class", tool.getClass().getSimpleName());
			result.appendHtmlTag("a", false, ToolUtils.getActionAttributeName(tool), ToolUtils.getActionAttributeValue(tool));
			result.appendHtmlElement("img", "", "title", tool.getDescription(), "src", tool.getIconPath());
			result.appendHtmlTag("/a");
			result.appendHtmlTag("/div");
		}
		result.appendHtmlTag("/div");
	}

	public void appendMenu(ToolSet tools, String id, UserContext user, RenderResult result) {

		if (!tools.hasTools()) return;

		Map<String, Map<String, List<Tool>>> groupedTools = ToolUtils.groupTools(tools);

		result.appendHtml("<div id='menu_" + id + "' class='markupMenu'>");

		List<String> levelOneCategories = new ArrayList<String>(groupedTools.keySet());
		Collections.sort(levelOneCategories);

		for (String category : levelOneCategories) {
			Map<String, List<Tool>> levelTwoTools = groupedTools.get(category);

			List<String> levelTwoCategories = new ArrayList<String>(levelTwoTools.keySet());
			Collections.sort(levelTwoCategories);

			for (String subcategory : levelTwoCategories) {
				List<Tool> subTools = groupedTools.get(category).get(subcategory);
				if (Tool.CATEGORY_INLINE.equals(subcategory)) {
					appendAsInlineTools(subTools, result);
				}
				else {
					for (Tool tool : subTools) {
						appendToolAsMenuItem(tool, result);
					}
				}
			}

			if (!category.equals(levelOneCategories.get(levelOneCategories.size() - 1))) {
				result.appendHtml("<span class=\"markupMenuDivider\">&nbsp;</span>");
			}
		}

		result.appendHtml("</div>");
	}

	private void appendAsInlineTools(List<Tool> subTools, RenderResult result) {
		result.appendHtmlTag("div", "class", "InlineTool");
		result.appendHtmlTag("div", "class", "markupMenuItem");
		for (Tool tool : subTools) {
			appendToolAsInlineItem(tool, result);
		}
		result.appendHtmlTag("/div");
		result.appendHtmlTag("/div");
	}

	protected void appendToolAsInlineItem(Tool tool, RenderResult result) {
		String clazz = Strings.isBlank(tool.getAction()) ? "markupMenuInlineSpacer" : "markupMenuInlineItem";
		result.appendHtmlTag("a", false, "class", clazz,
				"title", Strings.encodeHtml(tool.getDescription()),
				ToolUtils.getActionAttributeName(tool),
				ToolUtils.getActionAttributeValue(tool));
		result.appendHtmlElement("span", tool.getTitle());
		result.appendHtmlTag("/a");
	}

	protected void appendToolAsMenuItem(Tool tool, RenderResult result) {
		String icon = tool.getIconPath();
		boolean hasIcon = icon != null && !icon.trim().isEmpty();
		result.appendHtmlTag("div", "class", tool.getClass().getSimpleName());
		result.appendHtmlTag("div", "class", "markupMenuItem");
		result.appendHtmlTag("a", false, "class", "markupMenuItem",
				"title", Strings.encodeHtml(tool.getDescription()),
				ToolUtils.getActionAttributeName(tool),
				ToolUtils.getActionAttributeValue(tool));
		if (hasIcon) {
			result.appendHtmlElement("img", "", "src", icon);
			result.append(" ");
		}
		result.appendHtmlElement("span", tool.getTitle());
		result.appendHtmlTag("/a");
		result.appendHtmlTag("/div");
		result.appendHtmlTag("/div");
	}

	public ToolsRenderMode getRenderMode() {
		return renderMode;
	}

	public void setRenderMode(ToolsRenderMode renderMode) {
		this.renderMode = renderMode;
	}

	public boolean isPreFormattedStyle() {
		return preFormattedStyle;
	}

	public void setPreFormattedStyle(boolean preFormattedStyle) {
		this.preFormattedStyle = preFormattedStyle;
	}

	/**
	 * Returns if the annotations shall be rendered as an unordered list, instead of plain rendering.
	 *
	 * @return if the annotation-as-list mode is enabled
	 * @created 28.01.2014
	 */
	public boolean isListAnnotations() {
		return listAnnotations;
	}

	/**
	 * Specified if the annotations shall be rendered as an unordered list. The default behavior for this renderer is
	 * "false", rendering annotations as their plain section content using their specific renderer(s).
	 *
	 * @param listAnnotations if the annotation-as-list mode shall be enabled (true) or disabled (false)
	 * @created 28.01.2014
	 */
	public void setListAnnotations(boolean listAnnotations) {
		this.listAnnotations = listAnnotations;
	}

}