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

import java.util.Collections;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;

public class ContentType extends AbstractType {

	private final DefaultMarkup markup;

	private final static int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
			| Pattern.DOTALL;

	private final static String REGEX =
			// prefix (declare the markup section)
			"^\\p{Blank}*%%$NAME$\\p{Blank}*[:=\\p{Space}]" +
					// skip empty lines before content
					"(?:\\p{Space}*[\\r\\n]+)*" +
					// 100921 by ochlast: New content matching! This means:
					// "From now on match everything, that begins NOT with a
					// sequence of spaces, followed by an @ at the beginning
					// of
					// the line."
					"((?:(?!$LINESTART$\\p{Space}*@\\w+).)*?)" +
					// skip emtpy lines after content
					"(?:\\p{Space}*[\\r\\n]+)*" +
					// suffix: terminate-tag or end-of-input or declare next
					// parameter
					"(^\\p{Blank}*/?%\\p{Blank}*$" +
					"|\\z|" +
					"\\p{Space}+@\\w+)";

	public ContentType(DefaultMarkup markup) {
		this.markup = markup;
		this.setSectionFinder(new AdaptiveMarkupFinder(markup.getName(), REGEX, FLAGS, 1));
		Collections.addAll(this.childrenTypes, this.markup.getTypes());
	}

	/**
	 * Return the name of the content type section.
	 */
	@Override
	public String getName() {
		return "content";
	}
}
