/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * 
 * @author Jochen
 * @created 29.07.2010 
 */
public abstract class ConditionalSectionFinder implements ISectionFinder {
	
	ISectionFinder finder = null;
	
	public ConditionalSectionFinder(ISectionFinder internalFinder) {
		this.finder = internalFinder;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		if (text.length() > 0) {
			if (condition(text, father)) {
				return finder.lookForSections(text, father, type);
			}
		}
		return result;
	}
	
	protected abstract boolean condition(String text, Section<?> father);

}
