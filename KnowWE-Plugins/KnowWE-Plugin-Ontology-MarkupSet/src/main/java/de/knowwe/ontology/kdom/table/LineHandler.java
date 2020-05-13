/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.kdom.table;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.StatementProvider;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class LineHandler extends OntologyCompileScript<TableLine> {

	@Override
	public void compile(OntologyCompiler compiler, Section<TableLine> section) throws CompilerMessage {
		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		Rdf2GoCore core = compiler.getRdf2GoCore();
		List<Statement> statements = new LinkedList<>();
		Section<NodeProvider> subjectReference = findSubject(section);

		Message typeAnnotationMissing = addStatements(compiler, section, core, statements, subjectReference);

		core.addStatements(section, statements);

		/*
		Error message handling after committing statements
		 */
		if (typeAnnotationMissing != null) {
			throw new CompilerMessage(typeAnnotationMissing);
		}
	}

	@Nullable
	public Message addStatements(OntologyCompiler compiler, Section<TableLine> section, Rdf2GoCore core, List<Statement> statements, Section<NodeProvider> subjectReference) {
		if (subjectReference == null) {
			// obviously no subject in this line, could be an empty table line
			return null;
		}

		//noinspection unchecked
		Value subjectNode = subjectReference.get().getNode(compiler, subjectReference);
		List<Section<OntologyTableCellEntry>> cells = Sections.successors(section, OntologyTableCellEntry.class);
		Set<Value> predicates = new HashSet<>();
		for (Section<OntologyTableCellEntry> cell : cells) {
			Section<ObjectList> objectList = Sections.child(cell, ObjectList.class);
			List<Section<OntologyTableMarkup.OntologyTableTurtleObject>> cellEntries = Sections.children(objectList, OntologyTableMarkup.OntologyTableTurtleObject.class);
			for (Section<OntologyTableMarkup.OntologyTableTurtleObject> objectReference : cellEntries) {

				if (Sections.ancestor(objectReference, TableCellContent.class) != null) {
					Section<Predicate> propertyReference = TableUtils.getColumnHeader(objectReference, Predicate.class);
					if (propertyReference != null) {
						List<Section<StatementProvider>> statementProviders = Sections.successors(
								section, StatementProvider.class);
						for (Section<StatementProvider> statementSection : statementProviders) {
							//noinspection unchecked
							StatementProviderResult result =
									statementSection.get().getStatementsSafe(compiler, statementSection);
							for (Statement statement : result.getStatements()) {
								predicates.add(statement.getPredicate());
								statements.add(statement);
							}
							Messages.storeMessages(section, this.getClass(), result.getMessages());
						}
					}
				}
				else {
					// TODO: clarify whenever this case can make sense...!?
					final StatementProviderResult result =
							objectReference.get().getStatementsSafe(compiler, objectReference);
					statements.addAll(result.getStatements());
				}
			}
		}

		// add default predicate for subject
		for (Section<? extends AnnotationContentType> annotation : DefaultMarkupType.getAnnotationContentSections($(section)
				.ancestor(DefaultMarkupType.class)
				.getFirst(), OntologyTableMarkup.ANNOTATION_DEFAULT_PREDICATE_OBJECT_SET)) {
			Value predicate = $(annotation).successor(Predicate.class).mapFirst(p -> p.get().getNode(compiler, p));
			if (!(predicate instanceof IRI)) continue;
			if (!(subjectNode instanceof Resource)) continue;
			if (predicates.contains(predicate)) continue;
			$(annotation).successor(Object.class)
					.map(o -> o.get().getNode(compiler, o))
					.forEach(o -> statements.add(core.createStatement((Resource) subjectNode, (IRI) predicate, o)));
		}

		/*
		Additionally handle header cell definition;
		A subject is type of the class specified in the first column header
		*/
		Message typeAnnotationMissing = null;
		Section<TableCellContent> cell = Sections.ancestor(subjectReference, TableCellContent.class);
		Section<TableCellContent> rowHeaderCell = TableUtils.getColumnHeader(cell);
		Section<OntologyTableMarkup.BasicURIType> colHeaderConcept = Sections.successor(rowHeaderCell, OntologyTableMarkup.BasicURIType.class);
		if (colHeaderConcept != null) {
			Section<NodeProvider> nodeProviderSection = $(colHeaderConcept)
					.successor(NodeProvider.class)
					.getFirst();
			@SuppressWarnings("unchecked")
			Value headerClassResource = nodeProviderSection.get().getNode(compiler, nodeProviderSection);
			Sections<DefaultMarkupType> markup = $(section).ancestor(DefaultMarkupType.class);
			String typeRelationAnnotationValue = DefaultMarkupType.getAnnotation(markup.getFirst(), OntologyTableMarkup.ANNOTATION_TYPE_RELATION);
			if (typeRelationAnnotationValue != null) {
				org.eclipse.rdf4j.model.URI propertyUri = compiler.getRdf2GoCore()
						.createIRI(typeRelationAnnotationValue);
				statements.add(core.createStatement(compiler.getRdf2GoCore()
						.createIRI(subjectNode.stringValue()), propertyUri, headerClassResource));
			}
			else {
				typeAnnotationMissing = Messages.error("If subject concepts should be defined as instance of the class given in the first column header, a type-relation has to be defined via the typeRelation-typeRelationAnnotationValue. Otherwise, leave the first cell header blank.");
			}
		}
		return typeAnnotationMissing;
	}

	protected Section<NodeProvider> findSubject(Section<TableLine> section) {
		final Section<TableCellContent> firstCell = Sections.successor(section, TableCellContent.class);
		return Sections.successor(firstCell, NodeProvider.class);
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<TableLine> section) {
		Rdf2GoCore core = compiler.getRdf2GoCore();
		core.removeStatements(section);
	}
}
