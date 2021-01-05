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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.NamedCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.ScriptManager;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.kdom.Types;
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
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

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

	private static boolean isMultiCompiled(Collection<Compiler> compilers, Section<?> rootSection) {
		compilers.remove(null);
		// only consider package compilers
		compilers.removeIf(c -> !(c instanceof PackageCompiler));
		if (compilers.isEmpty()) return false;
		if (compilers.size() > 1) return true;

		// if only one compiler produced the message but multiple of these compilers compile the section,
		// also consider as multi compiled
		return Compilers.getCompilers(rootSection, compilers.iterator().next().getClass()).size() > 1;
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

	@SuppressWarnings("UnusedParameters")
	protected String getTitleIcon(Section<?> section, UserContext user) {
		return this.iconPath;
	}

	protected String getTitleName(Section<?> section, UserContext user) {
		return section.get().getName();
	}

	public void renderMessages(Section<?> section, RenderResult string, UserContext context) {
		renderMessageBlock(section, string, context, Message.Type.ERROR, Message.Type.WARNING);
	}

	private static Map<Section<?>, Map<Message, Collection<Compiler>>> getMessageSectionsOfSubtree(Section<?> rootSection, Type messageType) {
		Map<Section<?>, Map<Message, Collection<Compiler>>> collectedMessages = new LinkedHashMap<>();
		Collection<Compiler> compilers = new ArrayList<>(Compilers.getCompilers(rootSection, Compiler.class));
		compilers.add(null);
		for (Section<?> subTreeSection : Sections.successors(rootSection)) {
			Map<Message, Collection<Compiler>> compilersForMessage = new LinkedHashMap<>();
			for (Compiler compiler : compilers) {
				Collection<Message> messages = Messages.getMessages(compiler, subTreeSection, messageType);
				for (Message message : messages) {
					Collection<Compiler> messageCompilers = compilersForMessage.computeIfAbsent(message, k -> new HashSet<>());
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
										  UserContext context,
										  Message.Type... types) {

		for (Type type : types) {
			Collection<String> messages = getMessageStrings(rootSection, type, context);
			renderMessageBlock(string, type, messages);
		}
	}

	public static void renderMessageBlock(RenderResult out, Collection<Message> messages) {
		MultiMap<Type, String> groups = messages.stream().collect(MultiMaps.toMultiMap(
				Message::getType, Message::getVerbalization, DefaultMultiMap::newLinked));
		groups.toMap().forEach((type, msgs) -> renderMessageBlock(out, type, msgs));
	}

	private static void renderMessageBlock(RenderResult out, Type type, Collection<String> messages) {
		// only if there are such messages
		if (messages.isEmpty()) return;
		String clazz = type.toString().toLowerCase();
		if (messages.size() == 1) {
			clazz += " singleLine";
		}
		out.appendHtml("<span class='" + clazz
				+ "' style='white-space: pre-wrap;'>");
		for (String messageString : messages) {
			out.append(messageString).append("\n");
		}
		out.appendHtml("</span>");
	}

	public static Collection<String> getMessageStrings(Section<?> rootSection, Type type, @Nullable UserContext context) {
		Map<Section<?>, Map<Message, Collection<Compiler>>> collectedMessages =
				getMessageSectionsOfSubtree(rootSection, type);

		Collection<String> messages = new LinkedHashSet<>();
		for (Section<?> section : collectedMessages.keySet()) {
			Map<Message, Collection<Compiler>> compilerForMessage = collectedMessages.get(section);
			for (Entry<Message, Collection<Compiler>> entry : compilerForMessage.entrySet()) {
				Message msg = entry.getKey();
				String verbalization = msg.getVerbalization();
				String message = msg.getDisplay() == Message.Display.PLAIN ? KnowWEUtils.maskJSPWikiMarkup(verbalization) : verbalization;
				// if we have multiple other article compilers
				Collection<Compiler> compilers = entry.getValue();
				if (isMultiCompiled(compilers, rootSection)) {
					message += compilers.stream().map(DefaultMarkupRenderer::renderName).distinct()
							.collect(Collectors.joining(", ", " (compiled in ", ")"));
				}
				messages.add(message);
			}
		}

		if (type == Type.WARNING) {
			checkNotCompiledWarning(rootSection, messages, context);
		}

		return messages;
	}

	private static String renderName(Compiler compiler) {
		if (compiler instanceof NamedCompiler) {
			final String name = ((NamedCompiler) compiler).getName();
			if (compiler instanceof PackageCompiler) {
				return "[" + name + "|" + KnowWEUtils.getWikiLink(((PackageCompiler) compiler).getCompileSection()) + "]";
			}
			else {
				return name;
			}
		}
		else {
			return compiler.toString();
		}
	}

	/**
	 * We create an additional warning in case this section has package compile scripts but no compiler compiling them
	 */
	private static void checkNotCompiledWarning(Section<?> rootSection, Collection<String> messages, @Nullable UserContext context) {
		// if there is a package annotation, a message will be produced there, no need to produce another one
		if (DefaultMarkupType.getAnnotation(rootSection, PackageManager.PACKAGE_ATTRIBUTE_NAME) != null) return;
		if (context != null && context.isRenderingPreview()) return;
		// happens for temp articles, e.g. while viewing older version
		if (rootSection.getArticle().getArticleManager() == null) return;

		List<ScriptManager<? extends Compiler>> unCompiledScriptManagersWithScriptsForTypeTree =
				CompilerManager.getScriptManagers()
						.stream()
						// ignore markups that don't have package compile scripts
						.filter(sm -> PackageCompiler.class.isAssignableFrom(sm.getCompilerClass()))
						// check if the script manager has script for the type of this section or any sub type
						// get all remaining managers for which there is currently no compiler
						.filter(sm -> sm.hasScriptsForSubtree(rootSection.get())
								&& Compilers.getCompiler(rootSection, sm.getCompilerClass()) == null)
						.collect(Collectors.toList());

		// check that the found unused compiled scripts belong to types of sections that are actually in the current sub-KDOM
		for (ScriptManager<? extends Compiler> scriptManager : unCompiledScriptManagersWithScriptsForTypeTree) {
			for (de.knowwe.core.kdom.Type type : Types.getAllChildrenTypesRecursive(rootSection.get())) {
				//noinspection rawtypes
				Map map = scriptManager.getScripts(type);
				if (map.isEmpty()) continue;
				if ($(rootSection).successor(type.getClass()).isEmpty()) continue;
				Class<? extends Compiler> compilerClass = scriptManager.getCompilerClass();
				if (ScriptManager.IgnoreNotCompiledSections.class.isAssignableFrom(compilerClass)) continue;
				messages.add("This section has " + compilerClass.getSimpleName() + " knowledge, "
						+ "but does not belong to package compiled by one.");
			}
		}
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		ToolSet tools = getTools(section, user);

		// add an anchor to enable direct link to the section
		RenderResult markupTitle = new RenderResult(result);
		KnowWEUtils.renderAnchor(section, markupTitle);

		// render markup title
		renderTitle(section, user, markupTitle);

		// create content
		RenderResult content = new RenderResult(result);

		// render messages and content
		if (section.get() instanceof DefaultMarkupType) {
			// only render messages if this is a DefaultMarkupType section (can be other e.g. in %%Include)
			renderMessages(section, content, user);
		}
		renderProgress(section, user, tools, content);
		int validLength = content.length();
		try {
			renderContentsAndAnnotations(section, user, content);
		}
		catch (Throwable e) {
			content.delete(validLength, content.length());
			content.appendHtmlElement("span", "Error while rendering content, if the problem persists, "
					+ "please contact your administrator.\n"
					+ Strings.getStackTrace(e, 10) + "\n\t...", "class", "error", "style", "white-space: pre");
			Log.severe("Exception while rendering content of " + section.get().getName(), e);
		}

		renderDefaultMarkupStyled(
				markupTitle.toStringRaw(), content.toStringRaw(),
				section, tools, user, result);
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

	public static void renderMessageOfType(RenderResult result, Type type, String message) {
		renderMessagesOfType(type, Messages.asList(new Message(type, message)), result);
	}

	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
		renderContentsAndAnnotations(Sections.cast(section, DefaultMarkupType.class), section.getChildren(), user, result);
	}

	public void renderContentsAndAnnotations(Section<? extends DefaultMarkupType> markupSection, List<Section<?>> subSections, UserContext user, RenderResult result) {
		renderContents(markupSection, $(subSections).filter(ContentType.class).asList(), user, result);
		renderAnnotations(markupSection, $(subSections).filter(AnnotationType.class).asList(), user, result);
	}

	protected void renderContents(Section<? extends DefaultMarkupType> markupSection, List<Section<ContentType>> contentSections, UserContext user, RenderResult result) {
		for (Section<ContentType> contentSection : contentSections) {
			result.append(contentSection, user);
		}
	}

	protected void renderAnnotations(Section<? extends DefaultMarkupType> markupSection, List<Section<AnnotationType>> annotations, UserContext user, RenderResult result, boolean renderPackagesAndCompilers) {
		if (listAnnotations) {
			result.append("\n\n");
			renderAnnotations(annotations, user, result, "ul", "li");
		}
		else {
			renderAnnotations(annotations, user, result, "div", "span");
		}

		// render package rules and compiler names if necessary
		if (renderPackagesAndCompilers && shouldRenderPackagesAndCompilers(markupSection)) {
			if (DefaultMarkupType.getAnnotation(markupSection, PackageManager.PACKAGE_ATTRIBUTE_NAME) == null) {
				renderPackages(markupSection, result, user);
			}
			renderCompilers(markupSection, result);
		}
	}

	protected void renderAnnotations(Section<? extends DefaultMarkupType> markupSection, List<Section<AnnotationType>> annotations, UserContext user, RenderResult result) {
		renderAnnotations(markupSection, annotations, user, result, true);
	}

	protected void renderAnnotations(List<Section<AnnotationType>> annotations, UserContext user, RenderResult result, String parentTag, String elementTag) {
		result.appendHtmlTag(parentTag, "class", "markupAnnotations", "style", "white-space:normal");
		for (Section<AnnotationType> annotation : annotations) {
			result.appendHtmlTag(elementTag, "class", "markupAnnotation", "data-name", annotation.get().getName());
			result.append(annotation, user);
			result.appendHtmlTag("/" + elementTag);
		}
		result.appendHtmlTag("/" + parentTag);
	}

	/**
	 * Check several parameters to decide whether packages and compilers should be rendered for every markup
	 *
	 * @param markupSection ArticleManager
	 * @return true when there are more than one compiler of every class, otherwise false
	 */
	private boolean shouldRenderPackagesAndCompilers(Section<? extends DefaultMarkupType> markupSection) {

		@NotNull Collection<PackageCompiler> compilers = Compilers.getCompilers(markupSection, PackageCompiler.class);
		// if not compiled by a package compiler, no need to render anything
		if (compilers.isEmpty()) return false;

		// if exactly one package compiler, check whether the sections of the compiler are distributed over more than 3 articles
		if (compilers.size() == 1) {
			PackageCompiler compiler = compilers.iterator().next();
			int threshold = 3;
			List<String> articleTitles = compiler.getPackageManager()
					.getSectionsOfPackage(compiler.getCompileSection()
							.get()
							.getPackagesToCompile(compiler.getCompileSection()))
					.parallelStream().map(Section::getTitle)
					.distinct()
					.limit(threshold + 1)
					.collect(Collectors.toList());
			return articleTitles.size() > threshold;
		}

		// if more than one package compiler, check whether there are several instances of the same compiler in the wiki
		else {
			return compilers.stream()
					.anyMatch(c -> Compilers.getCompilers(markupSection.getArticleManager(), c.getClass()).size() > 1);
		}
	}

	/**
	 * renders names of packages (i.e. PackageRules) for this section
	 *
	 * @param markupSection section for which packages should be rendered
	 * @param result        the render result
	 * @param user          user context
	 */
	private void renderPackages(Section<? extends DefaultMarkupType> markupSection, RenderResult result, UserContext user) {
		// get all package rules
		List<Section<PackageRule>> packageRules = $(markupSection.getArticle()).successor(DefaultMarkupType.class)
				.filter(m -> m.get().getName().equals("Package"))
				.successor(PackageRule.class)
				.asList();

		// render them
		if (!packageRules.isEmpty()) {
			result.appendHtml(Icon.PACKAGE.addClasses("packageOpacity").addStyle("margin-right: .3em").toHtml());
			for (int i = 0; i < packageRules.size(); i++) {
				Section<PackageRule> packageRule = packageRules.get(i);
				if (i > 0) { // separate them with a comma
					result.appendHtmlElement("span", ", ", "class", "packageOpacity");
				}
				packageRule.get().getRenderer().render(packageRule, user, result);
			}
		}
	}

	/**
	 * renders all compilers that are compiled in a given section
	 * Compilers that are present within a GroupingCompiler are not displayed here, but the GroupingCompiler is.
	 *
	 * @param markupSection section for which compilers should be rendered
	 * @param result        the render result
	 */
	private void renderCompilers(Section<? extends DefaultMarkupType> markupSection, RenderResult result) {
		// get all compilers
		Collection<PackageCompiler> compilers = Compilers.getCompilers(markupSection, PackageCompiler.class);

		// remove compilers from the collection that are also present in a GroupingCompiler
		//noinspection SuspiciousMethodCalls
		compilers.stream()
				.filter(c -> c instanceof GroupingCompiler)
				.map(c -> (GroupingCompiler) c)
				.collect(Collectors.toSet())
				.forEach(groupingCompiler -> compilers.removeAll(groupingCompiler.getChildCompilers()));

		// render them
		if (!compilers.isEmpty()) {
			result.appendHtmlTag("span", "style", "display: block");
			result.appendHtml(Icon.COMPILER.addClasses("packageOpacity").addStyle("margin-right: .3em").toHtml());
			result.appendHtmlElement("span",
					compilers.stream()
							.map(Compilers::getCompilerName)
							.collect(Collectors.joining(", ")), "class", "packageOpacity");
			result.appendHtmlTag("/span");
		}
	}

	public void renderDefaultMarkupStyled(String title,
										  String content,
										  Section<?> section,
										  ToolSet tools,
										  UserContext user,
										  RenderResult string) {

		Collection<PackageCompiler> compilers = Compilers.getCompilers(section, PackageCompiler.class);
		String defaultCompilerClass = getDefaultCompilerClass(section, user, compilers);

		String sectionID = section.getID();
		string.appendHtmlTag("div",
				"id", sectionID,
				"class", "defaultMarkupFrame toolMenuParent type_" + section.get().getName() + defaultCompilerClass,
				"data-name", section.get().getName());

		appendHeader(title, sectionID, tools, user, string);

		// add compiling compilers for debug purposes
		if (!compilers.isEmpty()) {
			string.appendHtmlElement("div",
					compilers.stream()
							.map(PackageCompiler::getName)
							.collect(Collectors.joining(";")),
					"class", "compiler-preview", "style", "display:none");
		}

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
			String cleanedContent = content.replaceAll(newLine, newLineReplacement);
			string.append(cleanedContent);
		}
		else {
			string.append("\n").append(content);
		}

		// and close the box(es)
		string.appendHtml("</div>"); // class=markupText
		string.appendHtml("</div>"); // class=defaultMarkup
		string.appendHtml("</div>");
	}

	@NotNull
	private String getDefaultCompilerClass(Section<?> section, UserContext user, Collection<PackageCompiler> compilers) {
		boolean isDefault = (compilers.isEmpty() && section.getPackageNames()
				.isEmpty()) || Compilers.getCompilers(user.getArticleManager(), GroupingCompiler.class).isEmpty();
		for (Compiler compiler : compilers) {
			isDefault = isDefault || Compilers.isDefaultCompiler(user, compiler);
		}
		return isDefault ? "" : " not-default-compiler";
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
		appendToolbarTools(tools, user, result);
		result.appendHtmlTag("/div");
	}

	protected void appendToolbarTools(ToolSet tools, UserContext user, RenderResult result) {
		for (Tool tool : tools) {
			result.appendHtmlTag("div", "class", "list-group " + tool.getClass().getSimpleName());
			result.appendHtmlTag("a", false, "class", "list-group-item", "title", tool.getDescription(), ToolUtils.getActionAttributeName(tool), ToolUtils
					.getActionAttributeValue(tool));
			result.appendHtml(tool.getIcon().fixWidth().toHtml());
			result.appendHtmlTag("/a");
			result.appendHtmlTag("/div");
		}
	}

	@SuppressWarnings("UnusedParameters")
	public void appendMenu(ToolSet tools, String id, UserContext user, RenderResult result) {

		if (!tools.hasTools()) return;

		Map<String, Map<String, List<Tool>>> groupedTools = ToolUtils.groupTools(tools);

		result.appendHtml("<div id='menu_" + id + "' class='markupMenu'>");

		List<String> levelOneCategories = new ArrayList<>(groupedTools.keySet());
		Collections.sort(levelOneCategories);

		for (String category : levelOneCategories) {
//			if (category.matches("..-\".*\"")) {
//				String displayName = Strings.unquote(category.substring(3));
//				result.appendHtml("<span class=\"markupMenuDivider\">&nbsp;<span class=\"categoryName\">")
//						.append(displayName).appendHtml("</span></span>");
//			}
//			else
			if (!category.equals(levelOneCategories.get(0))) {
				String displayName = Strings.unquote(category);
				result.appendHtml("<span class=\"markupMenuDivider\">&nbsp;</span>");
			}

			Map<String, List<Tool>> levelTwoTools = groupedTools.get(category);

			List<String> levelTwoCategories = new ArrayList<>(levelTwoTools.keySet());
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
		result.appendHtmlTag("div", "class", tool.getClass().getSimpleName());
		result.appendHtmlTag("div", "class", "markupMenuItem list-group");
		result.appendHtmlTag("a", false, "class", "markupMenuItem list-group-item",
				"title", Strings.encodeHtml(tool.getDescription()),
				ToolUtils.getActionAttributeName(tool),
				ToolUtils.getActionAttributeValue(tool));
		if (tool.getIcon() != null) {
			result.appendHtml(tool.getIcon().fixWidth().toHtml());
			result.append(" ");
		}
		result.appendHtmlElement("span", tool.getTitle());
		result.appendHtmlTag("/a");
		result.appendHtmlTag("/div");
		result.appendHtmlTag("/div");
	}

	@SuppressWarnings("unused")
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
