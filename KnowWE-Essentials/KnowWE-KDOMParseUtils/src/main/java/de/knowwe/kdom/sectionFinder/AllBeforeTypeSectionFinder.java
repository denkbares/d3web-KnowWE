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

package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sectionizable;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class AllBeforeTypeSectionFinder implements SectionFinder {

	Sectionizable markerType = null;

	ThreadLocal<LinkedList<String>> findThese = new ThreadLocal<>();


	/**
	 * To use this SectionFinder, the argument type has to be added as an
	 * allowed children to the parent type DIRECTLY before the type with this
	 * SectionFinder is added to the parent type.
	 * <p/>
	 * <b>Examples:</b>
	 * 
	 * <pre>
	 * 
	 * 
	 * public ParentType() {
	 * 
	 * 	FirstType firstType = new FirstType();
	 * 	SecondType secondType = new SecondType();
	 * 
	 * 	secondType.setSectionFinder(new AllBeforeTypeSectionFinder(firstType));
	 * 
	 * 	this.addChildType(firstType);
	 * 	this.addChildType(secondType);
	 * }
	 * </pre>
	 * 
	 */
	public AllBeforeTypeSectionFinder(Sectionizable type) {
		this.markerType = type;
		type.setSectionFinder(new AllBeforeTypeSectionFinderWrapper(type.getSectionFinder(), this));
	}

	private LinkedList<String> findThese() {
		if (findThese.get() == null) {
			findThese.set(new LinkedList<>());
		}
		return findThese.get();
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		if (!findThese().isEmpty()) {
			List<SectionFinderResult> results = new ArrayList<>(1);
			int start = text.indexOf(findThese().getFirst());
			if (start >= 0) {
				results.add(new SectionFinderResult(start, start
						+ findThese().getFirst().length()));
			}
			findThese().removeFirst();
			return results;
		}
		return null;
	}

	class AllBeforeTypeSectionFinderWrapper implements SectionFinder {

		private final SectionFinder allBeforeThis;

		private final AllBeforeTypeSectionFinder getsAllBefore;

		public AllBeforeTypeSectionFinderWrapper(SectionFinder allBeforeThis, AllBeforeTypeSectionFinder getsAllBefore) {
			this.allBeforeThis = allBeforeThis;
			this.getsAllBefore = getsAllBefore;

		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<SectionFinderResult> found = allBeforeThis.lookForSections(text, father, type);
			if (found != null) {
				int lastEnd = 0;
				for (SectionFinderResult result : found) {
					if (lastEnd < result.getStart()) {
						getsAllBefore.findThese().add(text.substring(lastEnd, result.getStart()).trim());
					}
					lastEnd = result.getEnd();
				}
			}
			return found;
		}

	}

}
