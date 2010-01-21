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

package tests;

import java.util.HashMap;
import java.util.Iterator;

import utils.KBCreationTestUtil;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.Rule;
import de.d3web.kernel.domainModel.formula.FormulaExpression;
import de.d3web.kernel.psMethods.PSMethod;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.kernel.psMethods.nextQASet.ActionIndication;
import de.d3web.kernel.psMethods.nextQASet.ActionRefine;
import de.d3web.kernel.psMethods.nextQASet.PSMethodNextQASet;
import de.d3web.kernel.psMethods.questionSetter.ActionQuestionSetter;
import de.d3web.kernel.psMethods.questionSetter.PSMethodQuestionSetter;
import junit.framework.TestCase;

/**
 * This class tests whether the Rules
 * are created as expected. 
 * 
 * This test also covers the rules
 * that are created "automatically" 
 * in the DecisionTree.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class RulesTest extends TestCase {
	
	public void testHeuristicRules() {
		
		// load KnowledgeBases
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
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
					assertEquals("Rule " + createdRule.getId() + " has wrong exception conditiontype.", 
							createdRule.getException().getClass(), loadedRule.getException().getClass());
					
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
					assertEquals("ActionHeuristicPS of " + createdRule.getId() + " has wrong Diagnosis.",
							((ActionHeuristicPS)createdRule.getAction()).getDiagnosis(),
							((ActionHeuristicPS)loadedRule.getAction()).getDiagnosis());
					
					// Compare Score of ActionHeuristicPS
					assertEquals("ActionHeuristicPS of " + createdRule.getId() + " has wrong Score.",
							((ActionHeuristicPS)createdRule.getAction()).getScore(),
							((ActionHeuristicPS)loadedRule.getAction()).getScore());
				}
			
			} else {
				
				assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
				assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));
				
			}
			
		}
		
	}
	
	public void testNextQASetRules() {
		
		// load KnowledgeBases
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
				
		// load Rules in HashMaps (necessary because they are unsorted)
		HashMap<String, Rule> loadedRules = 
			getRulesInHashMap(loadedKB, PSMethodNextQASet.class);
		HashMap<String, Rule> createdRules = 
			getRulesInHashMap(createdKB, PSMethodNextQASet.class);
		
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
					assertEquals("ActionIndication of " + createdRule.getId() + " has wrong QASets.",
							((ActionIndication)createdRule.getAction()).getQASets(),
							((ActionIndication)loadedRule.getAction()).getQASets());
				}
				
				// ActionRefine specific tests
				if (createdRule.getAction() instanceof ActionRefine) {
					
					// Compare QASets of ActionRefine
					assertEquals("ActionRefine of " + createdRule.getId() + " has wrong QASets.",
							((ActionRefine)createdRule.getAction()).getQASets(),
							((ActionRefine)loadedRule.getAction()).getQASets());
				}
			
			} else {
				
				assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
				assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));
				
			}
			
		}

	}
	
	public void testSetValueRules() {
		
		// load KnowledgeBases
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
			
		// load Rules in HashMaps (necessary because they are unsorted)
		HashMap<String, Rule> loadedRules = 
			getRulesInHashMap(loadedKB, PSMethodQuestionSetter.class);
		HashMap<String, Rule> createdRules = 
			getRulesInHashMap(createdKB, PSMethodQuestionSetter.class);
		
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
				if (createdRule.getAction() instanceof ActionQuestionSetter) {
					
					ActionQuestionSetter createdAction = (ActionQuestionSetter)createdRule.getAction();
					ActionQuestionSetter loadedAction = (ActionQuestionSetter)loadedRule.getAction();
					
					// Compare questions
					assertEquals("ActionSetValue of " + createdRule.getId() + " has wrong Question.",
							createdAction.getQuestion(),
							createdAction.getQuestion());
					
					// Compare types of ActionSetValue values
					assertEquals("ActionSetValue of " + createdRule.getId() + " has wrong valuetype.", 
							createdAction.getValues()[0].getClass(), 
							loadedAction.getValues()[0].getClass());
					
					// FormulaExpression specific tests
					if (createdAction.getValues()[0] instanceof FormulaExpression) {
						FormulaExpression createdFormula = 
							(FormulaExpression) createdAction.getValues()[0];
						FormulaExpression loadedFormula = 
							(FormulaExpression) loadedAction.getValues()[0];
						
						// Compare Question of FormulaExpression
						assertEquals("FormulaExpression of " + createdRule.getId() + " has wrong QuestionNum", 
								createdFormula.getQuestionNum(), 
								loadedFormula.getQuestionNum());
						
						// Compare FormulaExpression formula (sorry for the toString() comparison)
						assertEquals("FormulaExpression of " + createdRule.getId() + " has wrong FormulaElement", 
								createdFormula.getFormulaElement().toString(), 
								loadedFormula.getFormulaElement().toString());
					}
					
				} else {
					
					assertNotNull("Rule " + key + " does not exist!", createdRules.containsKey(key));
					assertNotNull("Rule " + key + " does not exist!", loadedRules.containsKey(key));
					
				}
			
			}
		}
		
	}
	

	/**
	 * Stores Rules in a HashMap. We have to do this, because otherwise
	 * they are not comparable because the collection in the KB is accessible
	 * only through an iterator and not sorted.
	 * @param kb KnowledgeBase
	 * @param PSMethod PSMethod
	 * @return HashMap<String, RuleComplex>
	 */
	private HashMap<String, Rule> getRulesInHashMap(KnowledgeBase kb, 
			Class<? extends PSMethod> PSMethod) {
		
		HashMap<String, Rule> rules = new HashMap<String, Rule>();
		Iterator<KnowledgeSlice> iter = kb.getAllKnowledgeSlicesFor(PSMethod).iterator();
		
		while (iter.hasNext()) {

			Rule rule = (Rule) iter.next();
			rules.put(rule.getId(), rule);

		}
		
		return rules;
		
	}

}
