package de.d3web.kernel.psMethods.diaFlux.predicates;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.NoAnswerException;
import de.d3web.kernel.domainModel.ruleCondition.UnknownAnswerException;

/**
 * 
 * @author hatko
 *
 */
public class RulePredicate implements IPredicate {
	
	private final AbstractCondition condition;

	public RulePredicate(AbstractCondition condition) {

		if (condition == null)
			throw new IllegalArgumentException();
		
		this.condition = condition;
	}
	
	
	
	public AbstractCondition getCondition() {
		return condition;
	}



	@Override
	public boolean evaluate(XPSCase theCase) {
		try {
			return getCondition().eval(theCase);
		} catch (NoAnswerException e) {
			return false;
		} catch (UnknownAnswerException e) {
			return false;
		}
	}
	
	
	
	

}
