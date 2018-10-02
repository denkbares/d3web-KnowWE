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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.StatementProvider;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public class Object extends AbstractType implements NodeProvider<Object>, StatementProvider<Object> {

	/*
	 * With strict compilation mode on, triples are not inserted into the
	 * repository when corresponding terms have errors, i.e., do not have a
	 * valid definition. With strict compilation mode off, triples are always
	 * inserted into the triple store, not caring about type definitions.
	 */
	public static boolean STRICT_COMPILATION = false;

	public Object() {
		this.setSectionFinder(
				(text, father, type) ->
						SectionFinderResult.resultList(Strings.splitUnquoted(text, ",", false, TurtleMarkup.TURTLE_QUOTES)));
		this.addChildType(new BlankNode());
		this.addChildType(new BlankNodeID());
		this.addChildType(TurtleCollection.getInstance());
		this.addChildType(new TurtleLiteralType());
		this.addChildType(new BooleanLiteral());
		this.addChildType(new NumberLiteral());
		this.addChildType(new EncodedTurtleURI());
		this.addChildType(createObjectURIWithDefinition());
		this.addChildType(new LazyURIReference());
	}

	@SuppressWarnings("unchecked")
	private Type createObjectURIWithDefinition() {
		TurtleURI turtleURI = new TurtleURI();
		SimpleReference reference = Types.successor(turtleURI, ResourceReference.class);
		reference.addCompileScript(Priority.HIGHEST, new ObjectPredicateKeywordDefinitionHandler(new String[] { "[\\w]*?:instance" }));
		return turtleURI;
	}

	static class ObjectPredicateKeywordDefinitionHandler extends PredicateKeywordDefinitionHandler {

		public ObjectPredicateKeywordDefinitionHandler(String[] matchExpressions) {
			super(matchExpressions);
		}

		@Override
		protected List<Section<Predicate>> getPredicates(Section<SimpleReference> s) {
			// find the one predicate relevant for this turtle object
			Section<PredicateSentence> predSentence = Sections.ancestor(s,
					PredicateSentence.class);
			if (predSentence != null) {
				return Sections.children(predSentence, Predicate.class);
			}

			return Collections.emptyList();
		}

		@Override
		public void compile(OntologyCompiler compiler, Section<SimpleReference> s) {

			List<Section<Predicate>> predicates = getPredicates(s);
			boolean hasInstancePredicate = false;
			for (Section<Predicate> section : predicates) {
				for (String exp : matchExpressions) {

					if (section.getText().matches(exp)) {
						hasInstancePredicate = true;
					}
				}
			}

			// we jump out if no matching predicate was found
			if (!hasInstancePredicate) return;

			// If termIdentifier is null, obviously section chose not to define
			// a term, however so we can ignore this case
			Identifier termIdentifier = s.get().getTermIdentifier(s);
			if (termIdentifier != null) {
				compiler.getTerminologyManager()
						.registerTermDefinition(compiler, s, de.knowwe.ontology.kdom.resource.Resource.class,
								termIdentifier);
			}
		}
	}

	@NotNull
	@Override
	public StatementProviderResult getStatements(Section<? extends Object> section, Rdf2GoCompiler core) {

		StatementProviderResult result = new StatementProviderResult(core);
		boolean termError = false;
		/*
		 * Handle OBJECT
		 */
		Value object = section.get().getNode(section, core);
		Section<TurtleURI> turtleURITermObject = Sections.child(section, TurtleURI.class);
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
			result.error("'" + section.getText().replaceAll("\\s+", " ") + "' is not a valid object.");
		}

		/*
		 * Handle PREDICATE
		 */

		Section<Predicate> predicateSection = getPredicateSection(section);

		if (predicateSection == null) {
			return result.error("No predicate section found: " + section);
		}

		org.openrdf.model.URI predicate = predicateSection.get().getURI(predicateSection, core);

		// check term definition
		Section<TurtleURI> turtleURITerm = Sections.successor(predicateSection, TurtleURI.class);
		if (turtleURITerm != null && STRICT_COMPILATION) {
			boolean isDefined = checkTurtleURIDefinition(turtleURITerm);
			if (!isDefined) {
				// error message is already rendered by term reference renderer
				// we do not insert statement in this case
				predicate = null;
				termError = true;
			}
		}

		if (predicate == null && !termError) {
			result.error("'" + predicateSection.getText() + "' is not a valid predicate.");
		}

		/*
		 * Handle SUBJECT
		 */
		org.openrdf.model.Resource subject = getSubject(core, result, termError, section);

		// create statement if all nodes are present
		if (object != null && predicate != null && subject != null) {
			result.addStatement(subject, predicate, object);
		}
		return result;
	}

	public Section<Predicate> getPredicateSection(Section<? extends Object> section) {
		Section<PredicateSentence> predSentenceSection = Sections.ancestor(section, PredicateSentence.class);
		assert predSentenceSection != null;
		return Sections.child(predSentenceSection, Predicate.class);
	}

	public @Nullable Resource getSubject(Rdf2GoCompiler core, StatementProviderResult result, boolean termError, Section<? extends Object> section) {
		Section<PredicateSentence> predSentenceSection = Sections.ancestor(section, PredicateSentence.class);
		assert predSentenceSection != null;

		Resource subject;
		// the subject can either be a normal turtle sentence subject
		// OR a blank node
		Section<BlankNode> blankNodeSection = Sections.ancestor(predSentenceSection, BlankNode.class);
		if (blankNodeSection != null) {
			subject = blankNodeSection.get().getResource(blankNodeSection, core);
			if (subject == null) {
				result.addMessage(Messages.error("'" + blankNodeSection.getText() + "' is not a valid subject."));
			}
		}
		else {
			Section<TurtleSentence> sentence = Sections.ancestor(predSentenceSection,
					TurtleSentence.class);
			Section<Subject> subjectSection = findSubjectSection(sentence);
			subject = subjectSection.get().getResource(subjectSection, core);

			// check term definition
			Section<TurtleURI> turtleURITermSubject = Sections.child(subjectSection,
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
		return subject;
	}

	public Section<Subject> findSubjectSection(Section<?> sentence) {
		return Sections.successor(sentence,
				Subject.class);
	}

	protected boolean checkTurtleURIDefinition(Section<TurtleURI> turtleURITerm) {
		TermCompiler compiler = Compilers.getCompiler(turtleURITerm, TermCompiler.class);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Section<TermReference> term = Sections.successor(turtleURITerm, TermReference.class);
		return terminologyManager.isDefinedTerm(term.get().getTermIdentifier(term));
	}

	@Override
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Value getNode(Section<? extends Object> section, Rdf2GoCompiler core) {
		// there should be exactly one NodeProvider child (while potentially
		// many successors)
		List<Section<NodeProvider>> nodeProviderSections = Sections.children(section,
				NodeProvider.class);
		if (nodeProviderSections != null) {
			if (nodeProviderSections.size() == 1) {

				Section<NodeProvider> nodeProvider = nodeProviderSections.get(0);
				return nodeProvider.get().getNode(nodeProvider, core);
			}
			// if there are more NodeProvider we return null to force an error
		}
		return null;
	}
}
