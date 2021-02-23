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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.StatementProvider;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;

public class Object extends AbstractType implements NodeProvider<Object>, StatementProvider<Object> {

	public Object() {
		this.setSectionFinder((text, father, type) -> SectionFinderResult.resultList(
				Strings.splitUnquoted(text, ",", false, TurtleMarkup.TURTLE_QUOTES)));
		this.addChildType(new BlankNode());
		this.addChildType(new BlankNodeID());
		this.addChildType(new ShowOtherExistingValuesWildCard());
		this.addChildType(TurtleCollection.getInstance());
		this.addChildType(new TurtleLiteralType());
		this.addChildType(new BooleanLiteral());
		this.addChildType(new NumberLiteral());
		this.addChildType(new EncodedTurtleURI());
		this.addChildType(createObjectURIWithDefinition());
		this.addChildType(new LazyURIReference());
	}

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
		protected List<Section<Predicate>> getPredicates(Section<SimpleReference> reference) {
			// find the one predicate relevant for this turtle object
			Section<PredicateSentence> sentence = Sections.ancestor(reference, PredicateSentence.class);
			return (sentence != null) ? Sections.children(sentence, Predicate.class) : Collections.emptyList();
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
			Identifier termIdentifier = s.get().getTermIdentifier(compiler, s);
			if (termIdentifier != null) {
				compiler.getTerminologyManager().registerTermDefinition(
						compiler, s, de.knowwe.ontology.kdom.resource.Resource.class, termIdentifier);
			}
		}
	}

	@NotNull
	@Override
	public StatementProviderResult getStatements(OntologyCompiler compiler, Section<? extends Object> section) throws CompilerMessage {

		StatementProviderResult result = new StatementProviderResult(compiler);
		/*
		 * Handle OBJECT
		 */
		Value object = section.get().getNode(compiler, section);
		if (object == null) {
			result.error("'" + section.getText() + "' is not a valid value.");
		}

		/*
		 * By convention for RDF.NIL as object, we do not create something
		 */
		if (object != null && object.stringValue().equals(RDF.NIL.toString())) {
			return result;
		}

		/*
		 * Handle PREDICATE
		 */
		Section<Predicate> predicateSection = getPredicateSection(section);
		if (predicateSection == null) {
			return result.error("No predicate section found: " + section);
		}

		IRI predicate = predicateSection.get().getIRI(compiler, predicateSection);

		// check term definition
		if (predicate == null) {
			result.error("'" + predicateSection.getText() + "' is not a valid predicate.");
		}

		/*
		 * Handle SUBJECT
		 */
		org.eclipse.rdf4j.model.Resource subject = findSubject(compiler, result, section);

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

	@Nullable
	protected Resource findSubject(OntologyCompiler core, StatementProviderResult result, Section<? extends Object> section) throws CompilerMessage {
		Section<PredicateSentence> predSentenceSection = Sections.ancestor(section, PredicateSentence.class);
		assert predSentenceSection != null;

		Resource subject;
		// the subject can either be a normal turtle sentence subject
		// OR a blank node
		Section<BlankNode> blankNodeSection = Sections.ancestor(predSentenceSection, BlankNode.class);
		if (blankNodeSection != null) {
			subject = blankNodeSection.get().getResource(core, blankNodeSection);
			if (subject == null) {
				result.addMessage(Messages.error("'" + blankNodeSection.getText() + "' is not a valid subject."));
			}
		}
		else {
			subject = findSubject(core, predSentenceSection);
		}
		return subject;
	}

	public Resource findSubject(OntologyCompiler compiler, Section<?> section) {
		if (!(section.get() instanceof TurtleSentence)) {
			section = Sections.ancestor(section, TurtleSentence.class);
		}
		Section<Subject> subject = Sections.successor(section, Subject.class);
		if (subject == null) return null;
		return subject.get().getResource(compiler, subject);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Value getNode(OntologyCompiler core, Section<? extends Object> section) {
		// there should be exactly one NodeProvider child (while potentially
		// many successors)
		List<Section<NodeProvider>> nodeProviderSections = Sections.children(section, NodeProvider.class);

		// if there are more NodeProvider we return null to force an error
		if (nodeProviderSections.size() == 1) {
			Section<NodeProvider> nodeProvider = nodeProviderSections.get(0);
			return nodeProvider.get().getNode(core, nodeProvider);
		}
		return null;
	}
}
