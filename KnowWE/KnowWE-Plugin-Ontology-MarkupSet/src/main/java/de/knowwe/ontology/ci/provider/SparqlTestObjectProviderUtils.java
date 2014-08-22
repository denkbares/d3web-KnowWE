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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.ExpectedSparqlResultTable;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.sparql.SparqlMarkupType;


/**
 * 
 * @author jochenreutelshofer
 * @created 10.01.2014
 */
public class SparqlTestObjectProviderUtils {

	public static Collection<Section<SparqlContentType>> getSparqlQueryContentSection(String sectionName) {
		return getSectionsForNameGlobal(sectionName, SparqlContentType.class);
	}

	public static Collection<Section<SparqlMarkupType>> getSparqlQuerySection(String sectionName) {
		return getSectionsForNameGlobal(sectionName, SparqlMarkupType.class);
	}

	public static Collection<Section<ExpectedSparqlResultTable>> getExpectedQueryResultSection(String sectionName) {
		return getSectionsForNameGlobal(sectionName, ExpectedSparqlResultTable.class);
	}

	public static String getName(Section<?> section) {
		Section<DefaultMarkupType> defaultMarkup = Sections.ancestor(section,
				DefaultMarkupType.class);
		if (defaultMarkup == null) return null;
		return DefaultMarkupType.getAnnotation(defaultMarkup, "name");
	}

	public static <T extends Type> Collection<Section<T>> getSectionsForNameGlobal(String sparqlName, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<Section<T>>();
		
		Collection<Section<? extends Type>> sparqlSections = Sections.successors(
				Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB), clazz
		);

		for (Section<? extends Type> section : sparqlSections) {
			String name = getName(section);
			if (name != null && sparqlName.matches(name)) {
				result.add(Sections.cast(section, clazz));
			}
		}

		return result;
	}
}
