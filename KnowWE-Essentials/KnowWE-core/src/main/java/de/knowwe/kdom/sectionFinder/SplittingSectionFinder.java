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
package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A sectionfinder that uses a regex to split the section. Each SectionFinderResult contains the
 * text up to the next match (including or excluding the match, depending if includeSeperator is set
 * to true or false).
 *
 * @author Reinhard Hatko Created on: 10.12.2009
 */
public class SplittingSectionFinder implements SectionFinder {

	private final boolean includeSeperator;
	private final Pattern pattern;

	public SplittingSectionFinder(String patternString) {
		this(true, patternString);
	}

	public SplittingSectionFinder(boolean includeSeperator, String patternString) {
		this.includeSeperator = includeSeperator;
		this.pattern = Pattern.compile(patternString);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		Matcher tagMatcher = pattern.matcher(text);
		List<SectionFinderResult> resultRegex = new ArrayList<>();
		int nextStart = 0;
		while (tagMatcher.find()) {
			resultRegex.add(new SectionFinderResult(nextStart,
					includeSeperator ? tagMatcher.end() : tagMatcher.start()));
			nextStart = tagMatcher.end();
		}
		// append section result after last split character / substring
		// also adds the total text, if there is no match (!)
		if (nextStart < text.length()) {
			resultRegex.add(new SectionFinderResult(nextStart, text.length()));
		}
		return resultRegex;
	}
}
