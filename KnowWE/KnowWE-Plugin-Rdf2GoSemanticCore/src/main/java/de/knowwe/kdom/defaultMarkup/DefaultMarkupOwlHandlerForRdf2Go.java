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
/**
 * 
 */
package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.RDFS;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.knowwe.rdf2go.RDF2GoSubtreeHandler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * This class implements a default behavior for generating owl concept and
 * instances for a default markup and its created sections.
 * <p>
 * All items are created in the local name-space nls. Please consider that all
 * names in owl are case-sensitive. The following items are created:
 * <ul>
 * <li>A concept for the markup. It has the same name as the markup has. The
 * concept is a subclass of the concept "ns:DefaultMarkup".
 * <li>A instance of the created section for the each section of the markup.
 * <li>An attribute "lns:hasContent" for the content block of the section.
 * <li>An attribute "lns:hasArticle" for the wiki article the section is
 * contained in.
 * <li>An attribute "lns:hasLink" for the the wiki link to the section.
 * <li>An attribute "lns:has&lt;annotation-name&gt;" for each annotation used in
 * the section.
 * </ul>
 * 
 * @author Volker Belli (denkbares GmbH)
 */
public class DefaultMarkupOwlHandlerForRdf2Go extends RDF2GoSubtreeHandler<DefaultMarkupType> {

	private URI conceptURI = null;

	@Override
	public Collection<Message> create(Article article, Section<DefaultMarkupType> section) {
		DefaultMarkup defaultMarkupType = section.get().getMarkup();
		String markupName = defaultMarkupType.getName();
		List<Statement> io = new ArrayList<Statement>();
		List<Message> msgs = new ArrayList<Message>();

		// try {
		// Access (or lazy build) parent concept
		URI superConceptURI = Rdf2GoCore.getInstance().createBasensURI("DefaultMarkup");

		// create a new class for this markup
		this.conceptURI = Rdf2GoCore.getInstance().createlocalURI(markupName);
		io.add(Rdf2GoCore.getInstance().createStatement(
				this.conceptURI,
				RDFS.subClassOf,
				superConceptURI));

		// create a new instance for the markup section
		// TODO: create node with section-id instead of blank node
		BlankNode bnode = Rdf2GoCore.getInstance().createBlankNode();
		io.add(Rdf2GoCore.getInstance().createStatement(bnode, RDF.type, this.conceptURI));

		// add content block as literal "hasContent"
		addStringLiteral(bnode,
				"hasContent", DefaultMarkupType.getContent(section),
				io);

		// add annotation blocks as literal "hasXYZ"
		for (Annotation annotation : defaultMarkupType.getAnnotations()) {
			String name = annotation.getName();
			addStringLiteral(bnode,
					"has" + name, DefaultMarkupType.getAnnotation(section, name),
					io);
		}

		// add hasArticle
		addStringLiteral(bnode,
				"hasArticle", section.getTitle(),
				io);

		// add hasLink
		addStringLiteral(bnode,
				"hasLink", KnowWEUtils.getWikiLink(section),
				io);
		// }
		// catch (RepositoryException e) {
		// Logger.getLogger("DefaultMarkup").log(Level.SEVERE,
		// "cannot create concept for default markup '" +
		// markupName + "'", e);
		// msgs.add(new SimpleMessageError(e.getMessage()));
		// return msgs;
		// }

		Rdf2GoCore.getInstance().addStatements(section, Rdf2GoUtils.toArray(io));
		return msgs;

	}

	private static void addStringLiteral(BlankNode bnode, String name, String text, List<Statement> io) {
		if (text == null) text = "";
		Literal content = Rdf2GoCore.getInstance().createLiteral(text);
		io.add(Rdf2GoCore.getInstance().createStatement(
				bnode, Rdf2GoCore.getInstance().createlocalURI(name), content));
	}

}