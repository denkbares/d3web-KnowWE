/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom;

import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.utils.Patterns;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.ontology.kdom.namespace.AbbreviationPrefixReference;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.02.2013
 */
public class OntologyUtils {

	public static final String RESOURCE_PATTERN = "(?:" + Patterns.QUOTED + "|[^\" ]+)";

	public static final String ABBREVIATED_RESOURCE_PATTERN = "(?:" +
			AbbreviationPrefixReference.ABBREVIATION_PREFIX_PATTERN + ")?" + RESOURCE_PATTERN;

	public static final SectionFinder ABBREVIATED_RESOURCE_FINDER = new ConstraintSectionFinder(
			new RegexSectionFinder(ABBREVIATED_RESOURCE_PATTERN),
			AtMostOneFindingConstraint.getInstance());

}
