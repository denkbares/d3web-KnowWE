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
import java.util.List;

import junit.framework.TestCase;
import utils.KBTestUtilNewMarkup;
import utils.TestArticleManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.Article;

/**
 * This class tests whether the Questionnaires are created as expected.
 * 
 * @author Sebastian Furth
 * @see KBTestUtilNewMarkup to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 */
public class QuestionnaireTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	public void testNumberOfQuestionnaires() {
		Article art = TestArticleManager.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();
		assertEquals("Number of Qestionnaires differ.",
				createdKB.getManager().getQContainers().size(),
				loadedKB.getManager().getQContainers().size());
	}

	public void testQuestionnaires() {

		Article art = TestArticleManager.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		if (loadedKB.getManager().getQContainers().size() == createdKB.getManager().getQContainers().size()) {
			for (int i = 0; i < loadedKB.getManager().getQContainers().size(); i++) {

				QContainer expected = createdKB.getManager().getQContainers().get(i);
				QContainer actual = loadedKB.getManager().getQContainers().get(i);

				// Test Name & ID(ID is outdated. But it does not make problems
				// here)
				// So I let it in. Johannes
				assertEquals("QContainer " + expected.getName() + " has wrong ID.",
						expected.getName(), actual.getName());
				assertEquals("QContainer " + expected.getName() + " has wrong name.",
						expected.getName(), actual.getName());

				// Test Hierarchy
				List<String> expectedList = new ArrayList<String>();
				for (TerminologyObject obj : expected.getParents()) {
					expectedList.add(obj.getName());
				}
				List<String> actualList = new ArrayList<String>();
				for (TerminologyObject obj : actual.getParents()) {
					actualList.add(obj.getName());
				}

				assertEquals("QContainer " + expected.getName() +
						" has wrong number of parents.",
						expectedList.size(), actualList.size());

				// Parents
				for (String t : expectedList) {
					assertTrue("QContainer " + expected.getName() +
							" has wrong parents.",
							actualList.contains(t));
				}

				// Children
				expectedList = new ArrayList<String>();
				for (TerminologyObject obj : expected.getChildren()) {
					expectedList.add(obj.getName());
				}
				actualList = new ArrayList<String>();
				for (TerminologyObject obj : actual.getChildren()) {
					actualList.add(obj.getName());
				}

				assertEquals("QContainer " + expected.getName() +
						" has wrong number of children.",
						expectedList.size(), actualList.size());

				for (String t : expectedList) {
					assertTrue("QContainer " + expected.getName() +
							" has wrong children.",
							actualList.contains(t));
				}

				// Test Explanation
				assertEquals("QContainer " + expected.getName() + " has wrong explanation.",
						expected.getInfoStore().getValue(MMInfo.PROMPT),
						actual.getInfoStore().getValue(MMInfo.PROMPT));
			}
		}
		else {
			Log.warning("QuestionnaireTest: Questionnaires have not been tested!");
		}

		assertEquals("Init Questions differ.", createdKB.getInitQuestions(),
				loadedKB.getInitQuestions());
	}

}
