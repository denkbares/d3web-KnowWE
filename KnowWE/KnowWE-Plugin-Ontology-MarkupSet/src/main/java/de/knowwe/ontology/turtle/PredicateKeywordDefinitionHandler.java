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
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;

/**
 * @author Jochen Reutelshofer (denkbares GmbH)
 * @created 06.03.14.
 */
public abstract class PredicateKeywordDefinitionHandler extends OntologyHandler<SimpleReference> {

	private List<String> matchExpressions;

	public PredicateKeywordDefinitionHandler(String[] matchExpressions) {
		this.matchExpressions = Arrays.asList(matchExpressions); //""
	}

	protected abstract List<Section<Predicate>> getPredicates(Section<SimpleReference> s);

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<SimpleReference> s) {

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
		if (!hasInstancePredicate) return validateReference(compiler, s);

		// If termIdentifier is null, obviously section chose not to define
		// a term, however so we can ignore this case
		Identifier termIdentifier = s.get().getTermIdentifier(s);
		if (termIdentifier != null) {
			compiler.getTerminologyManager().registerTermDefinition(compiler, s,
					s.get().getTermObjectClass(s),
					termIdentifier);
		}

		return Messages.noMessage();
	}

	public Collection<Message> validateReference(TermCompiler compiler, Section<? extends Term> section) {
		TerminologyManager tHandler = compiler.getTerminologyManager();
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		if (!tHandler.isDefinedTerm(termIdentifier)) {
			return Messages.asList(Messages.noSuchObjectError(
					section.get().getTermObjectClass(section).getSimpleName(),
					section.get().getTermName(section)));
		}
		return Messages.noMessage();
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<SimpleReference> s) {
		compiler.getTerminologyManager().unregisterTermDefinition(compiler, s,
				s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
	}

}
