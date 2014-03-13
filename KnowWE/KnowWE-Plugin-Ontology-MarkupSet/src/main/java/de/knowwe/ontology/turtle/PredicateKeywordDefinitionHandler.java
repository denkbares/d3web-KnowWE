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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;

/**
 * @author Jochen Reutelshofer (denkbares GmbH)
 * @created 06.03.14.
 */
public abstract class PredicateKeywordDefinitionHandler extends OntologyHandler<SimpleReference> {

	protected List<String> matchExpressions;

	public PredicateKeywordDefinitionHandler(String[] matchExpressions) {
		this.matchExpressions = Arrays.asList(matchExpressions); //""
	}

	protected abstract List<Section<Predicate>> getPredicates(Section<SimpleReference> s);

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<SimpleReference> s) {

		List<Section<Predicate>> predicates = getPredicates(s);
		boolean hasInstancePredicate = false;
		Section<Predicate> predicate = null;
		for (Section<Predicate> section : predicates) {
			for (String exp : matchExpressions) {

				if (section.getText().matches(exp)) {
					predicate = section;
					break;
				}
			}
			if (predicate != null) {
				break;
			}
		}

		// we jump out if no matching predicate was found
		if (predicate == null) return Messages.noMessage();

		// If termIdentifier is null, obviously section chose not to define
		// a term, however so we can ignore this case
		Identifier termIdentifier = s.get().getTermIdentifier(s);
		if (termIdentifier != null) {
			Section<PredicateSentence> predSentence = Sections.findAncestorOfType(predicate, PredicateSentence.class);
			Section<Object> object = Sections.findSuccessor(predSentence, Object.class);
			Section<ResourceReference> objectResource = Sections.findSuccessor(object, ResourceReference.class);
			Class<?> termObjectClass = null;

			if (objectResource == null) {
				Section<LazyURIReference> lazyRef = Sections.findSuccessor(object, LazyURIReference.class);
				Identifier id = lazyRef.get().getTermIdentifier(lazyRef);
				if (id != null) {

					TermCompiler termCompiler = Compilers.getCompiler(s, TermCompiler.class);
					Section<?> def = termCompiler.getTerminologyManager().getTermDefiningSection(id);
					/*
					Problem is that the definition might not yet have been compiled
					 */
					if (def != null) {
						termObjectClass = ((Section<? extends Term>) def).get()
								.getTermObjectClass(((Section<? extends Term>) def));
					}
					else {
						/*
						 * we just set Resource in this case (definition might not yet be available)
						 */
						termObjectClass = Resource.class;
					}
				}
				else {
					/*
						 * we just set Resource in this case (definition might not yet be available)
						 */
					termObjectClass = Resource.class;
				}
			}
			else {
				Collection<Class<?>> termClasses = ((TermCompiler) compiler).getTerminologyManager()
						.getTermClasses(objectResource.get().getTermIdentifier(objectResource));
				if (termClasses.size() > 0) {
					termObjectClass = termClasses.iterator().next();
				}
				else {
					termObjectClass = Resource.class;
				}
			}
			compiler.getTerminologyManager().registerTermDefinition(compiler, s, termObjectClass,
					termIdentifier);
		}
		return Messages.noMessage();
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<SimpleReference> s) {
		compiler.getTerminologyManager().unregisterTermDefinition(compiler, s,
				s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
	}

}
