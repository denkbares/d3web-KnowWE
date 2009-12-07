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

import java.util.Collection;

import utils.KBCreationTestUtil;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.we.logging.Logging;
import junit.framework.TestCase;

/**
 * This class tests whether the XCLModels
 * are created as expected.
 * 
 * @author Sebastian Furth
 * @see KBCreationTestUtil to modify the KB against which everything is tested
 * @see KBCreationTest.txt to modify the Article which is tested
 *
 */
public class XCLTest extends TestCase {
	
	public void testNumberOfXCLModels() {
		// load KnowledgeBases
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		// Check number of rules
		assertEquals("Wrong number of rules for PSMethodXCL.",
				createdKB.getAllKnowledgeSlicesFor(PSMethodXCL.class).size(), 
				loadedKB.getAllKnowledgeSlicesFor(PSMethodXCL.class).size());
	}
	
	
	public void testXCLModels() {
		
		// load KnowledgeBases
		KnowledgeBase loadedKB = KBCreationTestUtil.getInstance().getLoadedKB();
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();
		
		Collection<KnowledgeSlice> loadedXCLModels = loadedKB.getAllKnowledgeSlicesFor(PSMethodXCL.class);
		Collection<KnowledgeSlice> createdXCLModels = createdKB.getAllKnowledgeSlicesFor(PSMethodXCL.class);
		
		XCLModel loadedXCLModel = (XCLModel) loadedXCLModels.toArray()[0];
		XCLModel createdXCLModel = (XCLModel) createdXCLModels.toArray()[0];
		
		// Check whether models exist
		assertNotNull("XCLModel is null.", loadedXCLModel);
		assertNotNull("XCLModel is null.", createdXCLModel);
		
		if (loadedXCLModel != null && createdXCLModel != null) {
			
			assertEquals(createdXCLModel.getSolution(), loadedXCLModel.getSolution());
			
			XCLRelation createdRelation;
			XCLRelation loadedRelation;
			
			// Check Contradicting Relations
			createdRelation = (XCLRelation) createdXCLModel.getContradictingRelations().toArray()[0];
			loadedRelation = (XCLRelation) loadedXCLModel.getContradictingRelations().toArray()[0];
			assertEquals("Contradicting Relation is wrong.", createdRelation.getConditionedFinding(), loadedRelation.getConditionedFinding());
			
			// Check Necessary Relations
			createdRelation = (XCLRelation) createdXCLModel.getNecessaryRelations().toArray()[0];
			loadedRelation = (XCLRelation) loadedXCLModel.getNecessaryRelations().toArray()[0];
			assertEquals("Necessary Relation is wrong.", createdRelation.getConditionedFinding(), loadedRelation.getConditionedFinding());
			
			// Check Sufficient Relations
			createdRelation = (XCLRelation) createdXCLModel.getSufficientRelations().toArray()[0];
			loadedRelation = (XCLRelation) loadedXCLModel.getSufficientRelations().toArray()[0];
			assertEquals("Sufficient Relation is wrong.", createdRelation.getConditionedFinding(), loadedRelation.getConditionedFinding());

			
			// Check "normal" Relation with custom weight
			createdRelation = (XCLRelation) createdXCLModel.getRelations().toArray()[0];
			if (createdRelation.getWeight() <= 1) {
				createdRelation = (XCLRelation) createdXCLModel.getRelations().toArray()[1];
			}
			loadedRelation = (XCLRelation) loadedXCLModel.getRelations().toArray()[0];
			if (loadedRelation.getWeight() <= 1) {
				loadedRelation = (XCLRelation) loadedXCLModel.getRelations().toArray()[1];
			}
			assertEquals("Relation is wrong.", createdRelation.getConditionedFinding(), loadedRelation.getConditionedFinding());
			assertEquals("Relation has wrong weight.", createdRelation.getWeight(), loadedRelation.getWeight());
			
			
			// Check "normal" Relation with default weight (1)
			createdRelation = (XCLRelation) createdXCLModel.getRelations().toArray()[1];
			if (createdRelation.getWeight() > 1) {
				createdRelation = (XCLRelation) createdXCLModel.getRelations().toArray()[0];
			}
			loadedRelation = (XCLRelation) loadedXCLModel.getRelations().toArray()[1];
			if (loadedRelation.getWeight() > 1) {
				loadedRelation = (XCLRelation) loadedXCLModel.getRelations().toArray()[0];
			}
			assertEquals("Relation is wrong.", createdRelation.getConditionedFinding(), loadedRelation.getConditionedFinding());
			assertEquals("Relation has wrong weight.", createdRelation.getWeight(), loadedRelation.getWeight());
		} else {
			Logging.getInstance().getLogger().warning("XCLTest: XCLModel has not been tested!");
		}
	}

}
