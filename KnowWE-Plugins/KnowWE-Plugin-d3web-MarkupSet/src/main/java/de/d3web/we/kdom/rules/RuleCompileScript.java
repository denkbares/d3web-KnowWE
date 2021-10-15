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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondNonTerminalUnknown;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.action.D3webRuleAction;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.rules.action.ActionContainer;
import de.d3web.we.kdom.rules.action.ElseActionContainer;
import de.d3web.we.kdom.rules.action.ThenActionContainer;
import de.d3web.we.kdom.rules.action.UnknownActionContainer;
import de.d3web.we.kdom.rules.condition.ExceptionConditionContainer;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * This script compiles a parsed rule into the d3web knowledge base.
 *
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 * @created 11.02.2014
 */
public class RuleCompileScript implements D3webCompileScript<RuleType> {

	public static final String DEFAULT_RULE_STORE_KEY = "DEFAULT_RULE_STORE_KEY";
	public static final String ELSE_RULE_STORE_KEY = "ELSE_RULE_STORE_KEY";
	public static final String UNKNOWN_RULE_STORE_KEY = "UNKNOWN_RULE_STORE_KEY";

	@Override
	public void compile(D3webCompiler compiler, Section<RuleType> ruleSection) throws CompilerMessage {

		if (ruleSection.hasErrorInSubtree(compiler)) {
			throw new CompilerMessage(Messages.creationFailedWarning(Rule.class.getSimpleName()));
		}

		Condition ifCondition = getCondition(compiler, ruleSection);
		Condition exceptCondition = getExceptConditions(compiler, ruleSection);
		Collection<RuleAction> thenActions = getThenAction(compiler, ruleSection);
		Collection<RuleAction> elseActions = getElseAction(compiler, ruleSection);
		Collection<RuleAction> unknownActions = getUnknownAction(compiler, ruleSection);

		if (thenActions.isEmpty()) {
			throw CompilerMessage.error("No action found.");
		}
		if (ifCondition == null) {
			throw CompilerMessage.error("No condition found.");
		}

		// create default IF - THEN - EXCEPT rules
		createRules(compiler, ruleSection, ifCondition, exceptCondition, thenActions, DEFAULT_RULE_STORE_KEY);

		Condition elseCondition = new CondNot(ifCondition);
		Condition ntUnknownCondition = new CondNonTerminalUnknown(Collections.singletonList(ifCondition));

		// the else action has to also fire if except is true
		if (exceptCondition != null) {
			elseCondition = new CondOr(Arrays.asList(elseCondition, exceptCondition));
		}

		// if there is a else action but no unknown action, we assume the user wants else to also include unknown
		if (!elseActions.isEmpty() && unknownActions.isEmpty()) {
			elseCondition = new CondOr(Arrays.asList(elseCondition, ntUnknownCondition));
		}

		// create IF - ELSE - EXCEPT rules
		createRules(compiler, ruleSection, elseCondition, null, elseActions, ELSE_RULE_STORE_KEY);

		// create IF - [ELSE] - UNKNOWN - EXCEPT rules
		createRules(compiler, ruleSection, ntUnknownCondition, null, unknownActions, UNKNOWN_RULE_STORE_KEY);
	}

	public static void createRules(D3webCompiler compiler, Section<? extends Type> ruleSection, Condition condition, Condition exceptCondition, Collection<RuleAction> thenAction, String key) {
		Collection<Rule> rules = new ArrayList<>(thenAction.size());
		for (RuleAction action : thenAction) {
			@SuppressWarnings("unchecked")
			Rule rule = RuleFactory.createRule(action.action, condition, exceptCondition, action.psContext);
			rules.add(rule);
		}
		ruleSection.storeObject(compiler, key, Collections.unmodifiableCollection(rules));
	}

	private Collection<RuleAction> getThenAction(D3webCompiler compiler, Section<RuleType> ruleSection) {
		return getRuleAction(compiler, ruleSection, ThenActionContainer.class);
	}

	private Collection<RuleAction> getElseAction(D3webCompiler compiler, Section<RuleType> ruleSection) {
		return getRuleAction(compiler, ruleSection, ElseActionContainer.class);
	}

	private Collection<RuleAction> getUnknownAction(D3webCompiler compiler, Section<RuleType> ruleSection) {
		return getRuleAction(compiler, ruleSection, UnknownActionContainer.class);
	}

