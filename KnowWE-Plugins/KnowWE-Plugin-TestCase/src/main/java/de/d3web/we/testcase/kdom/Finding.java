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

import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.d3web.we.kdom.condition.FindingAnswerReference;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.LeftOfTokenFinder;
import de.knowwe.core.kdom.sectionFinder.RightOfTokenFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class Finding extends AbstractType {

	public Finding() {

		QuestionReference question = new QuestionReference();
		question.setSectionFinder(new LeftOfTokenFinder("="));

		AnswerReference answer = new FindingAnswerReference();
		answer.setSectionFinder(new RightOfTokenFinder("="));

		this.addChildType(question);
		this.addChildType(answer);
		this.setSectionFinder(new FindingSectionFinder());
	}

	public static class FindingSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<>();
			List<StringFragment> findings = Strings.splitUnquoted(text, ",");
			for (StringFragment finding : findings) {

				// this might be dangerous when identical findings occur !
				int indexOf = finding.getStart();
				SectionFinderResult s =
						new SectionFinderResult(indexOf, indexOf
								+ finding.getContent().length());
				result.add(s);
			}

			return result;
		}
	}
}
