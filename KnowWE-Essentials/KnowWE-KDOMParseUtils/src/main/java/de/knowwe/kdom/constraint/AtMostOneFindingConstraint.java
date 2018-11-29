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

package de.knowwe.kdom.constraint;

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Allows only the first found section to be used. If multiple sections are detected, the succeeding ones are ignored.
 */
public class AtMostOneFindingConstraint implements SectionFinderConstraint {

	private static final AtMostOneFindingConstraint instance = new AtMostOneFindingConstraint();

	public static AtMostOneFindingConstraint getInstance() {
		return instance;
	}

	private AtMostOneFindingConstraint() {
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		if (found == null || found.size() <= 1) return;

		SectionFinderResult firstResult = found.get(0);
		found.clear();
		found.add(firstResult);
	}
}
