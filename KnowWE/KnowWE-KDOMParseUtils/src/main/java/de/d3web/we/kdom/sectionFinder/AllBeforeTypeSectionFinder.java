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

package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sectionizable;
import de.d3web.we.kdom.Type;

public class AllBeforeTypeSectionFinder implements ISectionFinder {

	Sectionizable markerType = null;

	LinkedList<String> findThese = new LinkedList<String>();

	/**
	 * To use this SectionFinder, the argument type has to be added as an
	 * allowed children to the parent type directly before the type with this
	 * SectionFinder is added to the parent type.
	 * <p/>
	 * <b>Examples:</b>
	 * <pre>
	 * public ParentType() {
	 * 
	 * 	FirstType firstType = new FirstType();
	 * 	SecondType secondType = new SecondType();
	 * 
	 * 	secondType.setSectionFinder(new AllBeforeTypeSectionFinder(firstType));
	 * 
	 * 	this.childrenTypes.add(firstType);
	 * 	this.childrenTypes.add(secondType);
	 * 
	 * }
	 * </pre>
	 * 
	 */
	public AllBeforeTypeSectionFinder(Sectionizable type) {
		this.markerType = type;
		type.setSectionFinder(new AllBeforeTypeSectionFinderWrapper(type.getSectioFinder(), this));
	}

	public void addStringToFind(String findThis) {
		findThese.add(findThis);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		if (!findThese.isEmpty()) {
			List<SectionFinderResult> results = new ArrayList<SectionFinderResult>(findThese.size());
			int start = text.indexOf(findThese.getFirst());
			if (start >= 0) {
				results.add(new SectionFinderResult(start, findThese.getFirst().length()));
				findThese.removeFirst();
			}
			return results;
		}
		return null;
	}

	class AllBeforeTypeSectionFinderWrapper implements ISectionFinder {

		private final ISectionFinder allBeforeThis;

		private final AllBeforeTypeSectionFinder getsAllBefore;

		public AllBeforeTypeSectionFinderWrapper(ISectionFinder allBeforeThis, AllBeforeTypeSectionFinder getsAllBefore) {
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
						getsAllBefore.addStringToFind(text.substring(lastEnd, result.getStart()).trim());
					}
					lastEnd = result.getEnd();
				}
			}
			return found;
		}

	}

}
