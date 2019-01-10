/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.ci.provider;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.ci.ExpectedSparqlResultTableMarkup;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class SparqlExpectedResultSection {

	private final Section<ExpectedSparqlResultTableMarkup> section;

	public SparqlExpectedResultSection(Section<ExpectedSparqlResultTableMarkup> section) {
		this.section = section;
	}

	public Section<ExpectedSparqlResultTableMarkup> getSection() {
		return section;
	}
}
