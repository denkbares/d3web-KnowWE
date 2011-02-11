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

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;

public class AllBeforeTypeSectionFinder extends SectionFinder {

	KnowWEObjectType markerType = null;

	private final AllTextFinderTrimmed allTextFinderTrimmed = new AllTextFinderTrimmed();

	private AllBeforeTypeSectionFinder(KnowWEObjectType type) {
		this.markerType = type;
	}

	public static ISectionFinder createFinder(KnowWEObjectType type) {

		ConstraintSectionFinder f = new ConstraintSectionFinder(
				new AllBeforeTypeSectionFinder(type));
		f.addConstraint(SingleChildConstraint.getInstance());
		return f;

	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		// note this indexOf call is unsafe - wrong matches are caught by
		// SingelChildConstraint
		Section s = father.getChildSectionAtPosition(father.getOriginalText().indexOf(
				text)
				+ text.length());
		if (s != null && s.getObjectType().getName().equals(markerType.getName())) {
			return allTextFinderTrimmed.lookForSections(text, father, type);
		}

		return null;
	}

}
