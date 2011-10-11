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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Jochen
 * 
 *         An abstract SectionFnder class that allows for the use of so called
 *         SectionFinderConstraints This simplifies the implementation of
 *         certain SectionFinders. E.g., if only the first occurrence of some
 *         text-entity should be allocated for a type, then the
 *         'AtMostOneFindingConstraint' can be applied, filtering out additional
 *         matches
 * 
 */
public class ConstraintSectionFinder implements SectionFinder {

	private final SectionFinder finder;

	public ConstraintSectionFinder(SectionFinder finder) {
		this.finder = finder;
	}

	public ConstraintSectionFinder(SectionFinder finder, SectionFinderConstraint c) {
		this.finder = finder;
		this.constraintList = new ArrayList<SectionFinderConstraint>();
		this.constraintList.add(c);
	}

	/**
	 * 
	 * TODO: remove - shouldnt be accessed externally
	 * 
	 * @created 20.07.2010
	 * @return
	 */
	public List<SectionFinderConstraint> getConstraints() {
		return constraintList;
	}

	private List<SectionFinderConstraint> constraintList;

	public void addConstraint(SectionFinderConstraint constraint) {
		if (constraintList == null) {
			constraintList = new ArrayList<SectionFinderConstraint>();

		}
		this.constraintList.add(constraint);
	}

	/**
	 * First the lookForSection-task is delegated the actual implementation.
	 * Then The SectionFinderConstraints are filtered
	 */
	@Override
	public List<SectionFinderResult> lookForSections(String text,
			Section<?> father, Type type) {

		List<SectionFinderResult> result = finder.lookForSections(text, father, type);
		if (result != null) {
			Collections.sort(result);
			applyConstraints(result, father, type.getClass(), text);
		}

		return result;
	}

	/**
	 * Delegates the lookForSection-task to the actual implementation
	 * 
	 * @created 20.07.2010
	 * @param text
	 * @param father
	 * @return
	 */

	private void applyConstraints(List<SectionFinderResult> results, Section<?> father,
			Class<? extends Type> ob, String text) {

		if (constraintList == null) return;

		for (SectionFinderConstraint sectionFinderConstraint : constraintList) {
			if (!sectionFinderConstraint.satisfiesConstraint(results, father, ob, text)) {
				sectionFinderConstraint.filterCorrectResults(results, father, ob, text);
			}
		}

	}

}
