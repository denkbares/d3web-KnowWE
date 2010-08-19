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

package de.d3web.we.kdom.bulletLists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.LineBreak;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class BulletListItemLine extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new BulletListItemLineFinder();
		this.childrenTypes.add(new BulletType());
		this.childrenTypes.add(new LineBreak());
		this.childrenTypes.add(new BulletCommentType());
		this.childrenTypes.add(new BulletContentType());

	}

	class BulletListItemLineFinder extends SectionFinder {

		@SuppressWarnings("unchecked")
		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {

			String lineRegex = "\\r?\\n";
			Pattern linePattern = Pattern.compile(lineRegex);

			Matcher tagMatcher = linePattern.matcher(text);
			ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();
			int lastStart = 0;
			while (tagMatcher.find()) {

				String line = text.substring(lastStart,
						tagMatcher.end());
				// only lines starting with '*' can be bulletListItemLines
				if (line.trim().startsWith("*")) {
					resultRegex.add(new SectionFinderResult(lastStart,
							tagMatcher.end()));
					lastStart = tagMatcher.end();
				}
			}
			return resultRegex;
		}

	}

}
