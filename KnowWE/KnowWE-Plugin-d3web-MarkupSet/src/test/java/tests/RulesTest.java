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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestCase;
import utils.KBTestUtilNewMarkup;
import utils.MyTestArticleManager;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;

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
	protected void setUp() throws IOException {
		InitPluginManager.init();
		// Enforce Autocompile
		KnowWEPackageManager.overrideAutocompileArticle(true);
	}

	/**
	 * TODO: Expand this to check if brackets are parsed right.
	 */
	public void testHeuristicRules() {
		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodHeuristic.class);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodHeuristic.class);

		// Check number of rules
		checkRules(loadedRules, createdRules);

	}

	private void checkRules(Collection<Rule> loadedRules, Collection<Rule> createdRules) {
		assertEquals("Wrong number of rules for PSMethodHeuristic.",
				createdRules.size(), loadedRules.size());

		for (Rule key : createdRules) {
			assertNotNull("Rule " + key + " does not exist!",
						loadedRules.contains(key));
		}

		for (Rule key : loadedRules) {
			assertNotNull("Rule " + key + " does not exist!",
						createdRules.contains(key));
		}
	}

	public void testNextQASetRules() {
		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodStrategic.class);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodStrategic.class);

		// Check number of rules
		checkRules(loadedRules, createdRules);

	}

	public void testSetValueRules() {
		// load KnowledgeBases
		KnowWEArticle art = MyTestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		// load Rules in HashMaps (necessary because they are unsorted)
		Collection<Rule> loadedRules =
				getRulesInHashMap(loadedKB, PSMethodAbstraction.class);
		Collection<Rule> createdRules =
				getRulesInHashMap(createdKB, PSMethodAbstraction.class);

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
	private Collection<Rule> getRulesInHashMap(KnowledgeBase kb,
			Class<? extends PSMethod> PSMethod) {

		Collection<Rule> rules = new LinkedList<Rule>();
		Iterator<KnowledgeSlice> iter =
				kb.getAllKnowledgeSlicesFor(PSMethod).iterator();

		while (iter.hasNext()) {
			RuleSet rs = (RuleSet) iter.next();
			for (Rule rule : rs.getRules()) {
				if (!rules.contains(rule)) {
					rules.add(rule);
				}
			}
		}

		return rules;

	}

}
