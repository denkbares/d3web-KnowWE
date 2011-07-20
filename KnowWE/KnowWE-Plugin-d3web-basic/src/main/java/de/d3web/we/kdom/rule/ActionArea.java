/* Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.rule;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.utils.StringFragment;

public class ActionArea extends AbstractType {

	public ActionArea(AbstractType action) {

		this.addChildType(new SingleAction(action));
	}

	class SingleAction extends AbstractType {

		public SingleAction(AbstractType t) {
			this.setSectionFinder(new SingleActionFinder());

			this.addChildType(t);
			t.setSectionFinder(new AllTextFinderTrimmed());
		}
	}

	class SingleActionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<StringFragment> actions = SplitUtility.splitUnquoted(text, ";;;");
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			int index = 0;
			for (StringFragment string : actions) {
				// int startIndex = calcStartIndex(actions, index);
				result.add(new SectionFinderResult(string.getStart(), string.getStart()
						+ string.getContent().length()));
				index++;
			}
			return result;
		}


	}

}
