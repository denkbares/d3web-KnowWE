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

package de.d3web.we.kdom.constraint;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class AtMostOneFindingConstraint implements SectionFinderConstraint {

	private static AtMostOneFindingConstraint instance = new AtMostOneFindingConstraint();

	public static AtMostOneFindingConstraint getInstance() {
		return instance;
	}

	private AtMostOneFindingConstraint() {
	}

	@Override
	public void filterCorrectResults(
			List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		if (found == null || found.size() == 0) return;

		SectionFinderResult firstResult = found.get(0);
		found.clear();
		found.add(firstResult);

	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found,
			Section father, KnowWEObjectType type) {
		if (found.size() <= 1) {
			return true;
		}
		return false;
	}

}
