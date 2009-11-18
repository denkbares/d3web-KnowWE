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

public class LineSectionFinder extends SectionFinder {
	
	private static LineSectionFinder instance;
	
	public static LineSectionFinder getInstance() {
		if (instance == null) {
			instance = new LineSectionFinder();
			
		}

		return instance;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		
		String lineRegex = "\\r\\n";
		Pattern linePattern = Pattern.compile(lineRegex);
		
        Matcher tagMatcher = linePattern.matcher(text);		
        ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();
        int lastStart = 0;
        while (tagMatcher.find()) {
        	resultRegex.add(new SectionFinderResult(lastStart, tagMatcher.end()));
        	lastStart = tagMatcher.end();
		}
		return resultRegex;
	}
}
