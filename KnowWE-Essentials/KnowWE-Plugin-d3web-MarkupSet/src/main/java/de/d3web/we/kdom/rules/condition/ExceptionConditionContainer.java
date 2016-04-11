package de.d3web.we.kdom.rules.condition;

import de.d3web.we.kdom.rules.RuleContainerFinder;
import de.d3web.we.kdom.rules.RuleTokenType;
import de.d3web.we.kdom.rules.RuleType;
import de.knowwe.core.kdom.TypePriorityList;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.03.2014
 */
public class ExceptionConditionContainer extends ConditionContainer {

	public ExceptionConditionContainer() {
		setSectionFinder(new RuleContainerFinder(RuleType.EXCEPT_TOKENS, RuleType.INNER_TOKENS));
		addChildType(TypePriorityList.DEFAULT_PRIORITY - 1, new RuleTokenType(RuleType.EXCEPT_TOKENS));
	}
}
