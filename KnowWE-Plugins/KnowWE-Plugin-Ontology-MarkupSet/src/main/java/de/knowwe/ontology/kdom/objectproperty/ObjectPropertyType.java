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
package de.knowwe.ontology.kdom.objectproperty;

import java.util.Collection;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.OntologyLineType;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ObjectPropertyType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	private static final String DOMAIN_ANNOTATION_NAME = "domain";
	private static final String RANGE_ANNOTATION_NAME = "range";
	private static final String FUNCTIONAL_ANNOTATION_NAME = "functional";

	static {
		MARKUP = new DefaultMarkup("ObjectProperty");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(DOMAIN_ANNOTATION_NAME, false);
		MARKUP.addAnnotation(FUNCTIONAL_ANNOTATION_NAME, false, "true", "false");
		MARKUP.addAnnotationContentType(DOMAIN_ANNOTATION_NAME, new DomainRangeAnnotationType(
				DomainRangeAnnotationType.DomainRange.DOMAIN));
		MARKUP.addAnnotation(RANGE_ANNOTATION_NAME, false);
		MARKUP.addAnnotationContentType(RANGE_ANNOTATION_NAME, new DomainRangeAnnotationType(
				DomainRangeAnnotationType.DomainRange.RANGE));

		OntologyLineType lineType = new OntologyLineType();
		AbbreviatedPropertyDefinition propertyDefinition = new AbbreviatedPropertyDefinition();
		propertyDefinition.addCompileScript(new AbbreviatedObjectPropertyHandler());
		lineType.addChildType(propertyDefinition);
		MARKUP.addContentType(lineType);
	}

	public ObjectPropertyType() {
		super(MARKUP);
	}

	private static class AbbreviatedObjectPropertyHandler extends OntologyHandler<AbbreviatedPropertyDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<AbbreviatedPropertyDefinition> section) {
			Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);
			String namespace = core.getNamespaces().get(section.get().getAbbreviation(section));
			if (namespace == null) return Messages.noMessage();
			String property = section.get().getResource(section);
			URI propertyURI = core.createURI(namespace, property);
			core.addStatements(section, core.createStatement(propertyURI, RDF.TYPE, RDF.PROPERTY));

			/*
			 * set functional property if specified
			 */
			Section<DefaultMarkupType> markup = Sections.ancestor(section,
					DefaultMarkupType.class);
			String annotation = DefaultMarkupType.getAnnotation(markup, FUNCTIONAL_ANNOTATION_NAME);
			if ("true".equalsIgnoreCase(annotation)) {
				core.addStatements(section,
						core.createStatement(propertyURI, RDF.TYPE, OWL.FUNCTIONALPROPERTY));

			}

			return Messages.noMessage();
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<AbbreviatedPropertyDefinition> section) {
			Rdf2GoCore.getInstance(compiler).removeStatements(section);
		}
	}

	static class DomainRangeAnnotationType extends AbbreviatedResourceReference {

		public enum DomainRange {
			DOMAIN, RANGE
		}

		/**
		 * 
		 */
		public DomainRangeAnnotationType(final DomainRange kind) {
			this.addCompileScript(new OntologyHandler<DomainRangeAnnotationType>() {

				@Override
				public Collection<Message> create(OntologyCompiler compiler, Section<DomainRangeAnnotationType> section) {

					Section<DefaultMarkupType> defaultMarkup = Sections.ancestor(section,
							DefaultMarkupType.class);
					Section<AbbreviatedPropertyDefinition> propertySec = Sections.successor(
							defaultMarkup, AbbreviatedPropertyDefinition.class);
					Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);
					URI propertyURI = propertySec.get().getResourceURI(core, propertySec);

					URI objectURI = section.get().getResourceURI(core, section);

					URI predicateURI = RDFS.RANGE;
					if (kind == DomainRange.DOMAIN) {
						predicateURI = RDFS.DOMAIN;
					}

					if (objectURI != null) {
						core.addStatements(section, core.createStatement(propertyURI, predicateURI,
								objectURI));
					}
					return null;
				}

				@Override
				public void destroy(OntologyCompiler compiler, Section<DomainRangeAnnotationType> section) {
					Rdf2GoCore.getInstance(compiler).removeStatements(section);
				}

			});
		}
	}
}
