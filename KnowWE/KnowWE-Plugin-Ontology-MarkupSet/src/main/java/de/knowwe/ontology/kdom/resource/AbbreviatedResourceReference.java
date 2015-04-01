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
package de.knowwe.ontology.kdom.resource;

import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.ontology.kdom.namespace.AbbreviationPrefixReference;
import de.knowwe.ontology.kdom.namespace.AbbreviationReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class AbbreviatedResourceReference extends AbstractType {

	public AbbreviatedResourceReference() {
		this(Resource.class);
	}

	public AbbreviatedResourceReference(Class<?> termClass) {
		this.addChildType(new AbbreviationPrefixReference());
		this.addChildType(new ResourceReference(termClass));
		this.setSectionFinder(new AllTextFinderTrimmed());
	}

	public String getResource(Section<? extends AbbreviatedResourceReference> section) {
		Section<ResourceReference> resourceSection = Sections.child(section,
				ResourceReference.class);
		if (resourceSection == null) {
			return null;
		}
		String resource = resourceSection.get().getTermName(resourceSection);
		return resource;
	}

	public String getAbbreviation(Section<? extends AbbreviatedResourceReference> section) {
		Section<AbbreviationPrefixReference> abbreviationPrefixSection = Sections.child(
				section, AbbreviationPrefixReference.class);
		String abbreviation;
		if (abbreviationPrefixSection == null) {
			abbreviation = Rdf2GoCore.LNS_ABBREVIATION;
		}
		else {
			Section<AbbreviationReference> abbreviationSection = Sections.child(
					abbreviationPrefixSection, AbbreviationReference.class);
			if (abbreviationSection == null) {
				return Rdf2GoCore.LNS_ABBREVIATION;
			}
			else {
				abbreviation = abbreviationSection.get().getTermName(abbreviationSection);
			}
		}
		return abbreviation;
	}

	public URI getResourceURI(Rdf2GoCore core, Section<? extends AbbreviatedResourceReference> section) {
		if(core == null) return null;

		String propertyAbbreviation = getAbbreviation(section);
		String property = getResource(section);
		if (property == null) {
			return null;
		}
		return core.createURI(propertyAbbreviation, property);
	}

	public URI getShortURI(Rdf2GoCore core, Section<? extends AbbreviatedResourceReference> section) {
		return core.toShortURI(getResourceURI(core, section));
	}

}
