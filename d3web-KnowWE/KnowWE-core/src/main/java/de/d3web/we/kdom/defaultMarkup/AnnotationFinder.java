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

	private final Pattern startPattern;
	private final static Pattern nextAnnotationPattern = Pattern.compile("\\p{Space}@\\w+", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	private final static Pattern endpattern = Pattern.compile("^\\p{Blank}*%\\p{Blank}$", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	
	private final String name;
	
	public AnnotationFinder(String name) {
		this.name=name;
		startPattern = Pattern.compile("@"+name+"[:=\\p{Space}]", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
	
	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		int pos = 0;
		while (text.length()>0) {
			int start = find(text.substring(pos), startPattern);
			if (start==-1) {
				return result;
			}
			start +=  1 + name.length() + 1;
			int end1 = text.substring(start).length();
			int end2 = find(text.substring(start), nextAnnotationPattern);
			int end3 = find(text.substring(start), endpattern);
			if (end2==-1) {
				end2=end1;
			} if (end3==-1) {
				end3=end1;
			}
			end1 = Math.min(end1, end2);
			end1 = Math.min(end1, end3);
			result.add(new SectionFinderResult(pos+start, pos+start+end1));
			pos = pos+start+end1;
		}
		return result;
	}
	
	private int find(String text, Pattern p) {
		Matcher matcher = p.matcher(text);
		if (matcher.find()) {
			return matcher.start();
		} else {
			return -1;
		}
	}

}
