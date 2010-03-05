/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class AnnotationFinder extends SectionFinder {

	private final static Pattern nextAnnotationPattern = 
		Pattern.compile("\\p{Space}+@\\w+", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);

	private final static Pattern endpattern = 
		Pattern.compile("\\p{Space}*^\\p{Blank}*%\\p{Blank}*$", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);

	private final String name;
	private final Pattern startPattern;

	public AnnotationFinder(String name) {
		this.name = name;
		startPattern = Pattern.compile(
				"@" + name + "\\p{Blank}*[:=\\p{Space}]\\p{Space}*",
				Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		int pos = 0;
		while (text.length() > 0) {
			int start = findEnd(text, pos, startPattern);
			if (start == -1) {
				return result;
			}

			// find end (ends immediately if next annotation follows...)
			int end1 = (text.charAt(start) == '@') ? start : text.length();
			int end2 = findStart(text, start, nextAnnotationPattern);
			int end3 = findStart(text, start, endpattern);
			
			// find the earliest match of all possible terminators
			int end = end1;
			if (end2 != -1 && end2 < end) {
				end = end2;
			}
			if (end3 != -1 && end3 < end) {
				end = end3;
			}
			result.add(new SectionFinderResult(start, end));
			pos = end;
		}
		return result;
	}

	private int findStart(String text, int startIndex, Pattern pattern) {
		Matcher matcher = pattern.matcher(text);
		if (matcher.find(startIndex)) {
			return matcher.start();
		}
		else {
			return -1;
		}
	}

	private int findEnd(String text, int startIndex, Pattern pattern) {
		Matcher matcher = pattern.matcher(text);
		if (matcher.find(startIndex)) {
			return matcher.end();
		}
		else {
			return -1;
		}
	}
}
