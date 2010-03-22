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
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.logging.Logging;
import junit.framework.TestCase;

/**
 * This class tests whether the Diagnoses
 * are created as expected.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 *
 */
public class DiagnosesTest extends TestCase {
	
	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	public void testNumberOfDiagnoses() {
		
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		assertEquals("Number of Diagnoses differ.", createdKB.getDiagnoses().size(), loadedKB.getDiagnoses().size());
	}
	
	public void testDiagnoses() {
		
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		if (loadedKB.getDiagnoses().size() == createdKB.getDiagnoses().size()) {
			for (int i=0; i<loadedKB.getDiagnoses().size(); i++) {
				
				Diagnosis expected = createdKB.getDiagnoses().get(i);
				Diagnosis actual = loadedKB.getDiagnoses().get(i);
				
				// Test ID & Name
				assertEquals("Diagnosis " + expected.getName() + " has wrong ID.", expected.getId(), actual.getId());
				assertEquals("Diagnosis " + expected.getName() + " has wrong name.", expected.getName(), actual.getName());
				
				// Test Hierarchy
				assertEquals("Diagnosis " + expected.getName() + " has wrong parents.", expected.getParents(), actual.getParents());
				assertEquals("Diagnosis " + expected.getName() + " has wrong children.", expected.getChildren(), actual.getChildren());
				
				// Test Explanation
				assertEquals("Diagnosis " + expected.getName() + " has wrong explanation.", expected.getProperties().getProperty(Property.EXPLANATION), actual.getProperties().getProperty(Property.EXPLANATION));
			}
		} else {
			Logging.getInstance().getLogger().warning("DiagnosesTest: Diagnoses have not been tested!");
		}
	}	
}
