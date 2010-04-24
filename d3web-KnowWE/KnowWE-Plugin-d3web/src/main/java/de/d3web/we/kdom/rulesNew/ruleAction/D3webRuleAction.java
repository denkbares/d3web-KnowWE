package de.d3web.we.kdom.rulesNew.ruleAction;

import de.d3web.core.inference.PSAction;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public abstract class D3webRuleAction<T extends KnowWEObjectType> extends DefaultAbstractKnowWEObjectType {

	public abstract PSAction getAction(Section<T> s);
}
