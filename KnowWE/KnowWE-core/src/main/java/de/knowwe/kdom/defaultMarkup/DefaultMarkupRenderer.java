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

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
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

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder buffer) {
		String id = section.getID();
		Tool[] tools = ToolUtils.getTools(section, user);

		// render markup title
		StringBuilder markupTitle = new StringBuilder();
		renderTitle(section, user, markupTitle);

		// create content
		StringBuilder content = new StringBuilder();

		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(section);
		content.append(Strings.maskHTML("<a name='" + anchorName + "'></a>"));

		// render messages and content
		renderMessages(section, content);
		renderContents(section, user, content);

		String cssClassName = "type_" + section.get().getName();

		renderDefaultMarkupStyled(
				markupTitle.toString(), content.toString(),
				id, cssClassName, tools, user, buffer);
	}

	protected void renderTitle(Section<?> section, UserContext user, StringBuilder string) {
		String icon = getTitleIcon(section, user);
		String title = getTitleName(section, user);
		string.append(renderTitle(icon, title));
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
			result = "<img class='markupIcon' src='" + iconPath + "'></img> ";
		}

		// render heading
		result += "<span>" + title + "</span>";
		return result;
	}

	public void renderMessages(Section<?> section, StringBuilder string) {

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

	private void renderCompileWarning(Section<?> section, StringBuilder string) {
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
			StringBuilder string,
			Message.Type... types) {

		for (Type type : types) {
			Collection<String> messages = getMessageStrings(rootSection, type);

			// only if there are such messages
			if (messages.isEmpty()) continue;
			String clazz = type.toString().toLowerCase();
			if (messages.size() == 1) {
				clazz += " singleLine";
			}
			string.append(Strings.maskHTML("<span class='" + clazz
					+ "' style='white-space: pre-wrap;'>"));
			for (String messageString : messages) {
				string.append(messageString).append("\n");
			}
			string.append(Strings.maskHTML("</span>\n"));
		}
	}

	public static Collection<String> getMessageStrings(Section<?> rootSection, Type type) {
		Map<Section<?>, Map<Message, Collection<Article>>> collectedMessages =
				getMessageSectionsOfSubtree(rootSection, type);

		Collection<String> messages = new LinkedHashSet<String>();
		for (Section<?> section : collectedMessages.keySet()) {
			Map<Message, Collection<Article>> compilerForMessage = collectedMessages.get(section);
			for (Message msg : compilerForMessage.keySet()) {
				String message = Strings.maskJSPWikiMarkup(msg.getVerbalization());
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

	public static void renderMessagesOfType(Message.Type type, Collection<Message> messages, StringBuilder string) {
		string.append(Strings.maskHTML("<span class='" + type.toString().toLowerCase()
				+ "' style='white-space: pre-wrap;'>"));
		for (Message msg : messages) {
			string.append(Strings.maskJSPWikiMarkup(msg.getVerbalization()));
			string.append("\n");
		}
		string.append(Strings.maskHTML("</span>"));
	}

	protected void renderContents(Section<?> section, UserContext user, StringBuilder string) {
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
			StringBuilder string) {

		String cssClass = "defaultMarkupFrame";
		if (cssClassName != null) cssClass += " " + cssClassName;
		string.append(Strings.maskHTML("<div id=\"" + sectionID + "\" class='" + cssClass
				+ "'>\n"));

		renderHeader(title, sectionID, tools, user, string);

		// render pre-formatted box
		string.append(Strings.maskHTML("<div id=\"box_" + sectionID
				+ "\" class='defaultMarkup'>"));
		string.append(Strings.maskHTML("<div id=\"content_" + sectionID
				+ "\" class='markupText'>"));

		// render content
		// Returns are replaced to avoid JSPWiki to render <p> </p>, do not edit
		// the following line!
		string.append(content.replaceAll("(\r?\n){2}",
				Strings.maskHTML("<span>\n</span><span>\n</span>")));

		// and close the box(es)
		string.append(Strings.maskHTML("</div>")); // class=markupText
		string.append(Strings.maskHTML("</div>")); // class=defaultMarkup
		string.append(Strings.maskHTML("</div>"));

		if (renderMode == ToolsRenderMode.MENU) {
			string.append(Strings.maskHTML(renderTitleAnimation(sectionID)));
			string.append(Strings.maskHTML(renderMenuAnimation(sectionID)));
		}

	}

	protected void renderHeader(String title,
			String sectionID,
			Tool[] tools,
			UserContext user,
			StringBuilder string) {

		StringBuilder temp = new StringBuilder();

		String renderModerClass = renderMode == ToolsRenderMode.TOOLBAR
				? " headerToolbar"
				: " headerMenu";
		String openingDiv = "<div id='header_" + sectionID + "' class='markupHeaderFrame"
				+ renderModerClass + "'>";

		temp.append(openingDiv);

		temp.append("<div class='markupHeader'>");
		temp.append(title);

		if (renderMode == ToolsRenderMode.MENU) {
			temp.append("<span class='markupMenuIndicator' />");
		}
		else if (renderMode == ToolsRenderMode.TOOLBAR) {
			temp.append(renderToolbar(tools, user));
		}

		temp.append("</div>"); // class=markupHeader

		if (renderMode == ToolsRenderMode.MENU) {
			temp.append(renderMenu(tools, sectionID, user));
		}

		temp.append("</div>"); // class=markupHeaderFrame

		string.append(Strings.maskHTML(temp.toString()));

	}

	protected String renderToolbar(Tool[] tools, UserContext user) {
		StringBuilder toolbarHtml = new StringBuilder("<div class='markupTools'> ");
		for (Tool tool : tools) {
			toolbarHtml.append("<div class=\""
					+ tool.getClass().getSimpleName() + "\" >" +
					"<a href=\"javascript:" + tool.getJSAction() + ";undefined;\">" +
					"<img " +
					"title=\"" + tool.getDescription() + "\" " +
					"src=\"" + tool.getIconPath() + "\"></img>" +
					"</a></div>");
		}
		toolbarHtml.append("</div>");
		return toolbarHtml.toString();
	}

	public String renderMenu(Tool[] tools, String id, UserContext user) {

		if (tools == null || tools.length == 0) return "";

		Map<String, Map<String, List<Tool>>> groupedTools = ToolUtils.groupTools(tools);

		StringBuffer menuHtml = new StringBuffer("<div id='menu_" + id + "' class='markupMenu'>");

		List<String> levelOneCategories = new ArrayList<String>(groupedTools.keySet());
		Collections.sort(levelOneCategories);

		for (String category : levelOneCategories) {
			Map<String, List<Tool>> levelTwoTools = groupedTools.get(category);

			List<String> levelTwoCategories = new ArrayList<String>(levelTwoTools.keySet());
			Collections.sort(levelTwoCategories);

			for (String subcategory : levelTwoCategories) {
				for (Tool t : groupedTools.get(category).get(subcategory)) {
					menuHtml.append(renderToolAsMenuItem(t));
				}
			}

			if (!category.equals(levelOneCategories.get(levelOneCategories.size() - 1))) {
				menuHtml.append("<span class=\"markupMenuDivider\">&nbsp;</span>");
			}
		}

		return menuHtml.append("</div>").toString();
	}

	protected String renderToolAsMenuItem(Tool tool) {
		String icon = tool.getIconPath();
		String jsAction = tool.getJSAction();
		boolean hasIcon = icon != null && !icon.trim().isEmpty();

		return "<div class=\""
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
				" title=\"" + tool.getDescription() + "\">" +
				(hasIcon ? ("<img src=\"" + icon + "\"></img>") : "") +
				" <span>" + tool.getTitle() + "</span>" +
				"</" + (jsAction == null ? "span" : "a") + ">" +
				"</div></div>";
	}

	protected String renderTitleAnimation(String id) {
		return "<script>\n" +
				"var makeTitleFx = function() {\n" +
				"var a=$('header_" + id + "');\n" +
				"var b=a.effect(\"opacity\",{wait:false,duration:200}).set(0.3);\n" +
				"var d=a.effect(\"max-width\",{wait:false}).set(35);\n" +
				"a.set({href:\"#\",events:{" +
				"mouseout:function(){b.start(0.3);d.start(35);a.style['z-index']=1000;}," +
				"click:function(){b.start(1);d.start(250);a.style['z-index']=1500;}," +
				"mouseover:function(){b.start(1);d.start(250);a.style['z-index']=1500;}}});" +
				"};" +
				"makeTitleFx();" +
				"</script>\n";
	}

	protected String renderMenuAnimation(String id) {
		return "<script>\n" +
				"var makeMenuFx = function() {\n" +
				"var a=$('header_" + id + "'),c=$('menu_" + id + "');\n" +
				"if(!a||!c){return;}\n" +
				"var b=c.effect(\"opacity\",{wait:false}).set(0);\n" +
				"a.adopt(c).set({href:\"#\",events:{" +
				"mouseout:function(){b.start(0);}," +
				"click:function(){b.start(0.9);}," +
				"mouseover:function(){b.start(0.9);}}});" +
				"};" +
				"makeMenuFx();" +
				"</script>\n";
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