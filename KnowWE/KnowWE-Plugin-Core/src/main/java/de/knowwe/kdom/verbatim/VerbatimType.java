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

/**
 * 
 */
package de.knowwe.kdom.verbatim;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * @author kazamatzuri
 * 
 */
public class VerbatimType extends AbstractType {

	public static VerbatimType instance;

	public static synchronized VerbatimType getInstance() {
		if (instance == null) {
			instance = new VerbatimType();
		}
		return instance;
	}

	public VerbatimType() {
		this.setSectionFinder(new RegexSectionFinder(
				"\\{\\{\\{\\s*(.+?)\\s*\\}\\}\\}", Pattern.DOTALL));
	}

}
