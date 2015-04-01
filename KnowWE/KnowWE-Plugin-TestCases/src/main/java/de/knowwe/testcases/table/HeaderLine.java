/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.table.TableLine;

/**
 * The header line of the testcase table.
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class HeaderLine extends TableLine {

	public HeaderLine() {
		setSectionFinder(new ConstraintSectionFinder(
				new RegexSectionFinder(TableLine.LINEPATTERN),
				AtMostOneFindingConstraint.getInstance()));
	}

}
