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
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.Article;

/**
 * This class tests whether the Diagnoses are created as expected.
 * 
 * 
 * @author Sebastian Furth
 * @see KBTestUtilNewMarkup to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class SolutionsTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	public void testNumberOfSolutions() {

		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		assertEquals("Number of Solutions differ.", createdKB.getManager().getSolutions().size(),
				loadedKB.getManager().getSolutions().size());
	}

	public void testSolutions() {

		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		if (loadedKB.getManager().getSolutions().size() == createdKB.getManager().getSolutions().size()) {
			for (int i = 0; i < loadedKB.getManager().getSolutions().size(); i++) {

				Solution expected = createdKB.getManager().getSolutions().get(i);

				// Search right solution in KB
				Solution actual = null;
				for (Solution s : loadedKB.getManager().getSolutions()) {
					if (s.getName().equals(expected.getName())) {
						actual = s;
					}
				}

				// HOTFIX for testing: Johannes
				if (actual == null) continue;

				// Test Name(is ID)
				assertEquals("Solution " + expected.getName() +
						" has wrong name.",
						expected.getName(), actual.getName());

				// Test Hierarchy
				// for-loop for this because id is not relevant any more
				List<String> expectedList = new ArrayList<String>();
				for (TerminologyObject obj : expected.getParents()) {
					expectedList.add(obj.getName());
				}
				List<String> actualList = new ArrayList<String>();
				for (TerminologyObject obj : actual.getParents()) {
					actualList.add(obj.getName());
				}

				assertEquals("Question " + expected.getName() +
						" has wrong number of parents.",
						expectedList.size(), actualList.size());
				for (String t : expectedList) {
					assertTrue("Question " + expected.getName() +
							" has wrong parents.",
							actualList.contains(t));
				}

				// Test Hierarchy: Test children
				expectedList = new ArrayList<String>();
				for (TerminologyObject obj : expected.getChildren()) {
					expectedList.add(obj.getName());
				}
				actualList = new ArrayList<String>();
				for (TerminologyObject obj : actual.getChildren()) {
					actualList.add(obj.getName());
				}

				assertEquals("Question " + expected.getName() +
						" has wrong number of children.",
						expectedList.size(), actualList.size());

				for (String t : expectedList) {
					assertTrue("Question " + expected.getName() +
							" has wrong children.",
							actualList.contains(t));
				}

				// Test Explanation
				assertEquals("Solution " + expected.getName() +
						" has wrong explanation.",
						expected.getInfoStore().getValue(MMInfo.DESCRIPTION),
						actual.getInfoStore().getValue(MMInfo.DESCRIPTION));
			}
		}
		else {
			Log.warning("SolutionsTest: Solutions have not been tested!");
		}
	}
}
