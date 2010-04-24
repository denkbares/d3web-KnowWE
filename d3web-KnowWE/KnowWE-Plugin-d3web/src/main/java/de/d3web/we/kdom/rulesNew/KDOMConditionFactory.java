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
