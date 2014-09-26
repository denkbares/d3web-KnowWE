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
import java.util.List;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;

/**
 * @author Jochen Reutelshofer (denkbares GmbH)
 * @created 06.03.2014
 */
public abstract class PredicateKeywordDefinitionHandler extends OntologyCompileScript<SimpleReference> {

	protected List<String> matchExpressions;

	public PredicateKeywordDefinitionHandler(String[] matchExpressions) {
		this.matchExpressions = Arrays.asList(matchExpressions); //""
	}

	protected abstract List<Section<Predicate>> getPredicates(Section<SimpleReference> s);

	@Override
	public void compile(OntologyCompiler compiler, Section<SimpleReference> s) {

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
		if (predicate == null) return;

		Identifier termIdentifier;
		try {
			termIdentifier = s.get().getTermIdentifier(s);
		}
		catch (IllegalStateException e) {
			// in case of LazyURIReferences we just abort, they can never be definitions
			return;
		}

		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Section<?> section = terminologyManager.getTermDefiningSection(termIdentifier);
		Class<?> termClass = Resource.class;
		if (section != null && section.get() instanceof SimpleDefinition) {
			Section<SimpleDefinition> simpleDefinitionSection = Sections.cast(section, SimpleDefinition.class);
			termClass = simpleDefinitionSection.get().getTermObjectClass(simpleDefinitionSection);
		}
		terminologyManager.registerTermDefinition(compiler, s, termClass, termIdentifier);
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<SimpleReference> s) {
		compiler.getTerminologyManager().unregisterTermDefinition(compiler, s,
				s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
	}

}
