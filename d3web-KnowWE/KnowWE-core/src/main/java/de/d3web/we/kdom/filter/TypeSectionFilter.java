/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.filter;

import de.d3web.we.kdom.Section;

/**
 * a section filter for types
 * 
 * @author Fabian Haupt
 * 
 */
public class TypeSectionFilter implements SectionFilter {

	private String myType;

	public TypeSectionFilter(String type) {
		myType = type;
	}

	@Override
	public boolean accept(Section section) {
		boolean erg = section.getObjectType().getName().equalsIgnoreCase(myType);
		return erg;
	}

}
