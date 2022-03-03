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
package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * This class offers a method to create a (d3web-) Condition from a CompositeCondition section.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 26.07.2010
 * @see CompositeCondition
 */
public class KDOMConditionFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(KDOMConditionFactory.class);

	@SuppressWarnings("unchecked")
	public static Condition createCondition(D3webCompiler compiler, Section<? extends CompositeCondition> section) {
		if (section == null) return null;

		// if braced - delegate to next composite
		if (section.get().isBraced(section)) {
			Section<? extends NonTerminalCondition> braced = section.get().getBraced(section);
			return createCondition(compiler, Sections.successor(braced, CompositeCondition.class));
		}

		// create conjuncts
		if (section.get().isConjunction(section)) {
			List<Section<? extends NonTerminalCondition>> conjuncts = section.get().getConjuncts(section);

			List<Condition> conds = new ArrayList<>();
			for (Section<? extends NonTerminalCondition> conjunct : conjuncts) {
				Section<? extends CompositeCondition> subCondSection = Sections.child(
						conjunct, CompositeCondition.class);
				Condition subCond = createCondition(compiler, subCondSection);
				if (subCond != null) {
					conds.add(subCond);
				}
			}
			if (conds.isEmpty()) {
				return null;
			}
			else {
				return new CondAnd(conds);
			}
		}

		// create disjunctions
		if (section.get().isDisjunction(section)) {
			List<Section<? extends NonTerminalCondition>> disjuncts = section.get().getDisjuncts(
					section);

			List<Condition> conds = new ArrayList<>();
			for (Section<? extends NonTerminalCondition> disjunct : disjuncts) {
				Section<? extends CompositeCondition> subCondSection = Sections.child(
						disjunct, CompositeCondition.class);
				Condition subCond = createCondition(compiler, Sections.cast(subCondSection, CompositeCondition.class));
				if (subCond != null) {
					conds.add(subCond);
				}
			}

			if (conds.isEmpty()) {
				return null;
			}
			else {
				return new CondOr(conds);
			}
		}

		// create negations
		if (section.get().isNegation(section)) {
			// can only be one
			Section<? extends NonTerminalCondition> neg = section.get().getNegation(section);
			Section<? extends CompositeCondition> subCondSection = Sections.child(neg,
					CompositeCondition.class);
			Condition subCond = createCondition(compiler, Sections.cast(subCondSection, CompositeCondition.class));
			if (subCond == null) return null;
			return new CondNot(subCond);
		}

		// end of recursion => (let) create terminal conditions
		if (section.get().isTerminal(section)) {
			Section<? extends TerminalCondition> terminal = section.get().getTerminal(section);

			Section<D3webCondition> termChild = Sections.child(terminal, D3webCondition.class);

			if (termChild == null) {
				LOGGER.warn("Could not create Condition for: " + terminal.getParent());
				return null;
			}

			return termChild.get().getCondition(compiler, termChild);
		}

		return null;
	}
}
