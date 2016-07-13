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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.BeforeClass;

import utils.KBTestUtilNewMarkup;
import utils.TestArticleManager;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.KnowledgeKind;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.indication.inference.PSMethodStrategic;
import com.denkbares.plugin.test.InitPluginManager;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.knowwe.core.kdom.Article;

/**
 * This class tests whether the Rules are created as expected.
 * 
 * This test also covers the rules that are created "automatically" in the
 * DecisionTree.
 * 
 * TODO: Commented some assertions out. Because the new Markup makes them fail.
 * Johannes
 * 
 * @author Sebastian Furth
 * @see KBTestUtilNewMarkup to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class RulesTest extends TestCase {

	@Override
	@BeforeClass
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	/**
	 * TODO: Expand this to check if brackets are parsed right.
	 */
	public void testHeuristicRules() {
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();
		// load KnowledgeBases
		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);

		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodHeuristic.FORWARD);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodHeuristic.FORWARD);

		// Check number of rules
		checkRules(loadedRules, createdRules);

	}

	private void checkRules(Collection<Rule> loadedRules, Collection<Rule> createdRules) {

		List<String> loadedRulesStrings = new ArrayList<>();
		List<String> createdRulesStrings = new ArrayList<>();
		for (Rule rule : createdRules) {
			createdRulesStrings.add(rule.toString());
		}
		for (Rule rule : loadedRules) {
			loadedRulesStrings.add(rule.toString());
		}

		for (String ruleString : createdRulesStrings) {
			assertTrue("Rule " + ruleString + " does not exist!",
					loadedRulesStrings.contains(ruleString));
		}

		for (String ruleString : loadedRulesStrings) {
			assertTrue("Rule " + ruleString + " does not exist!",
					createdRulesStrings.contains(ruleString));
		}

		assertEquals("Wrong number of rules for PSMethodHeuristic.",
				createdRules.size(), loadedRules.size());
	}

	public void testNextQASetRules() {
		System.out.println();
		// load KnowledgeBases
		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodStrategic.FORWARD);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodStrategic.FORWARD);

		// Check number of rules
		checkRules(loadedRules, createdRules);

	}

	public void testSetValueRules() {
		// load KnowledgeBases
		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		// load Rules in HashMaps (necessary because they are unsorted)
		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodAbstraction.FORWARD);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodAbstraction.FORWARD);

		// Check number of rules
		checkRules(loadedRules, createdRules);

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
	private Collection<Rule> getRulesInHashMap(KnowledgeBase kb, KnowledgeKind<RuleSet> kind) {
		Set<Rule> rules = new HashSet<>();
		for (RuleSet rs : kb.getAllKnowledgeSlicesFor(kind)) {
			rules.addAll(rs.getRules());
		}
		return rules;

	}

}
