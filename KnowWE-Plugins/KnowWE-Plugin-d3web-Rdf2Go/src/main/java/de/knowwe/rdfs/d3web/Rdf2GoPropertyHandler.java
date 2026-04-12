/*
 * Copyright (C) 2010 denkbares GmbH
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

package de.knowwe.rdfs.d3web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.info.Property;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.PropertyDeclarationType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Adds selected properties to the Rdf2GoCore..
 *
 * @author Albrecht Striffler
 * @created 26.06.2013
 */
public class Rdf2GoPropertyHandler extends OntologyHandler<PropertyDeclarationType> {

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<PropertyDeclarationType> section) {

		// get Property
		Property<?> property = section.get().getProperty(section);
		if (property == null) return Messages.noMessage();

		// get NamedObject
		List<Identifier> objects = section.get().getTermIdentifiers(compiler, section);
		if (objects.isEmpty()) return Messages.asList();

		Locale locale = section.get().getLocale(section);

		// get content
		String content = section.get().getValueString(section);
		if (Strings.isBlank(content)) return Messages.asList();

		List<Statement> statements = new ArrayList<>();
		Rdf2GoCore core = compiler.getRdf2GoCore();
		for (Identifier namedObject : objects) {
			String externalForm = Rdf2GoUtils.getCleanedExternalForm(namedObject);
			// lns:Identifier lns:has[Property] "propertyString"@Locale
			IRI identifierIRI = core.createLocalIRI(externalForm);
			IRI propertyNameIRI = core.createLocalIRI(getD3webPropertyAsOntologyProperty(property));

			Literal contentLiteral = Locales.isEmpty(locale)
					? core.createLiteral(content)
					: core.createLanguageTaggedLiteral(content, locale.getLanguage());
			Rdf2GoUtils.addStatement(core, identifierIRI, propertyNameIRI, contentLiteral, statements);
			core.addStatements(section, Rdf2GoUtils.toArray(statements));
		}

		return Messages.asList();
	}

	public static String getD3webPropertyAsOntologyProperty(Property<?> property) {
		return "has" + StringUtils.capitalize(property.getName());
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<PropertyDeclarationType> section) {
		compiler.getRdf2GoCore().removeStatements(section);
	}
}
