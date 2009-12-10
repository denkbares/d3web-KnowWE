/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.Section;

public class RegexSectionFinder extends SectionFinder {

	
	private final Pattern pattern;
	private final int group;

	public RegexSectionFinder(String p) {
		this(p, 0);
	}
	
	public RegexSectionFinder(String p, int patternmod) {
		this(p, patternmod, 0); 
	}

	/**
	 * creates sections that reflect the content of the group <code>group</code>.
	 */
	public RegexSectionFinder(String p, int patternmod, int group) {
		pattern = Pattern.compile(p, patternmod); 
		this.group = group;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		
		ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		
		
		Matcher m = pattern.matcher(text);

		while (m.find()) {
			result.add(createSectionFinderResult(m));
		}
		return result;
	
	}
	
	protected SectionFinderResult createSectionFinderResult(Matcher m) {
		return new SectionFinderResult(m.start(group), m.end(group));
	}
	
}
