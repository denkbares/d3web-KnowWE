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

import junit.framework.TestCase;
import utils.KBCreationTestUtil;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.logging.Logging;

/**
 * This class tests whether the Questionnaires
 * are created as expected.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 */
public class QuestionnaireTest extends TestCase {
	
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	public void testNumberOfQuestionnaires() {
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		assertEquals("Number of Qestionnaires differ.", createdKB.getQContainers().size(), loadedKB.getQContainers().size());
	}
	
	public void testQuestionnaires() {
		
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		if (loadedKB.getQContainers().size() == createdKB.getQContainers().size()) {
			for (int i=0; i<loadedKB.getQContainers().size(); i++) {
				
				QContainer expected = createdKB.getQContainers().get(i);
				QContainer actual = loadedKB.getQContainers().get(i);
				
				// Test Name & ID
				assertEquals("QContainer " + expected.getText() + " has wrong ID.", expected.getId(), actual.getId());
				assertEquals("QContainer " + expected.getText() + " has wrong name.", expected.getText(), actual.getText());
				
				// Test Hierarchy
				assertEquals("QContainer " + expected.getText() + " has wrong parents.", expected.getParents(), actual.getParents());
				assertEquals("QContainer " + expected.getText() + " has wrong children.", expected.getChildren(), actual.getChildren());
				
				// Test Explanation
				assertEquals("QContainer " + expected.getText() + " has wrong explanation.", expected.getProperties().getProperty(Property.EXPLANATION), actual.getProperties().getProperty(Property.EXPLANATION));
			}
		} else {
			Logging.getInstance().getLogger().warning("QuestionnaireTest: Questionnaires have not been tested!");
		}
		
		assertEquals("Init Questions differ.", createdKB.getInitQuestions(), loadedKB.getInitQuestions());
	}
	
	
}
