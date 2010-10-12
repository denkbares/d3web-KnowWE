/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;
import utils.KBCreationTestUtil;
import utils.MyTestArticleManager;
import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaElement;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.ActionRefine;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;

/**
 * This class tests whether the Rules are created as expected.
 * 
 * This test also covers the rules that are created "automatically" in the
 * DecisionTree.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class RulesTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
		//Enfore Autocompile
		KnowWEPackageManager.overrideAutocompileArticle(true);
	}

	public void testHeuristicRules() {

		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager.getArticle(KBCreationTestUtil.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = MyTestArticleManager.getKnowledgeBase(art);
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();

		// load Rules in HashMaps (necessary because they are unsorted)
		HashMap<String, Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodHeuristic.class);
		HashMap<String, Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodHeuristic.class);

		// Check number of rules
		assertEquals("Wrong number of rules for PSMethodHeuristic.",
				createdRules.size(), loadedRules.size());

		Rule createdRule;
		Rule loadedRule;

		for (String key : createdRules.keySet()) {

			if (createdRules.containsKey(key) && loadedRules.containsKey(key)) {

				createdRule = createdRules.get(key);
				loadedRule = loadedRules.get(key);

				// Compare ConditionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong conditiontype.",
						createdRule.getCondition().getClass(), loadedRule.getCondition().getClass());

				// Compare Conditions
				assertEquals("Rule " + createdRule.getId() + " has wrong condition.",
						createdRule.getCondition(), loadedRule.getCondition());

				// Check if there is an exception condition
				if (createdRule.getException() != null && loadedRule.getException() != null) {

					// Compare Exception ConditionTypes
					assertEquals("Rule " + createdRule.getId()
							+ " has wrong exception conditiontype.",
							createdRule.getException().getClass(),
							loadedRule.getException().getClass());

					// Compare Exception Conditions
					assertEquals("Rule " + createdRule.getId() + " has wrong exception condition.",
							createdRule.getException(), loadedRule.getException());

				}

				// Compare ActionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong actiontype.",
						createdRule.getAction().getClass(), loadedRule.getAction().getClass());

				// ActionIndication specific tests
				if (createdRule.getAction() instanceof ActionHeuristicPS) {

					// Compare Diagnosis of ActionHeuristicPS
					assertEquals("ActionHeuristicPS of " + createdRule.getId()
							+ " has wrong Diagnosis.",
							((ActionHeuristicPS) createdRule.getAction()).getSolution(),
							((ActionHeuristicPS) loadedRule.getAction()).getSolution());

					// Compare Score of ActionHeuristicPS
					assertEquals("ActionHeuristicPS of " + createdRule.getId()
							+ " has wrong Score.",
							((ActionHeuristicPS) createdRule.getAction()).getScore(),
							((ActionHeuristicPS) loadedRule.getAction()).getScore());
				}

			}
			else {

				assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
				assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));

			}

		}

	}

	public void testNextQASetRules() {

		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager.getArticle(KBCreationTestUtil.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = MyTestArticleManager.getKnowledgeBase(art);
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();

		// load Rules in HashMaps (necessary because they are unsorted)
		HashMap<String, Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodStrategic.class);
		HashMap<String, Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodStrategic.class);

		// Check number of rules
		assertEquals("Wrong number of rules for PSMethodNextQASet.",
				createdRules.size(), loadedRules.size());

		Rule createdRule;
		Rule loadedRule;

		for (String key : createdRules.keySet()) {

			if (createdRules.containsKey(key) && loadedRules.containsKey(key)) {

				createdRule = createdRules.get(key);
				loadedRule = loadedRules.get(key);

				// Compare ConditionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong conditiontype.",
						createdRule.getCondition().getClass(), loadedRule.getCondition().getClass());

				// Compare Conditions
				assertEquals("Rule " + createdRule.getId() + " has wrong condition.",
						createdRule.getCondition(), loadedRule.getCondition());

				// Compare ActionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong actiontype.",
						createdRule.getAction().getClass(), loadedRule.getAction().getClass());

				// ActionIndication specific tests
				if (createdRule.getAction() instanceof ActionIndication) {

					// Compare QASets of ActionIndication
					assertEquals("ActionIndication of " + createdRule.getId()
							+ " has wrong QASets.",
							((ActionIndication) createdRule.getAction()).getQASets(),
							((ActionIndication) loadedRule.getAction()).getQASets());
				}

				// ActionRefine specific tests
				if (createdRule.getAction() instanceof ActionRefine) {

					// Compare QASets of ActionRefine
					assertEquals("ActionRefine of " + createdRule.getId() + " has wrong QASets.",
							((ActionRefine) createdRule.getAction()).getQASets(),
							((ActionRefine) loadedRule.getAction()).getQASets());
				}

			}
			else {

				assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
				assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));

			}

		}

	}

	public void testSetValueRules() {

		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager.getArticle(KBCreationTestUtil.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = MyTestArticleManager.getKnowledgeBase(art);
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();

		// load Rules in HashMaps (necessary because they are unsorted)
		HashMap<String, Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodAbstraction.class);
		HashMap<String, Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodAbstraction.class);

		// Check number of rules
		assertEquals("Wrong number of rules for PSMethodQuestionSetter.",
				createdRules.size(), loadedRules.size());

		Rule createdRule;
		Rule loadedRule;

		for (String key : createdRules.keySet()) {

			if (createdRules.containsKey(key) && loadedRules.containsKey(key)) {

				createdRule = createdRules.get(key);
				loadedRule = loadedRules.get(key);

				// Compare ConditionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong conditiontype.",
						createdRule.getCondition().getClass(), loadedRule.getCondition().getClass());

				// Compare Conditions
				assertEquals("Rule " + createdRule.getId() + " has wrong condition.",
						createdRule.getCondition(), loadedRule.getCondition());

				// Compare ActionTypes
				assertEquals("Rule " + createdRule.getId() + " has wrong actiontype.",
						createdRule.getAction().getClass(), loadedRule.getAction().getClass());

				// ActionSetValue specific tests
				if (createdRule.getAction() instanceof ActionSetValue) {

					ActionSetValue createdAction = (ActionSetValue) createdRule.getAction();
					ActionSetValue loadedAction = (ActionSetValue) loadedRule.getAction();

					// Compare questions
					assertEquals("ActionSetValue of " + createdRule.getId()
							+ " has wrong Question.",
							createdAction.getQuestion(),
							createdAction.getQuestion());

					// Compare types of ActionSetValue values
					assertEquals("ActionSetValue of " + createdRule.getId()
							+ " has wrong valuetype.",
							createdAction.getValue().getClass(),
							loadedAction.getValue().getClass());

					// FormulaExpression specific tests
					if (createdAction.getValue() instanceof FormulaElement) {
						FormulaElement createdFormula =
								(FormulaElement) createdAction.getValue();
						FormulaElement loadedFormula =
								(FormulaElement) loadedAction.getValue();

						// Compare FormulaExpression formula (sorry for the
						// toString() comparison)
						assertEquals("FormulaExpression of " + createdRule.getId()
								+ " has wrong FormulaElement",
								createdFormula.toString(),
								loadedFormula.toString());
					}

				}
				else {

					assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
					assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));

				}

			}
		}

	}

	/**
	 * Stores Rules in a HashMap. We have to do this, because otherwise they are
	 * not comparable because the collection in the KB is accessible only
	 * through an iterator and not sorted.
	 * 
	 * @param kb KnowledgeBase
	 * @param PSMethod PSMethod
	 * @return HashMap<String, RuleComplex>
	 */
	private HashMap<String, Rule> getRulesInHashMap(KnowledgeBase kb,
			Class<? extends PSMethod> PSMethod) {

		HashMap<String, Rule> rules = new HashMap<String, Rule>();
		Iterator<KnowledgeSlice> iter = kb.getAllKnowledgeSlicesFor(PSMethod).iterator();

		while (iter.hasNext()) {
			RuleSet rs = (RuleSet) iter.next();
			for (Rule rule : rs.getRules()) {
				rules.put(rule.getId(), rule);
			}
		}

		return rules;

	}

}
