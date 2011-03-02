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

package de.d3web.we.testcase.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.utils.SplitUtility;

public class RatedSolution extends AbstractType {

	public RatedSolution() {
		SolutionReference solution = new SolutionReference();
		solution.setSectionFinder(new SolutionSectionFinder());
		childrenTypes.add(solution);
		childrenTypes.add(new StateRating());
		this.sectionFinder = new RatedSolutionSectionFinder();
	}

	public class RatedSolutionSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<String> findings = SplitUtility.splitUnquoted(text, ",");
			for (String finding : findings) {
				int indexOf = text.indexOf(finding);
				SectionFinderResult s =
						new SectionFinderResult(indexOf, indexOf + finding.length());
				result.add(s);
			}

			return result;
		}

	}

	public class SolutionSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<String> solution = SplitUtility.splitUnquoted(text, "(");

			String solutionText = solution.get(0).trim();
			int start = text.indexOf(solutionText);
			int end = start + solutionText.length();

			SectionFinderResult s =
					new SectionFinderResult(start, end);
			result.add(s);

			return result;
		}

	}

}
