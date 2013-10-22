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

import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.ontology.kdom.namespace.AbbreviationPrefixReference;
import de.knowwe.ontology.kdom.namespace.AbbreviationReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class AbbreviatedResourceDefinition extends SimpleDefinition {

	public AbbreviatedResourceDefinition() {
		this(Resource.class);
	}

	public AbbreviatedResourceDefinition(Class<?> termClass) {
		super(TermRegistrationScope.LOCAL, termClass);
		this.addChildType(new AbbreviationPrefixReference());
		this.addChildType(new ResourceDefinition());
		this.setSectionFinder(new AllTextFinderTrimmed());
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		String abbreviation = getAbbreviation(section);
		String resource = getResource(section);
		return toTermName(abbreviation, resource);
	}

	public static String toTermName(String abbreviation, String resource) {
		return abbreviation + ":" + resource;
	}

	public String getResource(Section<? extends Term> section) {
		Section<ResourceDefinition> resourceSection = Sections.findChildOfType(section,
				ResourceDefinition.class);
		String resource = resourceSection.get().getTermName(resourceSection);
		return resource;
	}

	public String getAbbreviation(Section<? extends Term> section) {
		Section<AbbreviationPrefixReference> abbreviationPrefixSection = Sections.findChildOfType(
				section, AbbreviationPrefixReference.class);
		String abbreviation;
		if (abbreviationPrefixSection == null) {
			abbreviation = Rdf2GoCore.LNS_ABBREVIATION;
		}
		else {
			Section<AbbreviationReference> abbreviationSection = Sections.findChildOfType(
					abbreviationPrefixSection, AbbreviationReference.class);
			abbreviation = abbreviationSection.get().getTermName(abbreviationSection);
		}
		return abbreviation;
	}

	public URI getResourceURI(Rdf2GoCore core, Section<? extends AbbreviatedResourceDefinition> section) {
		String propertyAbbreviation = getAbbreviation(section);
		String property = getResource(section);
		return core.createURI(propertyAbbreviation, property);
	}

}
