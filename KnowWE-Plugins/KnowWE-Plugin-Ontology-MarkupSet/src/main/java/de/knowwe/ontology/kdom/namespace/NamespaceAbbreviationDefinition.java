/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom.namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryException;

import com.denkbares.utils.Log;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class NamespaceAbbreviationDefinition extends AbstractType {

	public NamespaceAbbreviationDefinition() {
		this.addCompileScript(Priority.HIGHEST, new NamespaceSubtreeHandler());
		this.setSectionFinder(new RegexSectionFinder("\\s*\\S+?\\s\\S+"));
		this.addChildType(new AbbreviationDefinition());
		AnonymousType abbreviationSeparator = new AnonymousType("AbbreviationSeparator");
		abbreviationSeparator.setSectionFinder(new RegexSectionFinder("^:\\s"));
		this.addChildType(abbreviationSeparator);
		this.addChildType(new NamespaceDefinition());
	}

	public String getNamespace(Section<NamespaceAbbreviationDefinition> section) {
		Section<NamespaceDefinition> namespace = Sections.child(section,
				NamespaceDefinition.class);
		assert namespace != null;
		return namespace.getText();
	}

	private static class NamespaceSubtreeHandler extends OntologyHandler<NamespaceAbbreviationDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<NamespaceAbbreviationDefinition> section) {
			String namespace = section.get().getNamespace(section);
			List<Message> messages = new LinkedList<>();
			try {
				compiler.getRdf2GoCore().createIRI(namespace);
			}
			catch (IllegalArgumentException e) {
				Message message = new Message(Message.Type.ERROR, "'" + namespace + "' is not a valid URI");
				messages.add(message);
			}
			Section<AbbreviationDefinition> abbreviation = Sections.child(section,
					AbbreviationDefinition.class);
			if (abbreviation == null) {
				Message message = new Message(Message.Type.ERROR, "Your namespace abbreviation is not valid");
				messages.add(message);
				return messages;
			}
			String abbreviationName = abbreviation.get().getTermName(abbreviation);
			Rdf2GoCore.getInstance(compiler).addNamespace(abbreviationName, namespace);
			return messages;
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<NamespaceAbbreviationDefinition> section) {
			Section<AbbreviationDefinition> abbreviationDef = Sections.child(section,
					AbbreviationDefinition.class);
			assert abbreviationDef != null;
			String abbreviation = abbreviationDef.get().getTermName(abbreviationDef);
			try {
				Rdf2GoCore.getInstance(compiler).removeNamespace(abbreviation);
			}
			catch (RepositoryException e) {
				Log.severe("Unable to remove namespace", e);
			}
		}
	}
}
