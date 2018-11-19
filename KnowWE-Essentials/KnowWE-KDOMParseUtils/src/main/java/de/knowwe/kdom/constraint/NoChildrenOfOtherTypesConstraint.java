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

public class NoChildrenOfOtherTypesConstraint implements SectionFinderConstraint {

	private static final NoChildrenOfOtherTypesConstraint instance = new NoChildrenOfOtherTypesConstraint();

	public static NoChildrenOfOtherTypesConstraint getInstance() {
		return instance;
	}

	private NoChildrenOfOtherTypesConstraint() {
	}

	@Override
	public <T extends Type> void filterCorrectResults(
			List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {

		if (!satisfiesConstraint(found, father, type, text)) {
			found.clear();
		}
	}

	private static <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {

		for (Section<?> child : father.getChildren()) {
			if (!child.get().getClass().equals(type)) return false;
		}
		return true;
	}

}
