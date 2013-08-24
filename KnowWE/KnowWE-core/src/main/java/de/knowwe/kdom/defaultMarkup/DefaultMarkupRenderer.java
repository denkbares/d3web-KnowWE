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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
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
import de.knowwe.tools.ToolUtils;

public class DefaultMarkupRenderer implements Renderer {

	private final String iconPath;

	private ToolsRenderMode renderMode = ToolsRenderMode.MENU;

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
		Tool[] tools = ToolUtils.getTools(section, user);

		// render markup title
		RenderResult markupTitle = new RenderResult(buffer);
		renderTitle(section, user, markupTitle);

		// create content
		RenderResult content = new RenderResult(buffer);

		// add an anchor to enable direct link to the section
		KnowWEUtils.renderAnchor(section, content);

		// render messages and content
		renderMessages(section, content);
		renderProgress(section, user, content);
		renderContents(section, user, content);

		String cssClassName = "type_" + section.get().getName();

		renderDefaultMarkupStyled(
				markupTitle.toStringRaw(), content.toStringRaw(),
				id, cssClassName, tools, user, buffer);
	}

	protected void renderProgress(Section<?> section, UserContext user, RenderResult result) {
		ProgressRenderer.getInstance().render(section, user, result);
	}

	protected void renderTitle(Section<?> section, UserContext user, RenderResult string) {
		String icon = getTitleIcon(section, user);
		String title = getTitleName(section, user);
		string.appendHtml(renderTitle(icon, title));
	}

	protected String getTitleIcon(Section<?> section, UserContext user) {
		return this.iconPath;
	}

	protected String getTitleName(Section<?> section, UserContext user) {
		return section.get().getName();
	}

	protected String renderTitle(String iconPath, String title) {
		String result = "";
		// render icon
		if (iconPath != null) {
			result = "<img class='markupIcon' src='" + iconPath + "' /> ";
		}

		// render heading
		result += "<span>" + title + "</span>";
		return result;
	}

	public void renderMessages(Section<?> section, RenderResult string) {

		renderCompileWarning(section, string);
		renderMessageBlock(section, string, Message.Type.ERROR, Message.Type.WARNING);
	}

	private static Map<Section<?>, Map<Message, Collection<Article>>> getMessageSectionsOfSubtree(Section<?> rootSection, Type messageType) {
		Map<Section<?>, Map<Message, Collection<Article>>> collectedMessages = new HashMap<Section<?>, Map<Message, Collection<Article>>>();
		for (Section<?> subTreeSection : Sections.getSubtreePreOrder(rootSection)) {
			Collection<Article> compilers = new TreeSet<Article>(new ArticleComparator());
			compilers.addAll(
					KnowWEUtils.getCompilingArticleObjects(subTreeSection));
			compilers.add(rootSection.getArticle());
			compilers.add(null);
			Map<Message, Collection<Article>> compilersForMessage = new HashMap<Message, Collection<Article>>();
			for (Article compiler : compilers) {
				Collection<Message> messages = Messages.getMessages(compiler, subTreeSection,
						messageType);
				for (Message message : messages) {
					Collection<Article> messageCompilers = compilersForMessage.get(message);
					if (messageCompilers == null) {
						messageCompilers = new LinkedList<Article>();
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

	private void renderCompileWarning(Section<?> section, RenderResult string) {
		// add warning if section is not compiled
		if (!section.get().isIgnoringPackageCompile()) {

			Set<String> compilingArticles = Environment.getInstance().getPackageManager(
					section.getWeb()).getCompilingArticles(section);
			if (compilingArticles.isEmpty()) {
				Set<String> packageNames = section.getPackageNames();
				String warningString;
				if (packageNames.size() == 1) {
					warningString = "This section is registered to the package '"
							+ packageNames.iterator().next()
							+ "' which is not compiled in any article.";
				}
				else if (packageNames.size() > 1) {
					warningString = "This section is registered to the packages "
							+ packageNames.toString() + " which are not compiled in any article.";
				}
				else {
					warningString = "This section is not registered to any package and therefore "
							+ "not compiled in any article.";
				}
				renderMessagesOfType(Type.WARNING,
						Messages.asList(Messages.warning(warningString)),
						string);
			}
		}
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
			string.appendHtml("</span>\n");
		}
	}

	public static Collection<String> getMessageStrings(Section<?> rootSection, Type type) {
		Map<Section<?>, Map<Message, Collection<Article>>> collectedMessages =
				getMessageSectionsOfSubtree(rootSection, type);

		Collection<String> messages = new LinkedHashSet<String>();
		for (Section<?> section : collectedMessages.keySet()) {
			Map<Message, Collection<Article>> compilerForMessage = collectedMessages.get(section);
			for (Message msg : compilerForMessage.keySet()) {
				String message = KnowWEUtils.maskJSPWikiMarkup(msg.getVerbalization());
				// if we have multiple other article compilers
				boolean multiCompiled = KnowWEUtils.getCompilingArticleObjects(section).size() > 1;
				Collection<Article> compilers = compilerForMessage.get(msg);
				compilers.remove(null);
				if (multiCompiled && !compilers.isEmpty()) {
					message += " (compiled in ";
					boolean first = true;
					for (Article article : compilers) {
						if (!first) message += ", ";
						first = false;
						message += "[" + article.getTitle() + "]";
					}
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
		Section<?> first = subsecs.get(0);
		Section<?> last = subsecs.get(subsecs.size() - 1);
		for (Section<?> subsec : subsecs) {
			if (subsec == first && subsec.get() instanceof PlainText) {
				continue;
			}
			if (subsec == last && subsec.get() instanceof PlainText) {
				continue;
			}
			subsec.get().getRenderer().render(subsec, user, string);
		}
	}

	public void renderDefaultMarkupStyled(String title,
			String content,
			String sectionID,
			String cssClassName,
			Tool[] tools,
			UserContext user,
			RenderResult string) {

		String cssClass = "defaultMarkupFrame";
		if (cssClassName != null) cssClass += " " + cssClassName;
		string.appendHtml("<div id=\"" + sectionID + "\" class='" + cssClass + "'>\n");

		appendHeader(title, sectionID, tools, user, string);

		// render pre-formatted box
		string.appendHtml("<div id=\"box_" + sectionID
				+ "\" class='defaultMarkup'>");
		string.appendHtml("<div id=\"content_" + sectionID
				+ "\" class='markupText'>");

		// render content
		// Returns are replaced to avoid JSPWiki to render <p> </p>, do not edit
		// the following lines!
		String newLine = "(\r?\n){2}";
		String newLineReplacement = new RenderResult(string).appendHtml(
				"<span>\n</span><span>\n</span>").toStringRaw();
		string.append(content.replaceAll(newLine, newLineReplacement));

		// and close the box(es)
		string.appendHtml("</div>"); // class=markupText
		string.appendHtml("</div>"); // class=defaultMarkup
		string.appendHtml("</div>");

	}

	protected void appendHeader(String title,
			String sectionID,
			Tool[] tools,
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

	protected void appendToolbar(Tool[] tools, UserContext user, RenderResult result) {
		result.appendHtml("<div class='markupTools'> ");
		for (Tool tool : tools) {
			result.appendHtml("<div class=\""
					+ tool.getClass().getSimpleName() + "\" >" +
					"<a href=\"javascript:" + tool.getJSAction() + ";undefined;\">" +
					"<img " +
					"title=\"" + tool.getDescription() + "\" " +
					"src=\"" + tool.getIconPath() + "\" \n/>" +
					"</a></div>");
		}
		result.appendHtml("</div>");
	}

	public void appendMenu(Tool[] tools, String id, UserContext user, RenderResult result) {

		if (tools == null || tools.length == 0) return;

		Map<String, Map<String, List<Tool>>> groupedTools = ToolUtils.groupTools(tools);

		result.appendHtml("<div id='menu_" + id + "' class='markupMenu'>");

		List<String> levelOneCategories = new ArrayList<String>(groupedTools.keySet());
		Collections.sort(levelOneCategories);

		for (String category : levelOneCategories) {
			Map<String, List<Tool>> levelTwoTools = groupedTools.get(category);

			List<String> levelTwoCategories = new ArrayList<String>(levelTwoTools.keySet());
			Collections.sort(levelTwoCategories);

			for (String subcategory : levelTwoCategories) {
				for (Tool t : groupedTools.get(category).get(subcategory)) {
					appendToolAsMenuItem(t, result);
				}
			}

			if (!category.equals(levelOneCategories.get(levelOneCategories.size() - 1))) {
				result.appendHtml("<span class=\"markupMenuDivider\">&nbsp;</span>");
			}
		}

		result.appendHtml("</div>");
	}

	protected void appendToolAsMenuItem(Tool tool, RenderResult result) {
		String icon = tool.getIconPath();
		String jsAction = tool.getJSAction();
		boolean hasIcon = icon != null && !icon.trim().isEmpty();

		result.appendHtml("<div class=\""
				+ tool.getClass().getSimpleName()
				+ "\" >"
				+ "<div class=\"markupMenuItem\">"
				+ "<"
				+ (jsAction == null ? "span" : "a")
				+ " class=\"markupMenuItem\""
				+ (jsAction != null
						? " href=\"javascript:" + tool.getJSAction()
								+ ";hideToolsPopupMenu();undefined;\""
						: "") +
				" title=\"" + Strings.encodeHtml(tool.getDescription()) + "\">" +
				(hasIcon ? ("<img src=\"" + icon + "\" />") : "") +
				" <span>" + tool.getTitle() + "</span>" +
				"</" + (jsAction == null ? "span" : "a") + ">" +
				"</div></div>");
	}

	public ToolsRenderMode getRenderMode() {
		return renderMode;
	}

	public void setRenderMode(ToolsRenderMode renderMode) {
		this.renderMode = renderMode;
	}

	private static class ArticleComparator implements Comparator<Article> {

		@Override
		public int compare(Article o1, Article o2) {
			if (o1 == null && o2 != null) return -1;
			if (o1 != null && o2 == null) return 1;
			if (o1 == null && o2 == null) return 0;
			return o1.getTitle().compareTo(o2.getTitle());
		}

	}

}