	private <T extends ActionContainer> Collection<RuleAction> getRuleAction(D3webCompiler compiler, Section<RuleType> ruleSection, Class<T> containerClass) {
		List<Section<T>> actionContainerSections = Sections.successors(ruleSection, containerClass);
		Collection<RuleAction> actions = new ArrayList<>();
		for (Section<T> actionContainerSection : actionContainerSections) {
			@SuppressWarnings("rawtypes")
			List<Section<D3webRuleAction>> actionSections = Sections.successors(actionContainerSection,
					D3webRuleAction.class);
			//noinspection rawtypes
			for (Section<D3webRuleAction> actionSection : actionSections) {
				@SuppressWarnings("unchecked")
				PSAction action = actionSection.get().getAction(compiler, actionSection);
				Class<?> context = actionSection.get().getProblemSolverContext();
				actions.add(new RuleAction(action, context));
			}
		}
		return actions;
	}

	private Condition getCondition(D3webCompiler compiler, Section<RuleType> ruleSection) {
		Section<CompositeCondition> conditionSection = Sections.successor(ruleSection,
				CompositeCondition.class);
		return KDOMConditionFactory.createCondition(compiler, conditionSection);
	}

	private Condition getExceptConditions(D3webCompiler compiler, Section<RuleType> ruleSection) throws CompilerMessage {
		List<Section<ExceptionConditionContainer>> exceptConditionSection = Sections
				.successors(ruleSection, ExceptionConditionContainer.class);
		if (exceptConditionSection.size() > 1) {
			throw CompilerMessage.error("There can only be one EXCEPT condition fore each rule");
		}
		else if (exceptConditionSection.isEmpty()) {
			return null;
		}
		Section<CompositeCondition> conditionSection = Sections
				.successor(exceptConditionSection.get(0), CompositeCondition.class);
		if (conditionSection == null) return null;
		return KDOMConditionFactory.createCondition(compiler, conditionSection);
	}

	@Override
	public void destroy(D3webCompiler compiler, Section<RuleType> section) {
		deleteRule(compiler, section, DEFAULT_RULE_STORE_KEY);
		deleteRule(compiler, section, ELSE_RULE_STORE_KEY);
		deleteRule(compiler, section, UNKNOWN_RULE_STORE_KEY);
	}

	private void deleteRule(D3webCompiler compiler, Section<RuleType> section, String ruleStoreKey) {
		Collection<Rule> rules = getRules(compiler, section, ruleStoreKey);
		for (Rule rule : rules) {
			rule.remove();
		}
		section.removeObject(compiler, ruleStoreKey);
	}

	/**
	 * Returns a collection of all rules created with the given section and compiler.
	 */
	public static Collection<Rule> getRules(D3webCompiler compiler, Section<RuleType> section) {
		Collection<Rule> rules = new ArrayList<>();
		rules.addAll(getDefaultRules(compiler, section));
		rules.addAll(getElseRules(compiler, section));
		rules.addAll(getUnknownRules(compiler, section));
		return rules;
	}

	/**
	 * Returns the first of the rules created with the THEN actions of the given section and compiler or null if no
	 * rules were created.
	 */
	public static Rule getRule(D3webCompiler compiler, Section<RuleType> section) {
		Collection<Rule> defaultRules = getDefaultRules(compiler, section);
		if (defaultRules.isEmpty()) {
			return null;
		}
		else {
			return defaultRules.iterator().next();
		}
	}

	/**
	 * Returns the collection of rules created with the THEN actions of the given section and compiler.
	 */
	public static Collection<Rule> getDefaultRules(D3webCompiler compiler, Section<RuleType> section) {
		return getRules(compiler, section, DEFAULT_RULE_STORE_KEY);
	}

	/**
	 * Returns the collection of rules created with the ELSE actions of the given section and compiler.
	 */
	public static Collection<Rule> getElseRules(D3webCompiler compiler, Section<RuleType> section) {
		return getRules(compiler, section, ELSE_RULE_STORE_KEY);
	}

	/**
	 * Returns the collection of rules created with the UNKNOWN actions of the given section and compiler.
	 */
	public static Collection<Rule> getUnknownRules(D3webCompiler compiler, Section<RuleType> section) {
		return getRules(compiler, section, UNKNOWN_RULE_STORE_KEY);
	}

	public static Collection<Rule> getRules(D3webCompiler compiler, Section<? extends Type> section, String ruleStoreKey) {
		Collection<Rule> rules = section.getObject(compiler, ruleStoreKey);
		if (rules == null) {
			return Collections.emptyList();
		}
		else {
			return rules;
		}
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<RuleType> section) {
		return true;
	}
}
