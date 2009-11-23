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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.Patterns;

public class XCLTail extends DefaultAbstractKnowWEObjectType {
	
	private static Pattern p = Pattern.compile(
			"\\s*" + 					// spaces
			"(\\w+)" + 					// Word chars
			"\\s*" + 					// spaces
			"=" +						 
			"\\s*" + 					// spaces
			"([-+]?[0-9]*\\.?[0-9]+)" + // fp-number
			"\\s*" + 					// spaces
			",?" +						// optional comma
			"\\s*"); 					// spaces
	
	public class XCLTailSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			List<SectionFinderResult> matches = new ArrayList<SectionFinderResult>(1);
			
			
			int end = text.lastIndexOf('}');
			int lastIndexOf = text.lastIndexOf('[');
			if(lastIndexOf > end) {
				matches.add(new SectionFinderResult(lastIndexOf, text.lastIndexOf(']') + 1));
			}
			
			return matches;
		}

	}
	
	
	public static double getSuggestedThreshold(Section section) {
		return getValue(section, "suggestedThreshold");
		
	}

	public static double getEstablisehdThreshold(Section section) {
		return getValue(section, "establishedThreshold");
		
	}
	
	public static double getMinSupport(Section section) {
		return getValue(section, "minSupport");
		
	}
	
	private static double getValue(Section section, String param) {
		if (!(section.getObjectType() instanceof XCLTail))
			return -1;		
		else {
			Matcher matcher = p.matcher(section.getOriginalText());
			
			while (matcher.find()) {
				if (matcher.group(1).equalsIgnoreCase(param))
					try {
						return Double.valueOf(matcher.group(2));
					} catch (Exception e) {
						return -1;
					}
				
			}
			return -1;
			
		}
			
		
		
	}

	@Override
	protected void init() {
		this.sectionFinder = new XCLTailSectionFinder();		
	}

	
	public static void main(String[] args) {
		
		String test = "[ establishedThreshold = 0.7,\n" + 
				"suggestedThreshold = 0.5,\r\n" + 
				"	 minSupport = 0.5\r" + 
				"]";
		
		
		Matcher matcher = p.matcher(test);
		int i = 0;
		while (matcher.find()) {
			
			System.out
					.println( ++i + ": " +  test.substring(matcher.start(), matcher.end()));
			
			int j = 1;
			int groupCount = matcher.groupCount();
			System.out.println("Groups:" + groupCount);
//			while (j <= groupCount) {
//				System.out.println(test.substring(matcher.start(j), matcher.end(j)));
//				j++;
//				
//			}
			System.out.println("***");
			
		}
		
		
	}
	
	
}
