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

package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.LineBreak;
import de.d3web.we.kdom.rule.Then;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class RuleActionLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new RuleActionLineSectionFinder();
		childrenTypes.add(new LineBreak());
		childrenTypes.add(new Then());
		AnonymousType action = new AnonymousType("RuleAction");
		action.setSectionFinder(new AllTextFinderTrimmed());
		childrenTypes.add(action);
	}

	public class RuleActionLineSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

			String lineRegex = "(THEN|DANN).+";
			Pattern linePattern = Pattern.compile(lineRegex, Pattern.DOTALL);

	        Matcher tagMatcher = linePattern.matcher(text);
	        ArrayList<SectionFinderResult> resultRegex =
	        			new ArrayList<SectionFinderResult>();

	        while (tagMatcher.find()) {
	        	resultRegex.add(
	        			new SectionFinderResult(
	        					tagMatcher.start(), tagMatcher.end()));
			}
			return resultRegex;
		}

	}


}