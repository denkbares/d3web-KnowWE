/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.basicType;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;

/**
 * Type to match a special keyword surrounded by only white-spaces.
 *
 * @author volker_belli
 * @created 21.01.2011
 */
public class KeywordType extends AbstractType {

	private final String keyWord;
	private static final Renderer DEFAULT_RENDERER = new StyleRenderer(StyleRenderer.KEYWORDS, MaskMode.htmlEntities);
	private static final Renderer PLAIN_RENDERER = DefaultTextRenderer.getInstance();

	/**
	 * Creates a new keyword type for a specific keyword that if treated as a literal string, not as a regular
	 * expression. Thus also characters may be included in the terminalKeyWord that usually interpreted as regular
	 * expression control characters.
	 *
	 * @param literalKeyWord the literal keyword
	 */
	public KeywordType(String literalKeyWord) {
		this(literalKeyWord, false);
	}

	/**
	 * Creates a new keyword type for a specific keyword that if treated as a literal string, not as a regular
	 * expression. Thus also characters may be included in the terminalKeyWord that usually interpreted as regular
	 * expression control characters.
	 *
	 * @param literalKeyWord the literal keyword
	 * @param renderPlain    specified if the keyword shall be rendered plain
	 */
	public KeywordType(String literalKeyWord, boolean renderPlain) {
		this.keyWord = literalKeyWord;
		String regex = "\\s*(" + Pattern.quote(literalKeyWord) + ")\\s*";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		setSectionFinder(new RegexSectionFinder(pattern, 1));
		setRenderer(renderPlain ? PLAIN_RENDERER : DEFAULT_RENDERER);
	}

	/**
	 * Creates a new keyword type for a specific keyword interpreted as a regular expression.
	 *
	 * @param keyWordPattern the keyword pattern
	 */
	public KeywordType(Pattern keyWordPattern) {
		this(keyWordPattern, 0);
	}

	/**
	 * Creates a new keyword type for a specific keyword interpreted as a regular expression.
	 *
	 * @param keyWordPattern the keyword pattern
	 * @param group          the group to take the regular expression from
	 */
	public KeywordType(Pattern keyWordPattern, int group) {
		this(keyWordPattern, group, false);
	}

	/**
	 * Creates a new keyword type for a specific keyword interpreted as a regular expression.
	 *
	 * @param keyWordPattern the keyword pattern
	 * @param group          the group to take the regular expression from
	 * @param renderPlain    specified if the keyword shall be rendered plain
	 */
	public KeywordType(Pattern keyWordPattern, int group, boolean renderPlain) {
		this.keyWord = keyWordPattern.pattern() + ".group(" + group + ")";
		setSectionFinder(new RegexSectionFinder(keyWordPattern, group));
		setRenderer(renderPlain ? PLAIN_RENDERER : DEFAULT_RENDERER);
	}

	public KeywordType(SectionFinder sectionFinder, String keyWord) {
		this(sectionFinder, keyWord, false);
	}

	public KeywordType(SectionFinder sectionFinder, String keyWord, boolean renderPlain) {
		this(sectionFinder, keyWord, renderPlain ? PLAIN_RENDERER : DEFAULT_RENDERER);
	}

	public KeywordType(SectionFinder sectionFinder, String keyWord, Renderer renderer) {
		super(sectionFinder);
		this.keyWord = keyWord;
		setRenderer(renderer);
	}

	@Override
	public String getName() {
		return "Keyword:" + keyWord;
	}
}