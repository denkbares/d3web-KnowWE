package de.d3web.we.kdom.rules.condition;

import de.d3web.we.kdom.rules.RuleContainerFinder;
import de.d3web.we.kdom.rules.RuleType;
import de.knowwe.core.kdom.TypePriorityList;

/**
 * Created by Albrecht Striffler (denkbares GmbH) on 02.03.14.
 */
public class ExceptionConditionContainer extends ConditionContainer {

	public ExceptionConditionContainer() {
		setSectionFinder(new RuleContainerFinder(RuleType.EXCEPT_TOKENS, RuleType.INNER_TOKENS));
		addChildType(TypePriorityList.DEFAULT_PRIORITY - 1, new ExceptType());
	}
}
