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

import java.util.List;

import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.ontology.turtle.compile.StatementProvider;
import de.knowwe.ontology.turtle.compile.StatementProviderResult;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class Object extends AbstractType implements NodeProvider<Object>, StatementProvider<Object> {

	/*
	 * With strict compilation mode on, triples are not inserted into the
	 * repository when corresponding terms have errors, i.e., do not have a
	 * valid definition. With strict compilation mode off, triples are always
	 * inserted into the triple store, not caring about type definitions.
	 */
	static boolean STRICT_COMPILATION = false;

	public Object() {
		this.setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				return SectionFinderResult.resultList(Strings.splitUnquoted(text, ",", false,
						TurtleMarkup.TURTLE_QUOTES));
			}
		});
		this.addChildType(new BlankNode());
		this.addChildType(new BlankNodeID());
		this.addChildType(TurtleCollection.getInstance());
		this.addChildType(new TurtleLiteralType());
		this.addChildType(new TurtleLongURI());
		this.addChildType(createObjectURIWithDefinition());
		this.addChildType(new LazyURIReference());
	}
	
	@SuppressWarnings("unchecked")
	private Type createObjectURIWithDefinition() {
		TurtleURI turtleURI = new TurtleURI();
		SimpleReference reference = Types.findSuccessorType(turtleURI, ResourceReference.class);
		reference.addCompileScript(Priority.HIGH, new ObjectPredicateKeywordDefinitionHandler(new String[]{"[\\w]*?:instance"}));
		reference.removeCompileScript(OntologyCompiler.class,
				SimpleReferenceRegistrationScript.class);
		return turtleURI;
	}

	class ObjectPredicateKeywordDefinitionHandler extends PredicateKeywordDefinitionHandler {

		public ObjectPredicateKeywordDefinitionHandler(String[] matchExpressions) {
			super(matchExpressions);
		}

		@Override
		protected List<Section<Predicate>> getPredicates(Section<SimpleReference> s) {
			// find the one predicate relevant for this turtle object
			Section<PredicateObjectSentenceList> predSentence = Sections.ancestor(s,
					PredicateObjectSentenceList.class);

			return Sections.successors(predSentence, Predicate.class);
		}
	}

	@Override
	public StatementProviderResult getStatements(Section<Object> section, Rdf2GoCore core) {

		StatementProviderResult result = new StatementProviderResult();
		boolean termError = false;
		/*
		 * Handle OBJECT
		 */
		Node object = section.get().getNode(section, core);
		Section<TurtleURI> turtleURITermObject = Sections.findChildOfType(section, TurtleURI.class);
		if (turtleURITermObject != null && STRICT_COMPILATION) {
			boolean isDefined = checkTurtleURIDefinition(turtleURITermObject);
			if (!isDefined) {
				// error message is already rendered by term reference renderer
				// we do not insert statement in this case
				object = null;
				termError = true;
			}
		}
		if (object == null && !termError) {
			result.addMessage(Messages.error("'" + section.getText()
					+ "' is not a valid object."));
		}

		/*
		 * Handle PREDICATE
		 */
		Section<PredicateSentence> predSentenceSection = Sections.findAncestorOfType(section,
				PredicateSentence.class);
		Section<Predicate> predicateSection = Sections.findChildOfType(predSentenceSection,
				Predicate.class);

		if (predicateSection == null) {
			result.addMessage(Messages.error("No predicate section found: " + section.toString()));
			return result;
		}

		URI predicate = predicateSection.get().getURI(predicateSection, core);

		// check term definition
		Section<TurtleURI> turtleURITerm = Sections.findSuccessor(predicateSection, TurtleURI.class);
		if (turtleURITerm != null && STRICT_COMPILATION) {
			boolean isDefined = checkTurtleURIDefinition(turtleURITerm);
			if (!isDefined) {
				// error message is already rendered by term reference renderer
				// we do not insert statment in this case
				predicate = null;
				termError = true;
			}
		}
		if (predicate == null && !termError) {
			result.addMessage(Messages.error("'" + predicateSection.getText()
					+ "' is not a valid predicate."));
		}

		/*
		 * Handle SUBJECT
		 */
		Resource subject;
		// the subject can either be a normal turtle sentence subject
		// OR a blank node
		Section<BlankNode> blankNodeSection = Sections.findAncestorOfType(predSentenceSection,
				BlankNode.class);
		if (blankNodeSection != null) {
			subject = blankNodeSection.get().getResource(blankNodeSection, core);
			if (subject == null) {
				result.addMessage(Messages.error("'" + blankNodeSection.getText()
						+ "' is not a valid subject."));
			}
		}
		else {
			Section<TurtleSentence> sentence = Sections.findAncestorOfType(predSentenceSection,
					TurtleSentence.class);
			Section<Subject> subjectSection = Sections.findSuccessor(sentence,
					Subject.class);
			subject = subjectSection.get().getResource(subjectSection, core);

			// check term definition
			Section<TurtleURI> turtleURITermSubject = Sections.findChildOfType(subjectSection,
					TurtleURI.class);
			if (turtleURITermSubject != null && STRICT_COMPILATION) {
				boolean isDefined = checkTurtleURIDefinition(turtleURITermSubject);
				if (!isDefined) {
					// error message is already rendered by term reference
					// renderer
					// we do not insert statement in this case
					subject = null;
					termError = true;
				}
			}

			if (subject == null && !termError) {
				result.addMessage(Messages.error("'" + subjectSection.getText()
						+ "' is not a valid subject."));
			}
		}

		// create statement if all nodes are present
		if (object != null && predicate != null && subject != null) {
			result.addStatement(core.createStatement(subject, predicate, object));
		}
		return result;
	}

	private boolean checkTurtleURIDefinition(Section<TurtleURI> turtleURITerm) {
		TermCompiler compiler = Compilers.getCompiler(turtleURITerm, TermCompiler.class);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Section<TermReference> term = Sections.findSuccessor(turtleURITerm, TermReference.class);
		return terminologyManager.isDefinedTerm(term.get().getTermIdentifier(term));
	}

	@Override
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Node getNode(Section<Object> section, Rdf2GoCore core) {
		// there should be exactly one NodeProvider child (while potentially
		// many successors)
		List<Section<NodeProvider>> nodeProviderSections = Sections.findChildrenOfType(section,
				NodeProvider.class);
		if (nodeProviderSections != null) {
			if (nodeProviderSections.size() == 1) {

				Section<NodeProvider> nodeProvider = nodeProviderSections.get(0);
				return nodeProvider.get().getNode(
						nodeProvider, core);
			}
			// if there are more NodeProvider we return null to force an error
		}
		return null;
	}

}
