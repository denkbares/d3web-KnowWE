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
package de.knowwe.testcases.table;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public final class UnchangedType extends AbstractType {

	public static final String UNCHANGED_VALUE_STRING = "-";
	public static final String REGEX = "\\s*(" + UNCHANGED_VALUE_STRING + ")\\s*";
	public static final Pattern PATTERN = Pattern.compile(REGEX);

	public UnchangedType() {
		setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				Matcher matcher = PATTERN.matcher(text);
				if (matcher.matches()) {
					return SectionFinderResult.createSingleItemResultList(matcher.start(1),
							matcher.end(1));
				}
				else return Collections.emptyList();
			}
		});
	}
}