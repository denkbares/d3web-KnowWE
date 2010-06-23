/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.rulesNew;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.d3web.we.kdom.condition.TerminalCondition;
import de.d3web.we.kdom.rulesNew.terminalCondition.D3webTerminalCondition;

public class KDOMConditionFactory {

	public static Condition createCondition(Section<CompositeCondition> c) {

		// if braced - delegate to next composite
		if (c.get().isBraced(c)) {
			Section<? extends NonTerminalCondition> braced = c.get().getBraced(c);
			return createCondition((Section<CompositeCondition>) braced.findSuccessor(CompositeCondition.class));
		}

		// create conjuncts
		if (c.get().isConjunction(c)) {
			List<Section<? extends NonTerminalCondition>> conjuncts = c.get().getConjuncts(
					c);

			List<Condition> conds = new ArrayList<Condition>();
			for (Section<? extends NonTerminalCondition> conjunct : conjuncts) {
				Section<? extends CompositeCondition> subCondSection = conjunct.findChildOfType(CompositeCondition.class);
				Condition subCond = createCondition((Section<CompositeCondition>) subCondSection);
				conds.add(subCond);
			}

			Condition cond = new CondAnd(conds);
			return cond;
		}

		// create disjunctions
		if (c.get().isDisjunction(c)) {
			List<Section<? extends NonTerminalCondition>> disjuncts = c.get().getDisjuncts(
					c);

			List<Condition> conds = new ArrayList<Condition>();
			for (Section<? extends NonTerminalCondition> disjunct : disjuncts) {
				Section<? extends CompositeCondition> subCondSection = disjunct.findChildOfType(CompositeCondition.class);
				Condition subCond = createCondition((Section<CompositeCondition>) subCondSection);
				conds.add(subCond);
			}

			Condition cond = new CondOr(conds);
			return cond;
		}

		// create negations
		if (c.get().isNegation(c)) {
			// can only be one
			Section<? extends NonTerminalCondition> neg = c.get().getNegation(
					c);
			Section<? extends CompositeCondition> subCondSection = neg.findChildOfType(CompositeCondition.class);
			Condition subCond = createCondition((Section<CompositeCondition>) subCondSection);
			Condition cond = new CondNot(subCond);
			return cond;
		}

		// end of recursion => (let) create terminal conditions
		if (c.get().isTerminal(c)) {
			Section<? extends TerminalCondition> terminal = c.get().getTerminal(c);

			Section<? extends D3webTerminalCondition> termChild = terminal.findChildOfType(D3webTerminalCondition.class);

			Condition cond = termChild.get().getTerminalCondition(termChild);

			return cond;
		}



		return null;

	}

}
