package de.d3web.we.kdom.rules.utils;

import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethodRulebased;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.RuleFactory;
import de.d3web.scoring.inference.PSMethodHeuristic;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.09.17.
 */
public class RuleCreationUtil {


	public static void createRules(List<Condition> conditions, List<PSAction> actions) {
		for (PSAction action : actions) {
			Condition condition = combineConditionsToConjunction(conditions);
			Class<? extends PSMethodRulebased> psMethodContext;
			if (action instanceof ActionSetQuestion) {
				psMethodContext = PSMethodAbstraction.class;
			}
			else {
				psMethodContext = PSMethodHeuristic.class;
			}
			RuleFactory.createRule(action, condition, null, psMethodContext);
		}
	}

	public static Condition combineConditionsToConjunction(List<Condition> conditions) {
		if (conditions.size() == 1) return conditions.get(0);
		return new CondAnd(conditions);
	}
}
