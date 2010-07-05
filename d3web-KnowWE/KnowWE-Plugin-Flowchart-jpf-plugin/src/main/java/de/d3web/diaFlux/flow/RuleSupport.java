package de.d3web.diaFlux.flow;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;

/**
 * @author Reinhard Hatko
 *
 * Created: 20.12.2009
 */
public class RuleSupport implements ISupport {

	private final Rule rule;
	
	
	/**
	 * @param condition
	 */
	public RuleSupport(Rule rule) {
		this.rule = rule;
	}


	@Override
	public boolean isValid(Session session) {
		try {
			
			rule.check(session);
			
			return rule.canFire(session);
		} catch (UnknownAnswerException e) {
			return false;
		}
	}
	
	
	/**
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

}
