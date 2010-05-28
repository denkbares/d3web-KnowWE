package de.d3web.we.kdom.rulesNew.terminalCondition;

import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public abstract class D3webTerminalCondition<T extends KnowWEObjectType> extends DefaultAbstractKnowWEObjectType {

	public abstract TerminalCondition getTerminalCondition(Section<T> s);

}
