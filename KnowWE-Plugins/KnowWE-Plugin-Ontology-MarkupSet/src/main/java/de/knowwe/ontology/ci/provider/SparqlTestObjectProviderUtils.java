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

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.ExpectedSparqlResultTableMarkup;
import de.knowwe.ontology.ci.RegisteredNameType;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.ontology.sparql.SparqlMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class SparqlTestObjectProviderUtils {

	public static Collection<Section<SparqlContentType>> getSparqlQueryContentSection(String sectionName) {
		return $(getSectionsForNameGlobal(sectionName, SparqlMarkupType.class)).successor(SparqlContentType.class).asList();
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
		DefaultGlobalCompiler defaultGlobalCompiler = Compilers.getGlobalCompiler(KnowWEUtils.getDefaultArticleManager());
		Section<? extends Type> registeredSections = defaultGlobalCompiler.getTerminologyManager()
				.getTermDefiningSection(new Identifier(clazz.getSimpleName(), sparqlName));
		return $(registeredSections).closest(clazz).asList();
	}
}
