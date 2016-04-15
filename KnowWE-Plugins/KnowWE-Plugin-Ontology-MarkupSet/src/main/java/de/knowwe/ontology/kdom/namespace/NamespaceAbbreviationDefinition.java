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

import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import de.d3web.strings.Identifier;
import de.d3web.utils.Log;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class NamespaceAbbreviationDefinition extends SimpleDefinition {

	public NamespaceAbbreviationDefinition() {
		super(OntologyCompiler.class, NamespaceAbbreviationDefinition.class);
		this.addCompileScript(Priority.HIGHEST, new NamespaceSubtreeHandler());
		this.setSectionFinder(new RegexSectionFinder("\\s*\\S+?\\s\\S+"));
		this.addChildType(new AbbreviationDefinition());
		this.addChildType(new NamespaceDefinition());
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		Section<AbbreviationDefinition> abbreviation = Sections.child(section,
				AbbreviationDefinition.class);
		if (abbreviation == null) {
			return section.getText();
		}
		String abbreviationName = abbreviation.get().getTermName(abbreviation);
		return abbreviationName + " - " + getNamespace(section);
	}

	@Override
	protected boolean verifyDefinition(de.knowwe.core.compile.Compiler compiler, Section<SimpleDefinition> section) {
		Section<AbbreviationDefinition> abbreviation = Sections.child(section,
				AbbreviationDefinition.class);
		return abbreviation != null;
	}

	public String getNamespace(Section<? extends Term> section) {
		Section<NamespaceDefinition> namespace = Sections.child(section,
				NamespaceDefinition.class);
		return namespace.getText();
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we dont want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}

	private static class NamespaceSubtreeHandler extends OntologyHandler<NamespaceAbbreviationDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<NamespaceAbbreviationDefinition> section) {
			String namespace = section.get().getNamespace(section);
			List<Message> messages = new LinkedList<>();
			try {
				new URIImpl(namespace, true);
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
			Section<AbbreviationDefinition> abbreviation1 = Sections.child(section,
					AbbreviationDefinition.class);
			String abbreviationName = abbreviation1.get().getTermName(abbreviation1);
			String abbreviation = abbreviationName;
			try {
				Rdf2GoCore.getInstance(compiler).removeNamespace(abbreviation);
			}
			catch (RepositoryException e) {
				Log.severe("Unable to remove namespace", e);
			}
		}

	}
}
