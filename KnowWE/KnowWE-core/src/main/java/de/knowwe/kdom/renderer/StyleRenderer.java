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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

public class StyleRenderer implements Renderer {

	public static final StyleRenderer KEYWORDS = new StyleRenderer("color:rgb(0, 0, 0)");
	public static final StyleRenderer OPERATOR = new StyleRenderer("color:rgb(40, 40, 160)");
	public static final StyleRenderer PROPERTY = new StyleRenderer("color:rgb(30, 40, 100)");
	public static final StyleRenderer CONDITION = new StyleRenderer("color:rgb(0, 128, 0)");
	public static final StyleRenderer PROMPT = new StyleRenderer("color:rgb(0, 128, 0)");
	public static final StyleRenderer NUMBER = new StyleRenderer("color:rgb(125, 80, 102)");
	public static final StyleRenderer COMMENT = new StyleRenderer("color:rgb(160, 160, 160)");
	public static final StyleRenderer CONTENT = new StyleRenderer("color:rgb(80, 80, 80)");
	public static final StyleRenderer LOCALE = new StyleRenderer("color:rgb(0, 128, 128)");
	public static final StyleRenderer ANNOTATION = COMMENT;

	public static final Renderer CHOICE = new ToolMenuDecoratingRenderer(
			new StyleRenderer("color:rgb(40, 40, 160)"));
	public static final Renderer SOLUTION = new ToolMenuDecoratingRenderer(
			new StyleRenderer("color:rgb(150, 110, 120)"));
	public static final Renderer Question = new ToolMenuDecoratingRenderer(
			new StyleRenderer("color:rgb(0, 128, 0)"));
	public static final Renderer Questionaire = new ToolMenuDecoratingRenderer(
			new StyleRenderer("color:rgb(128, 128, 0)"));

	public static final Renderer PACKAGE = new ToolMenuDecoratingRenderer(new StyleRenderer(
			"packageOpacity",
			"color:rgb(121,79, 64);"));

	public static final String CONDITION_FULLFILLED = "#CFFFCF";
	public static final String CONDITION_FALSE = "#FFCFCF";

	/**
	 * When normal functionality as in FontColorRenderer: Set background null;
	 * 
	 * @param color
	 * @param background
	 * @return
	 */
	public static StyleRenderer getRenderer(String color, String background) {
		return new StyleRenderer(generateCSSStyle(color, background));
	}

	/**
	 * Allows for setting the class attribute.
	 * 
	 * @param cssClass
	 * @param color
	 * @param background
	 * @return
	 */
	public static StyleRenderer getRenderer(String cssClass, String color, String background) {
		return new StyleRenderer(cssClass, generateCSSStyle(color, background));
	}

	private static String generateCSSStyle(String color, String background) {
		return (color == null ? "" : color + ";") +
				(background == null ? "" : "background-color:" + background);
	}

	private final String cssClass;
	private final String cssStyle;
	private boolean maskJSP = true;

	public StyleRenderer(String cssStyle) {
		this(null, cssStyle);
	}

	public StyleRenderer(String cssClass, String cssStyle) {
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		string.appendHtml("<span");
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}
		string.appendHtml(">");
		renderContent(section, user, string);
		string.appendHtml("</span>");
	}

	public void renderText(String text, UserContext user, RenderResult string) {
		string.appendHtml("<span");
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}
		string.appendHtml(">");
		string.append(text);
		string.appendHtml("</span>");
	}

	/**
	 * Renders the content that will automatically be styled in the correct way.
	 * You may overwrite it for special purposes.
	 * 
	 * @created 06.10.2010
	 * @param article the article to render for
	 * @param section the section to be rendered
	 * @param user the user to render for
	 * @param string the buffer to render into
	 */
	protected void renderContent(Section<?> section, UserContext user, RenderResult string) {
		RenderResult builder = new RenderResult(user);
		DelegateRenderer.getInstance().render(section, user, builder);
		if (maskJSP) {
			string.appendJSPWikiMarkup(builder);
		}
		else {
			string.append(builder);
		}
	}

	public String getCssStyle() {
		return this.cssStyle;
	}

	public String getCssClass() {
		return this.cssClass;
	}

	public void setMaskJSPWikiMarkup(boolean mask) {
		this.maskJSP = mask;
	}

	public boolean isMaskingJSPWikiMarkup() {
		return this.maskJSP;
	}

}
