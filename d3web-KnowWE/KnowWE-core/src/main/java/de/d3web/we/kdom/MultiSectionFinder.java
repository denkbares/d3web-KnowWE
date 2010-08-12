/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Jochen
 * 
 *         The MultiSectionFinder allows to combine multiple SectionFinder for
 *         one type (i.e. alternative syntax). It contains a list of
 *         SectionFinders, call them all, and returns all FindingResults to the
 *         parser
 * 
 *         WARNING: As the different SectionFinder are called independently with
 *         the same text, they possibly might allocate overlapping sections. The
 *         resulting (invalid) SectionFinderResult-Set will be returned to the
 *         parsing-algorithm which THEN reject all these findings (if any
 *         invalid allocations contained).
 * 
 */

public class MultiSectionFinder implements ISectionFinder {

	private List<ISectionFinder> finders = null;

	public MultiSectionFinder() {
		this.finders = new ArrayList<ISectionFinder>();
	}

	public MultiSectionFinder(ISectionFinder first) {
		this();
		this.addSectionFinder(first);
	}

	public MultiSectionFinder(List<ISectionFinder> initialList) {
		this.finders = initialList;
	}

	public void addSectionFinder(ISectionFinder f) {
		this.finders.add(f);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();

		// iterates all finders and gathers together all SectionFinderResults
		for (ISectionFinder finder : finders) {
			List<SectionFinderResult> singleResult = finder.lookForSections(text, father,
					type);
			if (singleResult != null) {
				results.addAll(singleResult);
			}
		}
		return results;
	}

}
