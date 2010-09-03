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

/**
 * @author Jochen
 * 
 *         this constraint is only used a marker
 * 
 */
public class ExclusiveType implements SectionFinderConstraint {

	private static ExclusiveType instance = null;

	public static ExclusiveType getInstance() {
		if (instance == null) {
			instance = new ExclusiveType();

		}

		return instance;
	}

	private ExclusiveType() {

	}

	@Override
	public void filterCorrectResults(List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		// this constraint is only used a marker

	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		// this constraint is only used a marker
		return false;
	}

}
