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
import java.util.Locale;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SectionFinderConstraint;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.EncodedTurtleURI;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleLiteralType;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCompiler;

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
		// use
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
			// add a type that consumes the cell content as a string literal, but only if the header is language-tagged
			addChildType(4, new UnquotedStringLiteral());
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
		public Resource findSubject(Rdf2GoCompiler compiler, Section<?> section) {
			Section<Subject> subjectSection = findSubjectSecTable(section);
			return subjectSection.get().getResource(subjectSection, compiler);
		}

		@Override
		@Nullable
		public Resource findSubject(Rdf2GoCompiler core, StatementProviderResult result, Section<? extends Object> section) {
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

	/**
	 * High-priority node provider, that matches the whole cell content, potentially quoted, but only for cells in
	 * columns with a language-tagged header. It over-rules the other cell types, and always returns tagged string
	 * literals.
	 */
	private static class UnquotedStringLiteral extends AbstractType implements NodeProvider<TurtleLiteralType>, SectionFinderConstraint {

		public UnquotedStringLiteral() {
			setSectionFinder(new ConstraintSectionFinder(AllTextFinderTrimmed.getInstance(), this));
		}

		@Override
		public Value getNode(Section<? extends TurtleLiteralType> section, Rdf2GoCompiler core) {
			Section<LocaleType> locale = TableUtils.getColumnHeader(section, LocaleType.class);
			Locale lang = locale.get().getLocale(locale);
			String text = Strings.unquote(section.getText());
			// unescape jspwiki text (forced returns and escaped characters) and create the literal
			text = text.replace("\\\\", "\n").replaceAll("~(.)", "$1");
			return core.getRdf2GoCore().createLanguageTaggedLiteral(text, lang);
		}

		@Override
		public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
			// if the cell's header is not language tagged, do not accept (clear) the match
			if (TableUtils.getColumnHeader(father, LocaleType.class) == null) found.clear();
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
