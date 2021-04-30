/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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
import java.util.Set;

import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SectionFinderConstraint;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.turtle.TurtleLiteralType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * High-priority node provider, that matches the whole cell content, potentially quoted, but only for cells in
 * columns with a language-tagged header. It over-rules the other cell types, and always returns tagged string
 * literals.
 */
public class UnquotedStringLiteral extends AbstractType implements NodeProvider<TurtleLiteralType>, SectionFinderConstraint {

	private static final Set<String> LITERAL_PROPERTIES = Set.of("rdfs:label", "skos:prefLabel", "skos:altLabel", "skos:hiddenLabel");

	public UnquotedStringLiteral() {
		setRenderer((section, user, result) -> result.appendJSPWikiMarkup(section.getText()));
		setSectionFinder(new ConstraintSectionFinder(AllTextFinderTrimmed.getInstance(), this));
	}

	@Override
	public Value getNode(OntologyCompiler core, Section<? extends TurtleLiteralType> section) {
		Section<LocaleType> locale = TableUtils.getColumnHeader(section, LocaleType.class);
		Locale lang = locale == null ? Locale.ROOT : locale.get().getLocale(locale);
		String text = Strings.unquote(section.getText());
		// unescape jspwiki text (forced returns and escaped characters) and create the literal
		text = text.replace("\\\\", "\n").replaceAll("~(.)", "$1");
		return core.getRdf2GoCore().createLanguageTaggedLiteral(text, lang);
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		// if the cell's header is not language tagged, do not accept (clear) the match
		if (!isLiteralColumn(father)) found.clear();
	}

	private boolean isLiteralColumn(Section<?> father) {
		Section<TableCellContent> columnHeaderCell = TableUtils.getColumnHeader(father);
		if (columnHeaderCell == null) return false;
		return isLiteralProperty(columnHeaderCell);
	}

	protected boolean isLiteralProperty(@NotNull Section<TableCellContent> columnHeaderCell) {
		if (LITERAL_PROPERTIES.contains(Strings.trim(columnHeaderCell.getText()))) return true;
		return $(columnHeaderCell).successor(LocaleType.class).isNotEmpty();
	}
}
