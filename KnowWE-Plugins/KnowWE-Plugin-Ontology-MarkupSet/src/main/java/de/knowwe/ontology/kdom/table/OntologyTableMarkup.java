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

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.turtle.BlankNode;
import de.knowwe.ontology.turtle.EncodedTurtleURI;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.PredicateObjectSentenceList;
import de.knowwe.ontology.turtle.PredicateSentence;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleSentence;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.ontology.turtle.compile.StatementProvider;
import de.knowwe.ontology.turtle.compile.StatementProviderResult;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCompiler;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class OntologyTableMarkup extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	public static final String ANNOTATION_TYPE_RELATION = "typeRelation";


	static {
		MARKUP = new DefaultMarkup("OntologyTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_TYPE_RELATION, false);
		MARKUP.addAnnotationContentType(ANNOTATION_TYPE_RELATION, new TurtleURI());


		/*
		Cell 0,0
		 */
		BasicURIType cell00 = new BasicURIType();
		cell00.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 0, 1)));
		content.injectTableCellContentChildtype(cell00);

		/*
		First column: cells 0, 1-n
		 */
		Subject resource = new Subject(new TableSubjectURIWithDefinition());
		resource.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(resource);

		/*
		Header Row: cells 1-n, 0
		 */
		Predicate property = new Predicate();
		property.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 0, 1)));
		content.injectTableCellContentChildtype(property);

		/*
		Inner cell entries: cells 1-n,1-n
		 */
		ObjectList object = new ObjectList(new OntologyTableTurtleObject());
		// add aux-type to enable drop-area-rendering
		OntologyTableCellEntry cellEntry = new OntologyTableCellEntry(object);
		cellEntry.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(cellEntry);

	}

	public OntologyTableMarkup() {
		super(MARKUP);
	}


	public static class OntologyTableTurtleObject extends Object {

		/*
		@Override
		public StatementProviderResult getStatements(Section<? extends Object> section, Rdf2GoCompiler core) {

			StatementProviderResult result = new StatementProviderResult();

			List<Section<StatementProvider>> bNodePOSentenceList = Sections.successors(section, StatementProvider.class);
			for (Section<StatementProvider> statementProviderSection : bNodePOSentenceList) {
				if(statementProviderSection.equals(section)) continue;
				StatementProviderResult statements = statementProviderSection.get()
						.getStatements(statementProviderSection, core);
				for (Statement statement : statements.getStatements()) {
					result.addStatement(statement);
				}
			}
			return result;
		}
		*/

		@Override
		public Section<Predicate> getPredicateSection(Section<? extends Object> section) {
			return TableUtils.getColumnHeader(section, Predicate.class);
		}

		public Section<Subject> findSubjectSec(Section<?> object) {
			Section<TableLine> line = Sections.ancestor(object, TableLine.class);
			return findSubjectInLine(line);
		}

		private Section<Subject> findSubjectInLine(Section<TableLine> section) {
			final Section<TableCellContent> firstCell = Sections.successor(section, TableCellContent.class);
			return Sections.successor(firstCell, Subject.class);
		}


		@Override
		public @Nullable Resource getSubject(Rdf2GoCompiler core, StatementProviderResult result, boolean termError, Section<? extends Object> section) {
			Resource subject;

				Section<Subject> subjectSection = findSubjectSec(section);
				subject = subjectSection.get().getResource(subjectSection, core);

				// check term definition
				Section<TurtleURI> turtleURITermSubject = Sections.child(subjectSection,
						TurtleURI.class);
				if (turtleURITermSubject != null && Object.STRICT_COMPILATION) {
					boolean isDefined = checkTurtleURIDefinition(turtleURITermSubject);
					if (!isDefined) {
						// error message is already rendered by term reference
						// renderer
						// we do not insert statement in this case
						subject = null;
						termError = true;
					}
				}

				if (subject == null && !termError) {
					result.addMessage(Messages.error("'" + subjectSection.getText()
							+ "' is not a valid subject."));
				}
			return subject;
		}

	}

	static class BasicURIType extends AbstractType {
		public BasicURIType() {
			this.setSectionFinder( new AllTextFinderTrimmed());
			this.addChildType(new EncodedTurtleURI());
			this.addChildType(new TurtleURI());
			this.addChildType(new LazyURIReference());
		}
	}
}
