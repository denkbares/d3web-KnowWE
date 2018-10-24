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
package de.knowwe.ontology.kdom.sparql;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.FilterSectionFinder;
import de.knowwe.ontology.turtle.TurtleURI;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 14.01.2014
 */
public class SparqlShortURIReference extends AbstractType {

	public SparqlShortURIReference() {
		this.setSectionFinder(new FilterSectionFinder(new RegexSectionFinder("\\w+:[\\w_\\-%]+")) {
			@Override
			protected boolean filter(SectionFinderResult match, Section<?> father) {
				// check whether the match is in open quotes
				return Strings.countUnescapedQuotes(father.getText().substring(0, match.getStart()), '"') % 2 == 0;
			}
		});
		this.addChildType(new TurtleURI());
	}

}
