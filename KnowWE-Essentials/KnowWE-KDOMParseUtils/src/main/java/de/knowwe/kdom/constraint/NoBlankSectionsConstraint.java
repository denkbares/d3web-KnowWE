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

package de.knowwe.kdom.constraint;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A constraint that prevents the creation of sections only consists of
 * whitespace characters.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 24.09.2013
 */
public class NoBlankSectionsConstraint extends AbstractFilterConstraint {

	private static final NoBlankSectionsConstraint instance = new NoBlankSectionsConstraint();

	public static NoBlankSectionsConstraint getInstance() {
		return instance;
	}

	private NoBlankSectionsConstraint() {
	}

	@Override
	public boolean accept(String text, SectionFinderResult result) {
		return !Strings.isBlank(text.substring(result.getStart(), result.getEnd()));
	}

}
