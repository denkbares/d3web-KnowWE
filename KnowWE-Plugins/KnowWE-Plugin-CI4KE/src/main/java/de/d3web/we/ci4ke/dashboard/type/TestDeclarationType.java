/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.d3web.we.ci4ke.dashboard.type;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;

/**
 * Class to parse a test declaration and its parameters
 * 
 * @author volker_belli
 * @created 15.09.2012
 */
public class TestDeclarationType extends AbstractType {

	public TestDeclarationType() {
		// first is test-name, second is test-object,
		// following are the arguments
		addChildType(new ParameterType());

		// add finder to get the whole text before the first ignore
		ConstraintSectionFinder finder = new ConstraintSectionFinder(AllTextFinder.getInstance());
		finder.addConstraint(SingleChildConstraint.getInstance());
		finder.addConstraint(AtMostOneFindingConstraint.getInstance());
		setSectionFinder(finder);
	}
}
