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

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class FirstOccurrenceOfFinder extends AbstractSingleResultFinder {

	private final SectionFinder finder;

	public FirstOccurrenceOfFinder(String regex) {
		this(new RegexSectionFinder(regex));
	}

	public FirstOccurrenceOfFinder(SectionFinder f) {
		this.finder = f;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section<?> father, Type type) {
		List<SectionFinderResult> secs = finder.lookForSections(text, father, type);
		if (secs.size() >= 1) return secs.get(0);
		return null;
	}
}
