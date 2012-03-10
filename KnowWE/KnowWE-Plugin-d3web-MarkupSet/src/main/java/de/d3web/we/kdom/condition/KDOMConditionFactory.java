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
import java.util.logging.Logger;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * 
 * this class offers a method to create a (d3web-) Condition from a
 * CompositeCondition KDOM.
 * 
 * @see CompositeCondition
 * 
 * @author Jochen
 * @created 26.07.2010
 */
public class KDOMConditionFactory {

	@SuppressWarnings("unchecked")
	public static Condition createCondition(Article article, Section<? extends CompositeCondition> c) {
		if (c == null) return null;

		// if braced - delegate to next composite
		if (c.get().isBraced(c)) {
			Section<? extends NonTerminalCondition> braced = c.get().getBraced(c);
			return createCondition(article,
					Sections.findSuccessor(braced, CompositeCondition.class));
		}

		// create conjuncts
		if (c.get().isConjunction(c)) {
			List<Section<? extends NonTerminalCondition>> conjuncts = c.get().getConjuncts(
					c);

			List<Condition> conds = new ArrayList<Condition>();
			for (Section<? extends NonTerminalCondition> conjunct : conjuncts) {
				Section<? extends CompositeCondition> subCondSection = Sections.findChildOfType(
						conjunct, CompositeCondition.class);
				Condition subCond = createCondition(article,
						(Section<CompositeCondition>) subCondSection);
				if (subCond == null) return null;
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
				Section<? extends CompositeCondition> subCondSection = Sections.findChildOfType(
						disjunct, CompositeCondition.class);
				Condition subCond = createCondition(article,
						(Section<CompositeCondition>) subCondSection);
				if (subCond == null) return null;
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
			Section<? extends CompositeCondition> subCondSection = Sections.findChildOfType(neg,
					CompositeCondition.class);
			Condition subCond = createCondition(article,
					(Section<CompositeCondition>) subCondSection);
			if (subCond == null) return null;
			Condition cond = new CondNot(subCond);
			return cond;
		}

		// end of recursion => (let) create terminal conditions
		if (c.get().isTerminal(c)) {
			Section<? extends TerminalCondition> terminal = c.get().getTerminal(c);

			Section<? extends D3webCondition> termChild = Sections.findChildOfType(terminal,
					D3webCondition.class);

			if (termChild == null) {
				Logger.getLogger(KDOMConditionFactory.class.getName()).warning(
						"Could not create Condition for: " + terminal.getFather());
				return null;
			}

			Condition cond = termChild.get().getCondition(article, termChild);

			return cond;
		}

		return null;

	}

}
