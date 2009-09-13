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

public class AllTextSectionFinder extends SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		Matcher textMatcher = Pattern.compile("</?includedFrom[^>]*?>", Pattern.DOTALL).matcher(text);
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		int start = 0;
		int end = 0;
		int matchEnd = 0;
		boolean found = false;
		while (textMatcher.find()) {
			found = true;
			start = matchEnd;
			end = textMatcher.start();
			matchEnd = textMatcher.end();
			result.add(new SectionFinderResult(start, end));
		}
		if (found && matchEnd < text.length()) {
			result.add(new SectionFinderResult(matchEnd, text.length()));
		}
		if (!found && text.length() > 0) {
			result.add(new SectionFinderResult(0, text.length()));
		}
		return result;
	}
	
	public static void main(String[] args) {
		String test = "asöf</includedFrom>lköasklföl</includedFrom>aksö";
		List<SectionFinderResult> results = new AllTextSectionFinder().lookForSections(test, null);
		for (SectionFinderResult result:results) {
			System.out.println(test.substring(result.getStart(), result.getEnd()));
		}
	}

}
