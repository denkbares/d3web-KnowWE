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

import java.io.IOException;

import utils.KBCreationTestUtil;
import de.d3web.core.KnowledgeBase;
import de.d3web.core.terminology.Question;
import de.d3web.core.terminology.QuestionChoice;
import de.d3web.core.terminology.QuestionNum;
import de.d3web.core.terminology.info.DCElement;
import de.d3web.core.terminology.info.DCMarkup;
import de.d3web.core.terminology.info.MMInfoObject;
import de.d3web.core.terminology.info.MMInfoStorage;
import de.d3web.core.terminology.info.MMInfoSubject;
import de.d3web.core.terminology.info.Property;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.logging.Logging;
import junit.framework.TestCase;

/**
 * This class tests whether the Questions
 * are created as expected.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 * 
 */
public class QuestionTreeTest extends TestCase {
	
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	public void testNumberOfQuestions() {
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		assertEquals("Number of Questions differ.", createdKB.getQuestions().size(), loadedKB.getQuestions().size());
	}
	
	public void testQuestions() {
		
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		if (loadedKB.getQuestions().size() == createdKB.getQuestions().size()) {
			for (int i=0; i<loadedKB.getQuestions().size(); i++) {
				
				Question expected = createdKB.getQuestions().get(i);
				Question actual = loadedKB.getQuestions().get(i);
				
				// Test Name & ID
				assertEquals("Question " + expected.getText() + " has wrong ID.",
						expected.getId(), actual.getId());
				assertEquals("Question " + expected.getText() + " has wrong name.",
						expected.getText(), actual.getText());
				
				// Test Hierarchy
				assertEquals("Question " + expected.getText() + " has wrong parents.",
						expected.getParents(), actual.getParents());
				assertEquals("Question " + expected.getText() + " has wrong children.",
						expected.getChildren(), actual.getChildren());
				
				// Test Properties (Abstraction, MMINFO)
				assertEquals("Question " + expected.getText() + " should be abstract.",
						expected.getProperties().getProperty(Property.ABSTRACTION_QUESTION),
						actual.getProperties().getProperty(Property.ABSTRACTION_QUESTION));
				
				// Test Question Type
				assertEquals("Question " + expected.getText() + " has wrong type.",
						expected.getClass(), actual.getClass());
				
				// Question Type specific tests
				if (expected instanceof QuestionChoice) {
					assertEquals("Question " + expected.getText() + " has different answer alternatives.", 
							((QuestionChoice)expected).getAllAlternatives(), 
							((QuestionChoice)actual).getAllAlternatives());
				}
				
				if (expected instanceof QuestionNum) {
					assertEquals("Question " + expected.getText() + " has wrong unit.", 
							expected.getProperties().getProperty(Property.UNIT), 
							actual.getProperties().getProperty(Property.UNIT));
					assertEquals("Question " + expected.getText() + " has wrong range.", 
							expected.getProperties().getProperty(Property.QUESTION_NUM_RANGE), 
							actual.getProperties().getProperty(Property.QUESTION_NUM_RANGE));
				}
				
			}
		} else {
			Logging.getInstance().getLogger().warning("QuestionTest: Questions have not been tested!");
		}
	}
	
	public void testMMInfo() {
		
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		// Get Question with ID "Q1": "Exhaust fumes"
		Question loadedQuestion = loadedKB.searchQuestion("Q1");
		Question createdQuestion = createdKB.searchQuestion("Q1");
		
		// Get MMInfoStorage of question
		MMInfoStorage loadedStorage = (MMInfoStorage) loadedQuestion.getProperties().getProperty(Property.MMINFO);
		MMInfoStorage createdStorage = (MMInfoStorage) createdQuestion.getProperties().getProperty(Property.MMINFO); 
		assertNotNull("Question " + loadedQuestion.getText() + " has no MMInfoStorage.", loadedStorage);
		assertNotNull("Question " + createdQuestion.getText() + " has no MMInfoStorage.", createdStorage);
		
		// Create DCMarkup
		DCMarkup markup = new DCMarkup();
		markup.setContent(DCElement.SOURCE, loadedQuestion.getId());
		markup.setContent(DCElement.TITLE, "LT");
		markup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());

		// Get MMInfoObject for created DCMarkup
		MMInfoObject loadedMMInfo = (MMInfoObject) loadedStorage.getMMInfo(markup).toArray()[0];
		MMInfoObject createdMMInfo = (MMInfoObject) createdStorage.getMMInfo(markup).toArray()[0];
		assertNotNull("Question " + loadedQuestion.getText() + " has no MMInfo.", loadedMMInfo);
		assertNotNull("Question " + createdQuestion.getText() + " has no MMInfo.", createdMMInfo);
		
		// Compare content of MMInfoObject
		assertEquals("Content of MMInfoObject of Diagnosis " + createdQuestion.getText() + " differs.", 
					  createdMMInfo.getContent(), loadedMMInfo.getContent());
		
	}
	
		
}
