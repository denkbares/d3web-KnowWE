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

package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.Section;



/**
 * @author Jochen
 *
 */
public abstract class SectionFinder {
	

	/**
	 * 
	 * Allocates text parts for this module. The resulting array contains substrings of the 
	 * passed text. These specified substrings will be allocated to this module. 
	 * Method will be called multiple times with various article fragments depending on 
	 * previous allocations of preceding modules.
	 * If no interesting section is found in a passed fragment, return 'null' or an array of length 0; 
	 * @param text Text fragment of the wiki article source
	 * @param father TODO
	 * @return List of SectionFinderResults with informations about what part of the next belongs
	 * to the ObjectType calling the SectionFinder
	 */
	public abstract List<SectionFinderResult> lookForSections(String text, Section father);

}
