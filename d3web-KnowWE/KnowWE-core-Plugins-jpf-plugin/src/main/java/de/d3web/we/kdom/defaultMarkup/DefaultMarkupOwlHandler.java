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
package de.d3web.we.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.subtreeHandler.OwlSubtreeHandler;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

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
public class DefaultMarkupOwlHandler extends OwlSubtreeHandler {

	private final DefaultMarkupType defaultMarkupType;
	private URI conceptURI = null;

	DefaultMarkupOwlHandler(DefaultMarkupType defaultMarkupType) {
		this.defaultMarkupType = defaultMarkupType;
	}

	private String getMarkupName() {
		return this.defaultMarkupType.getMarkup().getName();
	}

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section section) {

		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();

		IntermediateOwlObject io = new IntermediateOwlObject();
		OwlHelper helper = SemanticCoreDelegator.getInstance().getUpper().getHelper();

		try {
			// Access (or lazy build) parent concept
			URI superConceptURI = helper.createURI("DefaultMarkup");

			// create a new class for this markup
			this.conceptURI = helper.createlocalURI(getMarkupName());
			io.addStatement(helper.createStatement(
					this.conceptURI,
					RDFS.SUBCLASSOF,
					superConceptURI));

			// create a new instance for the markup section
			// TODO: create node with section-id instead of blank node
			BNode bnode = SemanticCoreDelegator.getInstance().getUpper().getVf().createBNode();
			io.addStatement(helper.createStatement(bnode, RDF.TYPE, this.conceptURI));

			// add content block as literal "hasContent"
			addStringLiteral(bnode,
					"hasContent", DefaultMarkupType.getContent(section),
					io, helper);

			// add annotation blocks as literal "hasXYZ"
			for (Annotation annotation : this.defaultMarkupType.getMarkup().getAnnotations()) {
				String name = annotation.getName();
				addStringLiteral(bnode,
						"has" + name, DefaultMarkupType.getAnnotation(section, name),
						io, helper);
			}

			// add hasArticle
			addStringLiteral(bnode,
					"hasArticle", section.getTitle(),
					io, helper);

			// add hasLink
			addStringLiteral(bnode,
					"hasLink", KnowWEUtils.getLink(section),
					io, helper);
		}
		catch (RepositoryException e) {
			Logger.getLogger("DefaultMarkup").log(Level.SEVERE,
					"cannot create concept for default markup '" +
							getMarkupName() + "'", e);
			msgs.add(new SimpleMessageError(e.getMessage()));
			return msgs;
		}

		SemanticCoreDelegator.getInstance().addStatements(io, section);
		return msgs;

	}

	private static void addStringLiteral(BNode bnode, String name, String text, IntermediateOwlObject io, OwlHelper helper) throws RepositoryException {
		if (text == null) text = "";
		Literal content = helper.createLiteral(text);
		io.addStatement(helper.createStatement(
				bnode, helper.createlocalURI(name), content));
	}

}