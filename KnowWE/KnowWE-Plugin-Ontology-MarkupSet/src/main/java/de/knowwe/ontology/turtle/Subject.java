/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.turtle;

import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.CompositeRenderer;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.ontology.turtle.compile.ResourceProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

public class Subject extends AbstractType implements ResourceProvider<Subject> {

	public Subject() {
		this.addChildType(new BlankNode());
		this.addChildType(new BlankNodeID());
		this.addChildType(new TurtleLongURI());
		this.addChildType(createSubjectURIWithDefinition());
		this.addChildType(new LazyURIReference());
		setSectionFinder(new FirstWordFinder());

		this.setRenderer(new CompositeRenderer(DelegateRenderer.getInstance(),
				new DropTargetRenderer()));

	}

	private Type createSubjectURIWithDefinition() {
		TurtleURI turtleURI = new TurtleURI();
		SimpleReference reference = Types.findSuccessorType(turtleURI, ResourceReference.class);
		reference.addSubtreeHandler(Priority.HIGHER, new RDFTypeDefinitionHandler(
				TermRegistrationScope.LOCAL));
		return turtleURI;
	}

	class RDFTypeDefinitionHandler extends SubtreeHandler<SimpleReference> {

		private final TermRegistrationScope scope;

		public RDFTypeDefinitionHandler(TermRegistrationScope scope) {
			this.scope = scope;
		}

		@Override
		public Collection<Message> create(Article article, Section<SimpleReference> s) {

			Section<TurtleSentence> sentence = Sections.findAncestorOfType(s, TurtleSentence.class);
			List<Section<Predicate>> predicates = Sections.findSuccessorsOfType(sentence,
					Predicate.class);
			boolean hasTypePredicate = false;
			for (Section<Predicate> section : predicates) {
				if (section.getText().matches("[\\w]*?:type")) {
					hasTypePredicate = true;
				}
			}

			// we jump out if no type predicate was found
			if (!hasTypePredicate) return Messages.noMessage();

			Identifier termIdentifier = s.get().getTermIdentifier(s);
			if (termIdentifier != null) {
				getTerminologyHandler(article).registerTermDefinition(s,
						s.get().getTermObjectClass(s),
						termIdentifier);
			}
			else {
				/*
				 * termIdentifier is null, obviously section chose not to define
				 * a term, however so we can ignore this case
				 */
			}

			return Messages.noMessage();
		}

		private TerminologyManager getTerminologyHandler(Article article) {
			if (scope == TermRegistrationScope.GLOBAL) {
				return KnowWEUtils.getGlobalTerminologyManager(article.getWeb());
			}
			else {
				return KnowWEUtils.getTerminologyManager(article);
			}
		}

		@Override
		public void destroy(Article article, Section<SimpleReference> s) {
			getTerminologyHandler(article).unregisterTermDefinition(s,
					s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
		}

	}

	@Override
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Node getNode(Section<Subject> section, Rdf2GoCore core) {
		// there should be exactly one NodeProvider child (while potentially
		// many successors)
		Section<NodeProvider> nodeProviderChild = Sections.findChildOfType(section,
				NodeProvider.class);
		if (nodeProviderChild != null) {
			return nodeProviderChild.get().getNode(nodeProviderChild, core);
		}
		return null;
	}

	@Override
	public Resource getResource(Section<Subject> section, Rdf2GoCore core) {
		return (Resource) getNode(section, core);

	}

}