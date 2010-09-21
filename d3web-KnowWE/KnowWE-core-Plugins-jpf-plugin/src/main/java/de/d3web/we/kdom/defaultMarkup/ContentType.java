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

package de.d3web.we.kdom.defaultMarkup;

import java.util.Collections;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class ContentType extends DefaultAbstractKnowWEObjectType {

	private final static int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
	private final static String SECTION_REGEXP =
			// prefix (declare the markup section)
			"^\\p{Blank}*%%$NAME$\\p{Blank}*[:=\\p{Space}]" +
					// skip empty lines before content
					"(?:\\p{Space}*[\\r\\n]+)*" +
					// 100921 by ochlast: New content matching! This means:
					// "From now on match everything, that begins NOT with a
					// sequence of spaces, followed by an @ at the beginning of
					// the line."
					"((?:(?!^(?:\\p{Space}*)@\\w+).)*?)" +
					// skip emtpy lines after content
					"(?:\\p{Space}*[\\r\\n]+)*" +
					// suffix: terminate-tag or end-of-input or declare next
					// parameter
					"(?:(?:^\\p{Blank}*/?%\\p{Blank}*$)" +
					"|" +
					"(?:\\z)" +
					"|" +
					"(?:\\p{Space}+@\\w+))";

	private final DefaultMarkup markup;

	public ContentType(DefaultMarkup markup) {
		this.markup = markup;
		Pattern pattern = getContentPattern(this.markup.getName());
		this.setSectionFinder(new RegexSectionFinder(pattern, 1));
		Collections.addAll(this.childrenTypes, this.markup.getTypes());
	}

	/**
	 * Return the name of the content type section.
	 */
	@Override
	public String getName() {
		return this.markup.getName() + "@content";
	}

	/**
	 * Returns the pattern to match the default block of a default mark-up
	 * section.
	 * 
	 * @param markupName the name of the parent section (opened by
	 *        "%%&lt;markupName&gt;")
	 * @return the pattern to match the default block of the section
	 */
	public static Pattern getContentPattern(String markupName) {
		String regexp = SECTION_REGEXP.replace("$NAME$", markupName);
		return Pattern.compile(regexp, FLAGS);
	}
}
