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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.tcas.Annotation;

import utils.Utils;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.knowwetypes.BuildingType;
import de.d3web.we.knowwetypes.RoomNumberType;
import de.d3web.we.logging.Logging;
import de.d3web.we.uimaconnector.UIMAConnector;
import de.d3web.we.utils.AnnotationStore;
import de.d3web.we.utils.KnowWEUtils;
import dummies.KnowWETestWikiConnector;

/**
 * This is a complete test.
 * It runs a sequence of steps:
 * - load analysis Engine
 * - analyse text with it
 * - build a KDOM from it
 * 
 * TODO: IntervalCalculator Test seperately??
 * 
 * @author Johannes Dienst
 *
 */
public class UIMAConnectorCompleteTest extends TestCase {
	
	private final static String WRONG_TYPE = "Wrong Type";
	private final static String WRONG_FEATURE = "Wrong Feature";
	
	private final static String WRONG_FIRST_START = "Wrong start at first finding";
	private final static String WRONG_FIRST_END = "Wrong end at first finding";
	private final static String WRONG_SECOND_START = "Wrong start at second finding";
	private final static String WRONG_SECOND_END = "Wrong end at second finding";
	private final static String WRONG_THIRD_START = "Wrong start at third finding";
	private final static String WRONG_THIRD_END = "Wrong end at third finding";
	private final static String WRONG_FOURTH_START = "Wrong start at fourth finding";
	private final static String WRONG_FOURTH_END = "Wrong end at fourth finding";
	private KnowWEArticleManager am;
	private KnowWEEnvironment ke;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		InitPluginManager.init();
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke = KnowWEEnvironment.getInstance();
		am = ke.getArticleManager("default_web");

	}

	public void testUIMAConnector() {
		String content = Utils.readTxtFile("src/test/resources/test");;
		DefaultAbstractKnowWEObjectType rootType = new DefaultAbstractKnowWEObjectType(new AllTextSectionFinder()) {			
			{
				addChildType(new RoomNumberType());
			}
		};
		
		this.initialiseCAS(content);
		KnowWEArticle test = ke.getArticle("default_web", "Test_Article");
		ke.processAndUpdateArticleJunit("TestUser", content, "Test_Article", "default_web", rootType);
//		KnowWEArticle article = new KnowWEArticle(content, "Test_Article", rootType, "default_web");
		Section articleSec = ke.getArticle("default_web", "Test_Article").getSection();
		List<Section> children = articleSec.getChildren();
		children = children.get(0).getChildren();
		
		// RoomNumbers
		assertEquals(WRONG_TYPE, true, (children.get(1).getObjectType() instanceof RoomNumberType));
		assertEquals(WRONG_TYPE, true, (children.get(3).getObjectType() instanceof RoomNumberType));
		assertEquals(WRONG_TYPE, true, (children.get(5).getObjectType() instanceof RoomNumberType));
		
		// Nested Type Building
		Section building = children.get(1).getChildSectionAtPosition(0);
		assertEquals(WRONG_TYPE, true, (building.getObjectType() instanceof BuildingType));
		building = children.get(3).getChildSectionAtPosition(0);
		assertEquals(WRONG_TYPE, true, (building.getObjectType() instanceof BuildingType));
		building = children.get(5).getChildSectionAtPosition(0);
		assertEquals(WRONG_TYPE, true, (building.getObjectType() instanceof BuildingType));
		
		// Feature 1
		Section child = children.get(1);
		HashMap<String, Object> map =
			(HashMap<String, Object>)KnowWEUtils.getStoredObject(child, child.getID());
		FeatureStructure feature = (FeatureStructure)map.get("building");
		Annotation feat = (Annotation) feature;
		String coveredText = feat.getCoveredText();
		assertNotNull(feature);
		assertEquals(WRONG_FEATURE, "GN-K35", coveredText);
		
		// Feature 2
		child = children.get(3);
		map = (HashMap<String, Object>)KnowWEUtils.getStoredObject(child, child.getID());
		feature = (FeatureStructure)map.get("building");
		feat = (Annotation) feature;
		coveredText = feat.getCoveredText();
		assertNotNull(feature);
		assertEquals(WRONG_FEATURE, "GN-K35", coveredText);
		
		// Feature 3
		child = children.get(5);
		map = (HashMap<String, Object>)KnowWEUtils.getStoredObject(child, child.getID());
		feature = (FeatureStructure)map.get("building");
		feat = (Annotation) feature;
		coveredText = feat.getCoveredText();
		assertNotNull(feature);
		assertEquals(WRONG_FEATURE, "GN-K35", coveredText);
	}
	
	private void initialiseCAS(String content) {
		try {

			// Reset the AnnotationStore: Because so it will not only
			// function once
			AnnotationStore.getInstance().reset();
			AnalysisEngine ae = UIMAConnector.getInstance().getAnalysisEngine(
					new File("src/test/resources/RoomNumberAnnotator.xml"));
			CAS cas = ae.newCAS();
			cas.setDocumentText(content);
			ae.process(cas);
			UIMAConnector.getInstance().setActualCAS(cas);

		} catch (Exception e) {
			Logging.getInstance().log(Level.FINER,
					"Something went wrong in UIMA-Processing");
		}
	}
}
