/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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

import de.d3web.strings.StringFragment;
import de.d3web.strings.Strings;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.RoundBracedType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Reinhard Hatko
 * @created 19.07.2011
 */
public class RatedFinding extends AbstractType {

	public RatedFinding() {
		QuestionReference question = new QuestionReference();
		question.setSectionFinder(new QuestionSectionFinder());
		this.addChildType(question);

		RoundBracedType bracedType = new RoundBracedType(new ValueType());
		bracedType.setSteal(true);

		this.addChildType(bracedType);
		this.setSectionFinder(new RatedFindingSectionFinder());
	}

	class RatedFindingSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<StringFragment> findings = Strings.splitUnquoted(text, ",");
			for (StringFragment finding : findings) {
				int indexOf = finding.getStartTrimmed();
				SectionFinderResult s =
						new SectionFinderResult(indexOf, indexOf + finding.lengthTrimmed());
				result.add(s);
			}

			return result;
		}

	}

	class QuestionSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<StringFragment> solution = Strings.splitUnquoted(text, "(");

			String solutionText = solution.get(0).getContentTrimmed();
			int start = text.indexOf(solutionText);
			int end = start + solutionText.length();

			SectionFinderResult s =
					new SectionFinderResult(start, end);
			result.add(s);

			return result;
		}

	}

}
