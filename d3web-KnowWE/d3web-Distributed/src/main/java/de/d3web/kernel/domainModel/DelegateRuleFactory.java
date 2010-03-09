/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.kernel.domainModel;

import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.AbstractCondition;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.manage.RuleFactory;
import de.d3web.kernel.psMethods.delegate.ActionDelegate;
import de.d3web.kernel.psMethods.delegate.ActionInstantDelegate;

public class DelegateRuleFactory {
	
	public static Rule createDelegateRule(
		String theId,
		List<NamedObject> theAction,
		String ns, AbstractCondition theCondition) {

		Rule rule = RuleFactory.createRule(theId);

		ActionDelegate ruleAction = new ActionDelegate();
		ruleAction.setRule(rule);
		ruleAction.setNamedObjects(theAction);
		ruleAction.setTargetNamespace(ns);
		RuleFactory.setRuleParams(rule, ruleAction, theCondition, null);
		return rule;
	}
	
	public static Rule createInstantDelegateRule(
			String theId,
			List<NamedObject> theAction,
			String ns, AbstractCondition theCondition) {

			Rule rule = RuleFactory.createRule(theId);

			ActionInstantDelegate ruleAction = new ActionInstantDelegate();
			ruleAction.setRule(rule);
			ruleAction.setNamedObjects(theAction);
			ruleAction.setTargetNamespace(ns);
			RuleFactory.setRuleParams(rule, ruleAction, theCondition, null);
			return rule;
		}
}
