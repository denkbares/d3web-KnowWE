/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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

import de.knowwe.core.kdom.sectionFinder.MultiSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.NoEmptySectionsConstraint;

/**
 * 
 * * A SplitSectionFinder that does not create sections of length 0.
 * 
 * 
 * @author Jochen Reutelshöfer
 * @created 28.11.2012
 */
public class SplitSectionFinderUnquotedNonEmpty extends ConstraintSectionFinder {

	public SplitSectionFinderUnquotedNonEmpty(String... keys) {
		super(createDelegate(keys), NoEmptySectionsConstraint.getInstance());
	}

	private static SectionFinder createDelegate(String[] keys) {
		if (keys.length == 1) {
			return new SplitSectionFinderUnquoted(keys[0]);
		}
		else {
			MultiSectionFinder finder = new MultiSectionFinder();
			for (String key : keys) {
				finder.addSectionFinder(new SplitSectionFinderUnquoted(key));
			}
			return finder;
		}
	}
}
