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
package de.knowwe.ontology.kdom.relation;

import java.util.Collection;

import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.ontology.kdom.objectproperty.AbbreviatedPropertyReference;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class RelationDefinition extends AbstractType {

	public RelationDefinition() {
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(new SubjectType());
		this.addChildType(new PredicateType());
		this.addChildType(new LiteralType());
		this.addChildType(new ObjectType());
		this.addCompileScript(Priority.LOWER, new RelationSubtreeHandler());
	}

	private static class SubjectType extends AbstractType {

		public SubjectType() {
			this.setSectionFinder(OntologyUtils.ABBREVIATED_RESOURCE_FINDER);
			this.addChildType(new AbbreviatedResourceReference());
		}
	}

	private static class PredicateType extends AbstractType {

		public PredicateType() {
			this.setSectionFinder(OntologyUtils.ABBREVIATED_RESOURCE_FINDER);
			this.addChildType(new AbbreviatedPropertyReference());
		}
	}

	private static class ObjectType extends AbstractType {

		public ObjectType() {
			this.setSectionFinder(OntologyUtils.ABBREVIATED_RESOURCE_FINDER);
			this.addChildType(new AbbreviatedResourceReference());
		}
	}

	private static class RelationSubtreeHandler extends OntologyHandler<RelationDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<RelationDefinition> section) {

			if (section.hasErrorInSubtree()) {
				Section<LiteralType> literalSection = Sections.child(section,
						LiteralType.class);
				Section<ObjectType> objectSection = Sections.child(section,
						ObjectType.class);
				if (literalSection != null && objectSection != null) {
					return Messages.asList(Messages.error("Multiple objects found. Only one object is allowed per relation."));
				}
				return Messages.noMessage();
			}

			Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);

			Section<SubjectType> subjectSection = Sections.child(section,
					SubjectType.class);
			if (subjectSection == null) {
				return Messages.asList(Messages.error("No subject found for relation definition '"
						+ section.getText() + "'."));
			}
			Section<AbbreviatedResourceReference> abbrSubjectSection = Sections.successor(
					subjectSection, AbbreviatedResourceReference.class);

			URI subjectURI = abbrSubjectSection.get().getResourceURI(core, abbrSubjectSection);

			Section<PredicateType> predicateSection = Sections.child(section,
					PredicateType.class);
			if (predicateSection == null) {
				return Messages.asList(Messages.error("No predicate found for relation definition '"
						+ section.getText() + "'."));
			}
			Section<AbbreviatedPropertyReference> abbrObjPropSection = Sections.successor(
					predicateSection, AbbreviatedPropertyReference.class);

			URI predicatedURI = abbrObjPropSection.get().getPropertyURI(core, abbrObjPropSection);

			Section<ObjectType> objectSection = Sections.child(section,
					ObjectType.class);

			if (objectSection == null) {
				Section<LiteralType> literalSection = Sections.child(section,
						LiteralType.class);
				Literal literal = literalSection.get().getLiteral(core, literalSection);
				core.addStatements(section,
						core.createStatement(subjectURI, predicatedURI, literal));
			}
			else {
				Section<AbbreviatedResourceReference> abbrObjectSection = Sections.successor(
						objectSection, AbbreviatedResourceReference.class);
				URI objectURI = abbrObjectSection.get().getResourceURI(core, abbrObjectSection);

				core.addStatements(section,
						core.createStatement(subjectURI, predicatedURI, objectURI));
			}

			return Messages.noMessage();
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<RelationDefinition> section) {
			Rdf2GoCore.getInstance(compiler).removeStatements(section);
		}

	}
}
