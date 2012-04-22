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

/**
 * Type to match a special keyword (or a regular expression) surrounded by only
 * white-spaces.
 * 
 * @author volker_belli
 * @created 21.01.2011
 */
public final class KeywordType extends AbstractType {

	private final String keyWord;

	public KeywordType(String keyWord) {
		this.keyWord = keyWord;
		String regex = "\\s*(" + keyWord + ")\\s*";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		setSectionFinder(new RegexSectionFinder(pattern, 1));
	}

	@Override
	public String getName() {
		return "Keyword:" + keyWord;
	}
}