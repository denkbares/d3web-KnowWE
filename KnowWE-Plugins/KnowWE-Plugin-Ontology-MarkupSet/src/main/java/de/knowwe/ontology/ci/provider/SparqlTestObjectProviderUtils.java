/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.ci.provider;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.ExpectedSparqlResultTableMarkup;
import de.knowwe.ontology.ci.RegisteredNameType;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.ontology.sparql.SparqlMarkupType;
import de.knowwe.rdf2go.Rdf2GoCompiler;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class SparqlTestObjectProviderUtils {

	private static final String SPLIT_SYMBOL = "@";

	public static Collection<Section<SparqlContentType>> getSparqlQueryContentSection(String sectionName) {
		return $(getSectionsForNameGlobal(sectionName, SparqlMarkupType.class)).successor(SparqlContentType.class)
				.asList();
	}

	public static Collection<Section<SparqlMarkupType>> getSparqlQuerySection(String sectionName) {
		return getSectionsForNameGlobal(sectionName, SparqlMarkupType.class);
	}

	public static Collection<Section<ExpectedSparqlResultTableMarkup>> getExpectedQueryResultSection(String sectionName) {
		return getSectionsForNameGlobal(sectionName, ExpectedSparqlResultTableMarkup.class);
	}

	public static String getName(Section<?> section) {
		return $(section).closest(DefaultMarkupType.class)
				.successor(RegisteredNameType.class)
				.mapFirst(s -> s.get().getTermName(s));
	}

	public static <T extends Type> Collection<Section<T>> getSectionsForNameGlobal(String sparqlName, Class<T> clazz) {
		return Stream.of(RegisteredNameType.getNamedMarkupSections(sparqlName, clazz),
				RegisteredNameType.getNamedMarkupSections(sparqlName.split(SPLIT_SYMBOL)[0], clazz))
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Nullable
	public static Rdf2GoCompiler getCompiler(Section<SparqlContentType> section, String name) {
		String[] split = name.split(SPLIT_SYMBOL);
		if (split.length < 2) return Compilers.getCompiler(section, Rdf2GoCompiler.class);
		return Compilers.getCompilers(section, Rdf2GoCompiler.class)
				.stream()
				.filter(c -> Compilers.getCompilerName(c).equals(split[1]))
				.findFirst()
				.orElse(null);
	}
}
