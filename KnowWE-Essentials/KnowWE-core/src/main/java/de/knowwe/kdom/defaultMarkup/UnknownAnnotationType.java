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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

public class UnknownAnnotationType extends AbstractType {

	private final static int FLAGS =
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;

	private final static String SECTION_REGEXP = "^\\s*@(\\w+)(.*?)(?=^\\s*@|/?%|\\z)";

	private final static Pattern PATTERN = Pattern.compile(SECTION_REGEXP, FLAGS);

	public UnknownAnnotationType() {
		this.setSectionFinder(new RegexSectionFinder(PATTERN));
		setRenderer(StyleRenderer.DEFAULT_STYLE_RENDERER.withCssClass("error_highlight"));
	}

	/**
	 * Returns the name of the underlying annotation.
	 * 
	 * @return the annotation's name
	 */
	public static String getName(Section<?> section) {
		return getGroup(section, 1);
	}

	/**
	 * Returns the content of the underlying annotation.
	 * 
	 * @return the annotation's name
	 */
	public static String getContent(Section<?> section) {
		return getGroup(section, 2);
	}

	private static String getGroup(Section<?> section, int group) {
		Matcher matcher = PATTERN.matcher(section.getText());
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}
}