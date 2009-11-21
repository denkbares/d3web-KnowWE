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

package de.d3web.we.refactoring;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

/**
 * This is an simple example type for the Hello-World Plugin
 * to demonstrate what a type is and what it can do
 * @author Franz Schwab
 */
public class RefactoringType extends DefaultAbstractKnowWEObjectType{
	
	@Override
	public void init() {
		/* Every custom type needs a SectionFinder to get recognized in the pages.
		 * @see de.d3web.we.kdom.sectionFinder.SectionFinder
		 * 
		 * You can create your own, or reuse own from the
		 * KnowWE core library like done here:
		 */
		this.sectionFinder = new RegexSectionFinder("Hello Refactoring!");
		
		
		/* A custom type may define a custom renderer
		 * to define how the type occurrences look like
		 * in the wiki page view.
		 * 
		 */
		this.setCustomRenderer(new RefactoringRenderer());
	}

}
