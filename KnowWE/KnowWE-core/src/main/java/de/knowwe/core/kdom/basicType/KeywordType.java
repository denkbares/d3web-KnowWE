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
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;

/**
 * Type to match a special keyword surrounded by only white-spaces.
 * 
 * @author volker_belli
 * @created 21.01.2011
 */
public final class KeywordType extends AbstractType {

	private final String keyWord;
	private static final StyleRenderer DEFAULT_RENDERER =
			new StyleRenderer(StyleRenderer.KEYWORDS, MaskMode.htmlEntities);

	/**
	 * Creates a new keyword type for a specific keyword that if treated as a
	 * literal string, not as a regular expression. Thus also characters may be
	 * included in the terminalKeyWord that usually interpreted as regular
	 * expression control characters.
	 * 
	 * @param literalKeyWord the literal keyword
	 */
	public KeywordType(String literalKeyWord) {
		this.keyWord = literalKeyWord;
		String regex = "\\s*(" + Pattern.quote(literalKeyWord) + ")\\s*";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		setSectionFinder(new RegexSectionFinder(pattern, 1));
		setRenderer(DEFAULT_RENDERER);
	}

	public KeywordType(Pattern keyWordPattern, int group) {
		this.keyWord = keyWordPattern.pattern() + ".group(" + group + ")";
		setSectionFinder(new RegexSectionFinder(keyWordPattern, group));
		setRenderer(DEFAULT_RENDERER);
	}

	@Override
	public String getName() {
		return "Keyword:" + keyWord;
	}
}