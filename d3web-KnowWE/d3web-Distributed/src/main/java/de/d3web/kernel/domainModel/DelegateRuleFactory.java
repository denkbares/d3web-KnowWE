package de.d3web.kernel.domainModel;

import java.util.List;

import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.delegate.ActionDelegate;
import de.d3web.kernel.psMethods.delegate.ActionInstantDelegate;

public class DelegateRuleFactory {
	
	public static RuleComplex createDelegateRule(
		String theId,
		List theAction,
		String ns, AbstractCondition theCondition) {

		RuleComplex rule = RuleFactory.createRule(theId);

		ActionDelegate ruleAction = new ActionDelegate(rule);
		ruleAction.setNamedObjects(theAction);
		ruleAction.setTargetNamespace(ns);
		RuleFactory.setRuleParams(rule, ruleAction, theCondition, null);
		return rule;
	}
	
	public static RuleComplex createInstantDelegateRule(
			String theId,
			List theAction,
			String ns, AbstractCondition theCondition) {

			RuleComplex rule = RuleFactory.createRule(theId);

			ActionInstantDelegate ruleAction = new ActionInstantDelegate(rule);
			ruleAction.setNamedObjects(theAction);
			ruleAction.setTargetNamespace(ns);
			RuleFactory.setRuleParams(rule, ruleAction, theCondition, null);
			return rule;
		}
}
