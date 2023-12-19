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
package de.knowwe.ontology.kdom.clazz;

import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.ontology.kdom.objectproperty.AbbreviatedPropertyDefinition;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class PropertyAnnotationType extends AbstractType {

	public PropertyAnnotationType() {
		AbbreviatedPropertyDefinition propertyDefinition = new AbbreviatedPropertyDefinition();
		propertyDefinition.setSectionFinder(OntologyUtils.OPTIONAL_ABBREVIATED_NS_RESOURCE_FINDER);
		this.addChildType(propertyDefinition);
		AbbreviatedResourceReference classReference = new AbbreviatedResourceReference();
		classReference.setSectionFinder(OntologyUtils.OPTIONAL_ABBREVIATED_NS_RESOURCE_FINDER);
		this.addChildType(classReference);
		this.addCompileScript(Priority.HIGH, new PropertyAnnotationHandler());
		this.setSectionFinder(new AllTextFinderTrimmed());
	}

	private static class PropertyAnnotationHandler extends OntologyHandler<PropertyAnnotationType> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<PropertyAnnotationType> section) {

			Section<DefaultMarkupType> classTypeSection = Sections.ancestor(section,
					DefaultMarkupType.class);

			Section<ContentType> contentSection = Sections.successor(classTypeSection,
					ContentType.class);
			List<Section<AbbreviatedClassDefinition>> classSections = Sections.successors(
					contentSection, AbbreviatedClassDefinition.class);

			Section<AbbreviatedPropertyDefinition> propertySection = Sections.child(
					section, AbbreviatedPropertyDefinition.class);

			if (propertySection == null || propertySection.hasErrorInSubtree()) return Messages.noMessage();

			Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);

			IRI propertyURI = propertySection.get().getPropertyURI(core, propertySection);

			Section<AbbreviatedResourceReference> rangeSection = Sections.child(section,
					AbbreviatedResourceReference.class);

			for (Section<AbbreviatedClassDefinition> classSection : classSections) {
				if (classSection.hasErrorInSubtree()) continue;
				IRI classNameURI = classSection.get().getClassNameURI(core, classSection);
				core.addStatements(section,
						core.createStatement(propertyURI, RDFS.DOMAIN, classNameURI));
			}

			if (rangeSection != null) {
				if (rangeSection.hasErrorInSubtree()) return Messages.noMessage();
				String rangeAbbreviation = rangeSection.get().getAbbreviation(rangeSection);
				String range = rangeSection.get().getResource(rangeSection);
				IRI rangeURI = core.createIRI(rangeAbbreviation, range);
				core.addStatements(section, core.createStatement(propertyURI, RDFS.RANGE, rangeURI));

				if (rangeAbbreviation.equalsIgnoreCase(XSD.PREFIX)) {
					core.addStatements(section, core.createStatement(propertyURI, RDF.TYPE,
							OWL.OBJECTPROPERTY));
				}
				else {
					core.addStatements(section, core.createStatement(propertyURI, RDF.TYPE,
							OWL.OBJECTPROPERTY));
				}
			}
			else {
				core.addStatements(section, core.createStatement(propertyURI, RDF.TYPE,
						RDF.PROPERTY));
			}

			return Messages.noMessage();
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<PropertyAnnotationType> section) {
			Rdf2GoCore.getInstance(compiler).removeStatements(section);
		}
	}
}
