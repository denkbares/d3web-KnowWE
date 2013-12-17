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

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.RDFS;

import de.knowwe.core.compile.packaging.PackageAnnotationNameType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.OntologyLineType;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ObjectPropertyType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	private static final String DOMAIN_ANNOTATION_NAME = "domain";
	private static final String RANGE_ANNOTATION_NAME = "range";

	static {
		MARKUP = new DefaultMarkup("ObjectProperty");
		MARKUP.addAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME, false);
		MARKUP.addAnnotationNameType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageAnnotationNameType());
		MARKUP.addAnnotationContentType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageTerm(true));
		MARKUP.addAnnotation(DOMAIN_ANNOTATION_NAME, false);
		MARKUP.addAnnotationContentType(DOMAIN_ANNOTATION_NAME, new DomainRangeAnnotationType(
				DomainRangeAnnotationType.DomainRange.DOMAIN));
		MARKUP.addAnnotation(RANGE_ANNOTATION_NAME, false);
		MARKUP.addAnnotationContentType(RANGE_ANNOTATION_NAME, new DomainRangeAnnotationType(
				DomainRangeAnnotationType.DomainRange.RANGE));

		OntologyLineType lineType = new OntologyLineType();
		AbbreviatedPropertyDefinition propertyDefinition = new AbbreviatedPropertyDefinition();
		propertyDefinition.addSubtreeHandler(new AbbreviatedObjectPropertyHandler());
		lineType.addChildType(propertyDefinition);
		MARKUP.addContentType(lineType);
	}

	public ObjectPropertyType() {
		super(MARKUP);
	}

	private static class AbbreviatedObjectPropertyHandler extends SubtreeHandler<AbbreviatedPropertyDefinition> {

		@Override
		public Collection<Message> create(Article article, Section<AbbreviatedPropertyDefinition> section) {
			Rdf2GoCore core = Rdf2GoCore.getInstance(article);
			String namespace = core.getNameSpaces().get(section.get().getAbbreviation(section));
			if (namespace == null) return Messages.noMessage();
			String property = section.get().getResource(section);
			URI propertyURI = core.createURI(namespace, property);
			core.addStatements(section, core.createStatement(propertyURI, RDF.type, RDF.Property));
			return Messages.noMessage();
		}
	}

	static class DomainRangeAnnotationType extends AbbreviatedResourceReference {

		public enum DomainRange {
			DOMAIN, RANGE
		};

		/**
		 * 
		 */
		public DomainRangeAnnotationType(final DomainRange kind) {
			this.addSubtreeHandler(new SubtreeHandler<DomainRangeAnnotationType>() {

				@Override
				public Collection<Message> create(Article article, Section<DomainRangeAnnotationType> section) {

					Section<DefaultMarkupType> defaultMarkup = Sections.findAncestorOfType(section,
							DefaultMarkupType.class);
					Section<AbbreviatedPropertyDefinition> propertySec = Sections.findSuccessor(
							defaultMarkup, AbbreviatedPropertyDefinition.class);
					Rdf2GoCore core = Rdf2GoCore.getInstance(article);
					URI propertyURI = propertySec.get().getResourceURI(core, propertySec);

					URI objectURI = section.get().getResourceURI(core, section);

					URI predicateURI = RDFS.range;
					if (kind.equals(DomainRange.DOMAIN)) {
						predicateURI = RDFS.domain;
					}

					if (objectURI != null) {
						core.addStatements(section,
								core.createStatement(propertyURI, predicateURI, objectURI));
					}
					return null;
				}

			});
		}
	}
}
