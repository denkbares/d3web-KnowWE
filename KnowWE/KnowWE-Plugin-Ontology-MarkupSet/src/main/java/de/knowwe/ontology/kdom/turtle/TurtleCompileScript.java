/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.ontology.kdom.turtle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.ontology.kdom.relation.LiteralType;
import de.knowwe.rdf2go.BlankNodeImpl;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class TurtleCompileScript extends SubtreeHandler<TurtleMarkup> {

	@Override
	public Collection<Message> create(Article article, Section<TurtleMarkup> section) {
		List<Section<TurtleObjectSection>> found = new ArrayList<Section<TurtleObjectSection>>();

		Sections.findSuccessorsOfType(section, TurtleObjectSection.class, found);

		List<Statement> triples = new ArrayList<Statement>();

		for (Section<TurtleObjectSection> objectSec : found) {

			createTriplesForObject(triples, objectSec, article);

		}
		Rdf2GoCore.getInstance(article).addStatements(section, Rdf2GoUtils.toArray(triples));
		return null;
	}

	private void createTriplesForObject(List<Statement> triples, Section<TurtleObjectSection> objectSec, Article article) {

		Rdf2GoCore core = Rdf2GoCore.getInstance(article);

		Section<LiteralType> literalSec = Sections.findSuccessor(
				objectSec, LiteralType.class);

		Node objURI = null;

		Section<TurtleObjectBlankNode> bn = Sections.findChildOfType(objectSec,
				TurtleObjectBlankNode.class);
		if (bn != null) {
			Section<TurtleMarkupN3Content> turtleInner = Sections.findSuccessor(bn,
					TurtleMarkupN3Content.class);
			objURI = new BlankNodeImpl(turtleInner);

		}
		else {
			Section<TurtleObjectTerm> termSec = Sections.findSuccessor(objectSec,
					TurtleObjectTerm.class);
			if (termSec != null) {
				objURI = termSec.get().getResourceURI(core, termSec);
			}
			if (literalSec != null) {
				objURI = literalSec.get().getLiteral(core, literalSec);
			}
		}

		Section<TurtlePredicate> predSec = Sections.findSuccessor(
				objectSec.getFather(), TurtlePredicate.class);

		URI predURI = predSec.get().getResourceURI(core, predSec);

		Section<TurtleSubject> subjectSec = Sections.findSuccessor(
				objectSec.getFather().getFather(), TurtleSubject.class);

		Resource subjectURI = subjectSec.get().getResourceURI(core, subjectSec);
		if (subjectURI == null) {
			subjectURI = new BlankNodeImpl(Sections.findSuccessor(
					objectSec.getFather().getFather().getFather(),
					TurtleMarkupN3Content.class));
		}

		if (objURI != null && predURI != null && subjectURI != null) {
			Statement triple = Rdf2GoCore.getInstance().createStatement(
					subjectURI, predURI, objURI);

			triples.add(triple);
		}
	}

}
