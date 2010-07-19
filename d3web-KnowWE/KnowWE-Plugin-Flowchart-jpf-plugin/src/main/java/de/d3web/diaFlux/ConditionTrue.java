package de.d3web.diaFlux;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;

/**
 *
 * @author hatko
 * Created on: 09.10.2009
 */
public class ConditionTrue extends TerminalCondition {
	
	public static final Condition INSTANCE = new ConditionTrue();
	
	
	private ConditionTrue() {
		super(null);
	}

	@Override
	public boolean eval(Session session) throws NoAnswerException,
			UnknownAnswerException {
		return true;
	}

	@Override
	public Condition copy() {
		return this;
	}
}