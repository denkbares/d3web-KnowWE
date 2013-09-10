/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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

import java.util.Iterator;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A contraint that prevents the creation of sections of length 0.
 * 
 * @author jochenreutelshofer
 * @created 28.11.2012
 */
public class NoEmptySectionsConstraint implements SectionFinderConstraint {

	private static NoEmptySectionsConstraint instance = new NoEmptySectionsConstraint();

	public static NoEmptySectionsConstraint getInstance() {
		return instance;
	}

	private NoEmptySectionsConstraint() {
	}

	@Override
	public <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		for (SectionFinderResult sectionFinderResult : found) {
			if (sectionFinderResult.getStart() == sectionFinderResult.getEnd()) return false;
		}
		return true;
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		Iterator<SectionFinderResult> iterator = found.iterator();
		while (iterator.hasNext()) {
			SectionFinderResult nextResult = iterator.next();
			if (nextResult.getStart() == nextResult.getEnd()) {
				iterator.remove();
			}
		}
	}

}
