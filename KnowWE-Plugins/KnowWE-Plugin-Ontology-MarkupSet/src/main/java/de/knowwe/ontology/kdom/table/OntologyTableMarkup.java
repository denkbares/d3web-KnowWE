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

import org.eclipse.rdf4j.model.Resource;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.EncodedTurtleURI;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.PredicateObjectSentenceList;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class OntologyTableMarkup extends DefaultMarkupType {

	protected static final DefaultMarkup MARKUP;

	public static final String ANNOTATION_TYPE_RELATION = "typeRelation";
	public static final String ANNOTATION_DEFAULT_PREDICATE_OBJECT_SET = "defaultPredicateObjectSet";

	static {
		MARKUP = new DefaultMarkup("OntologyTable");
		MARKUP.addContentType(createContentTable());
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_TYPE_RELATION, false);
		MARKUP.addAnnotationContentType(ANNOTATION_TYPE_RELATION, new TurtleURI());
		MARKUP.getAnnotation(ANNOTATION_TYPE_RELATION).setDocumentation("By setting this annotation, the concepts of " +
				"the first column will get the concept in the header of the first column with the concept given in " +
				"the annotation as the predicate.\n<br>" +
				"The resulting statement for each cell in the first column will be:<br>\n " +
				"S: Column 1 Cell, P: annotation, O: Column 1 Header");

		MARKUP.addAnnotation(ANNOTATION_DEFAULT_PREDICATE_OBJECT_SET, false);
		PredicateObjectSentenceList type = PredicateObjectSentenceList.getInstance();
		MARKUP.addAnnotationContentType(ANNOTATION_DEFAULT_PREDICATE_OBJECT_SET, type);
		MARKUP.getAnnotation(ANNOTATION_DEFAULT_PREDICATE_OBJECT_SET)
				.setDocumentation("Specify predicate and object (or " +
						"object list) using this annotation. Predicate and object(s) will be added to every concept of the " +
						"first column, as long as there is no column with the same predicate in the header and a different " +
						"(non empty) entry in its cell for the same row.");
	}

	public OntologyTableMarkup() {
		super(MARKUP);
	}

	public static Table createContentTable() {
		Table table = new Table();
		/*
		Cell 0,0
		 */
		BasicURIType cell00 = new BasicURIType();
		cell00.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 0, 1)));
		table.injectTableCellContentChildtype(cell00);

		/*
		First column: cells 0, 1-n
		 */
		Subject resource = new Subject(new TableSubjectURIWithDefinition());
		resource.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));
		table.injectTableCellContentChildtype(resource);

		/*
		Header Row: cells 1-n, 0
		in Addition the cell header may allow to specify a default language-tag
		 */
		TableIndexConstraint constraint = new TableIndexConstraint(1, Integer.MAX_VALUE, 0, 1);
		Predicate property = new Predicate();
		property.setSectionFinder(new ConstraintSectionFinder(new AllTextFinderTrimmed(), constraint));
		table.injectTableCellContentChildtype(new ColumnLocale(constraint));
		table.injectTableCellContentChildtype(property);

		/*
		Inner cell entries: cells 1-n,1-n
		 */
		// add aux-type to enable drop-area-rendering
		OntologyTableCellEntry cellEntry = new OntologyTableCellEntry(new ObjectList(new OntologyTableTurtleObject()));
		cellEntry.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
		table.injectTableCellContentChildtype(cellEntry);

		return table;
	}

	private static class ColumnLocale extends AbstractType {
		public ColumnLocale(TableIndexConstraint constraint) {
			setSectionFinder(new ConstraintSectionFinder(new RegexSectionFinder("(?i)@[a-z\\-_]+\\s*$"), constraint));
			addChildType(new LocaleType("@"));
			addChildType(new KeywordType("@"));
			addChildType(UnrecognizedSyntaxType.getInstance());
		}
	}

	public static class OntologyTableTurtleObject extends Object {

		public OntologyTableTurtleObject() {
			SectionFinder objectSectionFinder = this.getSectionFinder();
			this.setSectionFinder((text, father, type) -> {
				// If we have a column with a locale header and no quotes are used, we consider the cell to be exactly one literal
				// Reason: Otherwise, cells containing comma would be split at the comma, which is very likely not what we want.
				// If we want multiple literals in one cell, we can still use quoted literals.
				if (TableUtils.getColumnHeader(father, LocaleType.class) != null && !text.contains("\"")) {
					return AllTextFinderTrimmed.getInstance().lookForSections(text, father, type);
				}
				else {
					return objectSectionFinder.lookForSections(text, father, type);
				}
			});
			// add a type that consumes the cell content as a string literal, but only if the header is language-tagged
			addChildType(4, new LiteralColumnCell());
		}

		@Override
		public Section<Predicate> getPredicateSection(Section<? extends Object> section) {
			return TableUtils.getColumnHeader(section, Predicate.class);
		}

		public Section<Subject> findSubjectSecTable(Section<?> object) {
			Section<TableLine> line = Sections.ancestor(object, TableLine.class);
			return findSubjectInLine(line);
		}

		private Section<Subject> findSubjectInLine(Section<TableLine> section) {
			return Sections.successor(section, Subject.class);
		}

		@Override
		public Resource findSubject(OntologyCompiler compiler, Section<?> section) {
			Section<Subject> subjectSection = findSubjectSecTable(section);
			return subjectSection.get().getResource(compiler, subjectSection);
		}

		@Override
		@Nullable
		public Resource findSubject(OntologyCompiler core, StatementProviderResult result, Section<? extends Object> section) {
			Resource subject = findSubject(core, section);
			if (subject == null) {
				Section<Subject> subjectSection = findSubjectSecTable(section);
				// add error message, but only if there is not already an error message in the subject itself
				if (Messages.getMessagesFromSubtree(core, subjectSection, Message.Type.ERROR).isEmpty()) {
					result.addMessage(Messages.error("'" + subjectSection.getText() + "' is not a valid subject."));
				}
			}
			return subject;
		}
	}

	public static class BasicURIType extends AbstractType {
		public BasicURIType() {
			this.setSectionFinder(new AllTextFinderTrimmed());
			this.addChildType(new EncodedTurtleURI());
			this.addChildType(new TurtleURI());
			this.addChildType(new LazyURIReference());
		}
	}
}
