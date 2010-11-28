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

package de.d3web.we.testsuite.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.renderer.FontColorRenderer;

public class SequentialTestCaseName extends DefaultAbstractKnowWEObjectType {

	public SequentialTestCaseName() {
		this.sectionFinder = new SequentialTestCaseNameSectionFinder();
		// Do not remove this, otherwise rendering brokes with quoted names
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR0));
	}

	public class SequentialTestCaseNameSectionFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {

			if (text.length() == 0) return null;
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

			int start = 0;
			while (text.charAt(start) == ' ' || text.charAt(start) == '\n'
					|| text.charAt(start) == '\r') {
				start++;
				if (start == text.length()) break;
			}
			int end = text.indexOf('{');
			while (text.charAt(end - 1) == ' ') {
				end--;
			}
			SectionFinderResult s = new SectionFinderResult(start, end);
			result.add(s);

			return result;
		}

	}
}
