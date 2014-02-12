/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.kdom.rules;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.02.2014
 */
/**
 * This handler compiles a parsed rule into the d3web knowledge base (if it
 * doesn't have errors)
 * 
 * @author Jochen
 */
public class RuleCompileScript extends D3webCompileScript<RuleAction> {

	public static final String RULE_STORE_KEY = "RULE_STORE_KEY";

	@Override
	public void compile(D3webCompiler compiler, Section<RuleAction> section) throws CompilerMessage {

		Section<ConditionActionRuleContent> ruleSection = Sections
				.findAncestorOfType(section,
						ConditionActionRuleContent.class);

		if (ruleSection.hasErrorInSubtree(compiler)) {
			throw new CompilerMessage(
					Messages.creationFailedWarning(Rule.class.getSimpleName()));
		}

		// create condition
		Section<CompositeCondition> cond = Sections.findSuccessor(ruleSection,
				CompositeCondition.class);
		Condition d3Cond = KDOMConditionFactory.createCondition(compiler,
				cond);

		// create action
		@SuppressWarnings("rawtypes")
		Section<D3webRuleAction> action = Sections.findSuccessor(section,
				D3webRuleAction.class);
		if (action == null) {
			throw new CompilerMessage(
					Messages.creationFailedWarning(
							D3webUtils.getD3webBundle().getString(
									"KnowWE.rulesNew.notcreated")
									+ " : no valid action found"));
		}
		@SuppressWarnings("unchecked")
		PSAction d3action = action.get().getAction(compiler, action);

		// create exception (if exists)
		Section<ExceptionConditionArea> exceptionCondSec = Sections
				.findSuccessor(ruleSection, ExceptionConditionArea.class);
		Condition exceptionCond = null;
		if (exceptionCondSec != null) {
			Section<CompositeCondition> exceptionCompCondSec = Sections
					.findSuccessor(exceptionCondSec,
							CompositeCondition.class);
			if (exceptionCompCondSec != null) {
				exceptionCond = KDOMConditionFactory.createCondition(
						compiler, exceptionCompCondSec);
			}
		}

		// create actual rule
		if (d3action != null && d3Cond != null) {
			@SuppressWarnings("unchecked")
			Rule rule = RuleFactory.createRule(d3action, d3Cond,
					exceptionCond, action.get().getActionPSContext());
			if (rule != null) {
				KnowWEUtils.storeObject(compiler, section, RULE_STORE_KEY, rule);
				throw new CompilerMessage(
						Messages.objectCreatedNotice(
								"Rule"));
			}

		}

		// should not happen
		throw new CompilerMessage(Messages.creationFailedWarning(
				D3webUtils.getD3webBundle().getString("KnowWE.rulesNew.notcreated")));
	}

	@Override
	public void destroy(D3webCompiler compiler, Section<RuleAction> section) {
		Rule kbr = (Rule) section.getSectionStore().getObject(compiler,
				RULE_STORE_KEY);
		if (kbr != null) {
			kbr.remove();
		}
	}

}
