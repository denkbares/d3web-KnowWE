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

import java.util.Locale;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.EncodedTurtleURI;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCompiler;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class OntologyTableMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String ANNOTATION_TYPE_RELATION = "typeRelation";

	static {
		MARKUP = new DefaultMarkup("OntologyTable");
		MARKUP.addContentType(createContentTable());
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_TYPE_RELATION, false);
		MARKUP.addAnnotationContentType(ANNOTATION_TYPE_RELATION, new TurtleURI());
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
		ObjectList object = new ObjectList(new OntologyTableTurtleObject());
		// add aux-type to enable drop-area-rendering
		OntologyTableCellEntry cellEntry = new OntologyTableCellEntry(object);
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
		}
	}

	public static class OntologyTableTurtleObject extends Object {

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
		public Resource findSubject(Rdf2GoCompiler compiler, Section<?> section) {
			Section<Subject> subjectSection = findSubjectSecTable(section);
			return subjectSection.get().getResource(subjectSection, compiler);
		}

		@Override
		@Nullable
		public Resource findSubject(Rdf2GoCompiler core, StatementProviderResult result, Section<? extends Object> section) {
			Resource subject = findSubject(core, section);
			if (subject == null) {
				Section<TableCellContent> subjectCell = $(section).ancestor(TableLine.class)
						.successor(TableCellContent.class).getFirst();
				result.addMessage(Messages.error("'" + subjectCell.getText() + "' is not a valid subject."));
			}
			return subject;
		}

		@Override
		public Value getNode(Section<? extends Object> section, Rdf2GoCompiler core) {
			// we get the node from the object,
			// but if the value is a string literal, without a tagged language,
			// we apply a potentially available column lanuage
			Value value = super.getNode(section, core);
			if (!(value instanceof Literal)) return value;

			// according to specification, a non-language tagged literal string value always returns XMLSchema#STRING
			Literal literal = (Literal) value;
			if (!XMLSchema.STRING.equals(literal.getDatatype())) return value;

			// check if there is a column locale specified in the header
			Section<LocaleType> locale = TableUtils.getColumnHeader(section, LocaleType.class);
			if (locale == null) return value;

			// create a new string literal that adds the column language
			String text = literal.stringValue();
			Locale lang = locale.get().getLocale(locale);
			return core.getRdf2GoCore().createLanguageTaggedLiteral(text, lang);
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
