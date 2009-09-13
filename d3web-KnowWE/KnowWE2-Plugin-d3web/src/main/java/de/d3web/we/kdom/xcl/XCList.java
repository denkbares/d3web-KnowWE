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

public class XCList extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new XCListSectionFinder();
		childrenTypes.add(new XCLHead());
		childrenTypes.add(new XCLTail());
		childrenTypes.add(new XCLBody());
	}
	
	
	public class XCListSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			List<SectionFinderResult> matches = new ArrayList<SectionFinderResult>();
			
			Pattern p =
				Pattern.compile(
						"(\\s*?^\\s*?$\\s*|\\s*?\\Z|\\s*?<[/]?includedFrom[^>]*?>\\s*?)",
						Pattern.MULTILINE);
			
				Matcher m = p.matcher(text);
				
				int start = 0;
				int end = 0;
				while (m.find()) {
					end = m.start();
					if (text.substring(start, end).replaceAll(
							"\\s", "").length() > 0 && start < end) {
						matches.add(new SectionFinderResult(start, end));
					}
					start = m.end();
				}
			return matches;
		}
	}
}
