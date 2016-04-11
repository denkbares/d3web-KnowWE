/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Easy to use renderer for annotations. Name can be rendered as name or be replaces with a given icon.
 * Content is rendered in a given color (or default grey if no color is given).
 * <p>
 * Add this renderer with the following method: {@link DefaultMarkup#addAnnotationRenderer(String, Renderer)}
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.11.15
 */
public class AnnotationRenderer implements Renderer {

	private final Icon nameIcon;
	private final String name;
	private final StyleRenderer nameStyle;
	private final StyleRenderer contentStyle;

	/**
	 * Renders the annotation in complete in a light grey (including the @ at the start).
	 */
	public AnnotationRenderer() {
		this(null, null, null, null);
	}

	/**
	 * Renders the annotation content in a light grey with the given icon in front
	 *
	 * @param nameIcon the icon to render instead of the name
	 */
	public AnnotationRenderer(Icon nameIcon) {
		this(nameIcon, null);
	}

	/**
	 * Renders the annotation in a light grey, replaces the actual @[AnnotationName] with the given name.
	 *
	 * @param name the name to display instead of the actual @[AnnotationName]
	 */
	public AnnotationRenderer(String name) {
		this(name, null, null);
	}

	/**
	 * Renders the annotation with the icon instead of the name and the content using the given {@link StyleRenderer}.
	 *
	 * @param nameIcon     the icon to replace the annotation name
	 * @param contentStyle the style of the content - can be null
	 */
	public AnnotationRenderer(Icon nameIcon, StyleRenderer contentStyle) {
		this(nameIcon, null, null, contentStyle);
	}

	/**
	 * Renders the annotation with the give name (instead of the ) and styles for name and
	 * content
	 *
	 * @param name         the name to replace the actual name string
	 * @param nameStyle    the style of the name - can be null
	 * @param contentStyle the style of the content - can be null
	 */
	public AnnotationRenderer(String name, StyleRenderer nameStyle, StyleRenderer contentStyle) {
		this(null, name, nameStyle, contentStyle);
	}

	private AnnotationRenderer(Icon nameIcon, String name, StyleRenderer nameStyle, StyleRenderer contentStyle) {
		this.nameIcon = nameIcon;
		this.name = name;
		this.nameStyle = nameStyle == null ? StyleRenderer.COMMENT : nameStyle;
		this.contentStyle = contentStyle == null ? StyleRenderer.COMMENT : contentStyle;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		if (!(section.get() instanceof AnnotationType)) {
			throw new IllegalArgumentException("Invalid usage of " + this.getClass()
					.getSimpleName() + ". Expected to render " + AnnotationType.class.getSimpleName() +
					" but was " + section.get().getClass().getSimpleName());
		}
		if (name != null) {
			result.appendHtmlElement("span", name, "style", nameStyle.getCssStyle());
		}
		else if (nameIcon != null) {
			result.appendHtml(nameIcon.addStyle("padding-right: 4px").toHtml());
		}
		else {
			Section<AnnotationNameType> nameSection = $(section).successor(AnnotationNameType.class).getFirst();
			nameStyle.render(nameSection, user, result);

		}
		Section<AnnotationContentType> contentSection = $(section).successor(AnnotationContentType.class).getFirst();
		contentStyle.render(contentSection, user, result);
	}
}
