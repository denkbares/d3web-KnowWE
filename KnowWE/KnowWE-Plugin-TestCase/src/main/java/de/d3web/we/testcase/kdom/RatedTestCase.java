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
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.basic.SquareBracedType;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.utils.StringFragment;

public class RatedTestCase extends AbstractType {

	public RatedTestCase() {
		// for timestamps like [5h31m5s]
		SquareBracedType bracedType = new SquareBracedType(new TimeStampType());
		bracedType.setSteal(true);
		bracedType.setCustomRenderer(StyleRenderer.NUMBER);

		childrenTypes.add(bracedType);
		childrenTypes.add(new Findings());
		childrenTypes.add(new RatedSolutions());
		this.sectionFinder = new RatedTestCaseSectionFinder();
	}

	public class RatedTestCaseSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<StringFragment> cases = SplitUtility.splitUnquoted(text, ";");
			for (StringFragment string : cases) {
				int indexOf = string.getStartTrimmed();
				SectionFinderResult s =
						new SectionFinderResult(indexOf, indexOf + string.lengthTrimmed()
								+ 1);
				result.add(s);
			}

			return result;
		}

	}
}
