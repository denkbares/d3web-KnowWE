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

package de.knowwe.kdom.renderer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationRenderer;
import de.knowwe.tools.ToolMenuDecoratingRenderer;
import de.knowwe.util.Icon;

public final class StyleRenderer implements Renderer {
	public static final StyleRenderer DEFAULT = new StyleRenderer();
	public static final StyleRenderer KEYWORDS = new StyleRenderer("style-keywords");
	public static final StyleRenderer OPERATOR = new StyleRenderer("style-operator");
	public static final StyleRenderer CONSTANT = new StyleRenderer("style-constant");
	public static final StyleRenderer PROPERTY = new StyleRenderer("style-property");
	public static final StyleRenderer CONDITION = new StyleRenderer("style-condition");
	public static final StyleRenderer PROMPT = new StyleRenderer("style-prompt", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer NUMBER = new StyleRenderer("style-number");
	public static final StyleRenderer COMMENT = new StyleRenderer("style-comment", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer CONTENT = new StyleRenderer("style-content");
	public static final StyleRenderer LOCALE = new StyleRenderer("style-locale");

	public static final StyleRenderer CHOICE_NO_TOOLS = new StyleRenderer("style-choice", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer QUESTION_NO_TOOLS = new StyleRenderer("style-question", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer QUESTIONNAIRE_NO_TOOLS = new StyleRenderer("style-questionnaire", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer SOLUTION_NO_TOOLS = new StyleRenderer("style-solution", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);

	public static final StyleRenderer FLOWCHART_NO_TOOLS = new StyleRenderer("style-flowchart", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer FLOWCHART_START_NO_TOOLS = new StyleRenderer("style-flowchart-start", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final StyleRenderer FLOWCHART_EXIT_NO_TOOLS = new StyleRenderer("style-flowchart-exit", MaskMode.htmlEntities, MaskMode.jspwikiMarkup);
	public static final String CLICKABLE_TERM_CLASS = "clickable-term";

	public static final AnnotationRenderer ANNOTATION = new AnnotationRenderer();

	public static final Renderer CHOICE = CHOICE_NO_TOOLS.withToolMenu();
	public static final Renderer SOLUTION = SOLUTION_NO_TOOLS.withToolMenu();
	public static final Renderer QUESTION = QUESTION_NO_TOOLS.withToolMenu();
	public static final Renderer QUESTIONNAIRE = QUESTIONNAIRE_NO_TOOLS.withToolMenu();

	public static final Renderer FLOWCHART = FLOWCHART_NO_TOOLS.withToolMenu();
	public static final Renderer FLOWCHART_START = FLOWCHART_START_NO_TOOLS.withToolMenu();
	public static final Renderer FLOWCHART_EXIT = FLOWCHART_EXIT_NO_TOOLS.withToolMenu();

	public static final Renderer PACKAGE = new ToolMenuDecoratingRenderer(StyleRenderer.DEFAULT.withCssClass("packageOpacity")
			.withMaskMode(MaskMode.htmlEntities, MaskMode.jspwikiMarkup));

	public static final StyleRenderer CONDITION_FULFILLED = new StyleRenderer("style-condition-fulfilled");
	public static final StyleRenderer CONDITION_FALSE = new StyleRenderer("style-condition-false");

	public enum MaskMode {
		none, jspwikiMarkup, htmlEntities
	}

	private final String cssClass;
	private final String cssStyle;
	private final Icon icon;
	private MaskMode[] maskMode = new MaskMode[] { MaskMode.jspwikiMarkup };

	private StyleRenderer() {
		this(null, (String) null);
	}

	private StyleRenderer(String cssClass) {
		this(cssClass, null, null);
	}

	private StyleRenderer(String cssClass, String cssStyle) {
		this(cssClass, cssStyle, null);
	}

	private StyleRenderer(String cssClass, MaskMode... maskMode) {
		this(cssClass, null, maskMode);
	}

	private StyleRenderer(String cssClass, String cssStyle, MaskMode... maskMode) {
		this(cssClass, cssStyle, null, maskMode);
	}

	private StyleRenderer(String cssClass, String cssStyle, Icon icon, MaskMode... maskMode) {
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
		this.icon = icon;
		this.maskMode = maskMode;
	}

	/**
	 * Creates a copy of this style renderer, that additionally uses a specified css class.
	 *
	 * @param cssClass the css class to be (additionally) used
	 * @return the created style renderer
	 */
	public StyleRenderer withCssClass(String cssClass) {
		if (Strings.isBlank(cssClass)) return this;
		String css = Strings.isBlank(this.cssClass) ? cssClass : (this.cssClass + " " + cssClass);
		return new StyleRenderer(css, this.cssStyle, this.icon, this.maskMode);
	}

	/**
	 * Creates a copy of this style renderer, that additionally uses the specified css style instruction(s).
	 *
	 * @param cssStyle the css style instruction(s) to be (additionally) used
	 * @return the created style renderer
	 */
	public StyleRenderer withCssStyle(String cssStyle) {
		if (cssStyle.contains("color")) {
			throw new IllegalArgumentException("Please use CSS classes for color styling.");
		}
		if (Strings.isBlank(cssStyle)) return this;
		String css = Strings.isBlank(this.cssStyle) ? cssStyle : (this.cssStyle + ";" + cssStyle);
		return new StyleRenderer(this.cssClass, css, this.icon, this.maskMode);
	}

	/**
	 * Creates a decorated instance of this style renderer, that additionally provides a tool menu for the rendered
	 * section.
	 *
	 * @return the created tool menu renderer
	 */
	public Renderer withToolMenu() {
		return new ToolMenuDecoratingRenderer(withCssClass(CLICKABLE_TERM_CLASS));
	}

	public StyleRenderer withIcon(Icon icon) {
		return new StyleRenderer(this.cssClass, this.cssStyle, icon, this.maskMode);
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		renderOpeningTag(section, user, string);
		renderContent(section, user, string);
		string.appendHtml("</span>");
	}

	private void renderOpeningTag(Section<?> section, UserContext user, RenderResult string) {
		renderIcon(string);
		string.appendHtml("<span");
		String cssClasses = cssClass;
		if (section != null) {
			string.append(" sectionid='").append(section.getID()).append("'");
			if (greyOut(section, user)) {
				cssClasses += " greyout";
				cssClasses = cssClasses.trim();
			}
		}
		if (!Strings.isBlank(cssClasses)) {
			string.append(" class='").append(cssClasses).append("'");
		}
		if (!Strings.isBlank(cssStyle)) {
			string.append(" style='").append(cssStyle).append("'");
		}
		string.appendHtml(">");
	}

	private static boolean greyOut(Section<?> section, UserContext user) {
		if (KnowWEUtils.isAttachmentArticle(section.getArticle()) && section.get() instanceof TermReference) {
			Section<TermReference> reference = Sections.cast(section, TermReference.class);
			GroupingCompiler groupingCompiler = Compilers.getCompiler(user, section, GroupingCompiler.class);
			if (groupingCompiler == null) return false;
			List<TermCompiler> termCompilers = groupingCompiler.getChildCompilers()
					.stream()
					.filter(c -> c instanceof TermCompiler)
					.map(c -> (TermCompiler) c)
					.toList();
			Set<TermCompiler.ReferenceValidationMode> modes = termCompilers.stream()
					.map(c -> reference.get().getReferenceValidationMode(c, reference))
					.collect(Collectors.toSet());
			if (modes.size() != 1) return false;
			TermCompiler.ReferenceValidationMode mode = modes.iterator().next();
			if (mode == TermCompiler.ReferenceValidationMode.greyOut) {
				return termCompilers.stream().noneMatch(c -> reference.get().isDefinedTerm(c, reference));
			}
		}
		return false;
	}

	private void renderIcon(RenderResult string) {
		if (this.icon == null) return;
		string.appendHtml(this.icon.toHtml());
	}

	public void renderText(Section<?> section, String text, UserContext user, RenderResult string) {
		renderOpeningTag(section, user, string);
		string.appendJSPWikiMarkup(text);
		string.appendHtml("</span>");
	}

	public void renderText(String text, UserContext user, RenderResult string) {
		renderText(null, text, user, string);
	}

	/**
	 * Renders a text without escaping JSP wiki syntax
	 */
	public void renderTextUnmasked(String text, UserContext user, RenderResult string) {
		renderOpeningTag(null, user, string);
		string.append(text);
		string.appendHtml("</span>");
	}

	/**
	 * Renders the content that will automatically be styled in the correct way. You may overwrite it for special
	 * purposes.
	 *
	 * @param section the section to be rendered
	 * @param user    the user to render for
	 * @param string  the buffer to render into
	 * @created 06.10.2010
	 */
	private void renderContent(Section<?> section, UserContext user, RenderResult string) {
		RenderResult builder = new RenderResult(user);
		DelegateRenderer.getInstance().render(section, user, builder);
		if (ArrayUtils.contains(maskMode, MaskMode.jspwikiMarkup)
			&& ArrayUtils.contains(maskMode, MaskMode.htmlEntities)) {
			string.appendJSPWikiMarkup(encodeHtml(builder));
		}
		else if (ArrayUtils.contains(maskMode, MaskMode.jspwikiMarkup)) {
			string.appendJSPWikiMarkup(builder);
		}
		else if (ArrayUtils.contains(maskMode, MaskMode.htmlEntities)) {
			string.append(encodeHtml(builder));
		}
		else {
			string.append(builder);
		}
	}

	@Nullable
	private String encodeHtml(RenderResult builder) {
		String text = builder.toStringRaw();
		if (Strings.isQuoted(text)) {
			return "\"" + Strings.encodeHtml(Strings.unquote(text)) + "\"";
		}
		else {
			return Strings.encodeHtml(text);
		}
	}

	public String getCssStyle() {
		return this.cssStyle;
	}

	public String getCssClass() {
		return this.cssClass;
	}

	public StyleRenderer withMaskMode(MaskMode... maskMode) {
		return new StyleRenderer(this.cssClass, this.cssStyle, maskMode);
	}
}
