package tests;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import utils.KBTestUtilNewMarkup;
import utils.TestArticleManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.utils.Log;
import de.knowwe.core.kdom.Article;

/**
 * This class tests whether the Questions are created as expected.
 * 
 * 
 * @author Sebastian Furth
 * @see KBTestUtilNewMarkup to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class QuestionTreeTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	public void testNumberOfQuestions() {
		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();
		assertEquals("Number of Questions differ.", createdKB.getManager().getQuestions().size(),
				loadedKB.getManager().getQuestions().size());
	}

	public void testQuestions() {

		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		if (loadedKB.getManager().getQuestions().size() == createdKB.getManager().getQuestions().size()) {
			for (int i = 0; i < loadedKB.getManager().getQuestions().size(); i++) {

				Question expected = createdKB.getManager().getQuestions().get(i);
				// search the right question in loadedKB
				Question actual = null;
				for (Question q : loadedKB.getManager().getQuestions()) {
					if (q.getName().equals(expected.getName())) {
						actual = q;
					}
				}

				// Test Name
				assertEquals("Question " + expected.getName() +
						" has wrong name.",
						expected.getName(), actual.getName());

				// Test Hierarchy: Parents
				// for-loop for this because ID isnt relevant any more
				List<String> expectedList = new ArrayList<>();
				for (TerminologyObject obj : expected.getParents()) {
					expectedList.add(obj.getName());
				}
				List<String> actualList = new ArrayList<>();
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
				expectedList = new ArrayList<>();
				for (TerminologyObject obj : expected.getChildren()) {
					expectedList.add(obj.getName());
				}
				actualList = new ArrayList<>();
				for (TerminologyObject obj : actual.getChildren()) {
					actualList.add(obj.getName());
				}

				assertEquals("Question " + expected.getName() +
						" has wrong number of children.",
						expectedList.size(), actualList.size());

				// Driving should have "Other
				// as children and not null
				for (String t : expectedList) {
					assertTrue("Question " + expected.getName() +
							" has wrong children.",
							actualList.contains(t));
				}

				// Test Properties (Abstraction, MMINFO)
				assertEquals("Question " + expected.getName() +
						" should be abstract.",
						expected.getInfoStore().getValue(BasicProperties.ABSTRACTION_QUESTION),
						actual.getInfoStore().getValue(BasicProperties.ABSTRACTION_QUESTION));

				// Test Question Type
				assertEquals("Question " + expected.getName() +
						" has wrong type.",
						expected.getClass(), actual.getClass());

				// Question Type specific tests
				if (expected instanceof QuestionChoice) {
					expectedList = new ArrayList<>();
					for (Choice obj : ((QuestionChoice) expected).getAllAlternatives()) {
						expectedList.add(obj.getName());
					}
					actualList = new ArrayList<>();
					for (Choice obj : ((QuestionChoice) actual).getAllAlternatives()) {
						actualList.add(obj.getName());
					}

					assertEquals("Question " + expected.getName() +
							" has wrong number of answer alternatives.",
							expectedList.size(), actualList.size());

					for (String t : expectedList) {
						assertTrue("Question " + expected.getName()
								+ " has different answer alternatives.",
								actualList.contains(t));
					}

				}

				if (expected instanceof QuestionNum) {
					assertEquals("Question " + expected.getName() +
							" has wrong unit.",
							expected.getInfoStore().getValue(MMInfo.UNIT),
							actual.getInfoStore().getValue(MMInfo.UNIT));
					assertEquals("Question " + expected.getName() +
							" has wrong range.",
							expected.getInfoStore().getValue(BasicProperties.QUESTION_NUM_RANGE),
							actual.getInfoStore().getValue(BasicProperties.QUESTION_NUM_RANGE));
				}

			}
		}
		else {
			Log.warning("QuestionTest: Questions have not been tested!");
		}
	}

	public void testMMInfo() {

		Article art = TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = KBTestUtilNewMarkup.getInstance().getKnowledgeBase(art);
		KnowledgeBase createdKB = KBTestUtilNewMarkup.getInstance().getCreatedKB();

		// Get Question with ID "Q1": "Exhaust fumes"
		Question loadedQuestion = loadedKB.getManager().searchQuestion("Exhaust fumes");
		Question createdQuestion = createdKB.getManager().searchQuestion("Exhaust fumes");

		String loadedPrompt = loadedQuestion.getInfoStore().getValue(MMInfo.PROMPT);
		String createdPrompt = createdQuestion.getInfoStore().getValue(MMInfo.PROMPT);

		// Get MMInfoObject for created DCMarkup
		assertNotNull("Question " + loadedQuestion.getName() + " has no MMInfo.", loadedPrompt);
		assertNotNull("Question " + createdQuestion.getName() + " has no MMInfo.", createdPrompt);

		// Compare content of MMInfoObject
		assertEquals("Content of MMInfoObject of Diagnosis " + createdQuestion.getName()
				+ " differs.", loadedPrompt, createdPrompt);

	}

}
