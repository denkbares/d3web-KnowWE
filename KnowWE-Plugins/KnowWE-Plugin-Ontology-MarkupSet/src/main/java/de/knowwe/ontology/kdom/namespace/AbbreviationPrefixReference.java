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
package de.knowwe.ontology.kdom.namespace;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.rdf2go.Rdf2GoCore;

public class AbbreviationPrefixReference extends AbstractType {

	public static final String ABBREVIATION_PREFIX_PATTERN = "^\\s*\\w*:\\s*";

	public AbbreviationPrefixReference() {
		this.setSectionFinder(new ConstraintSectionFinder(new RegexSectionFinder(
				ABBREVIATION_PREFIX_PATTERN),
				AtMostOneFindingConstraint.getInstance()));
		this.addChildType(new AbbreviationReference());
	}

	public String getAbbreviation(Section<? extends AbbreviationPrefixReference> abbreviationPrefixSection) {
		String abbreviation;
		if (abbreviationPrefixSection == null) {
			abbreviation = Rdf2GoCore.LNS_ABBREVIATION;
		}
		else {
			Section<AbbreviationReference> abbreviationSection = Sections.child(
					abbreviationPrefixSection, AbbreviationReference.class);
			if (abbreviationSection == null) {
				return "";
			}
			else {
				abbreviation = abbreviationSection.get().getTermName(abbreviationSection);
			}
		}
		return abbreviation;
	}

}
