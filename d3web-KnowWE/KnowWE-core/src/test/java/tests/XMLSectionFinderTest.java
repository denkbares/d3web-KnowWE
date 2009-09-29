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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import utils.Utils;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.xml.GenericXMLObjectType;
import de.d3web.we.kdom.xml.XMLSectionFinder;
import dummies.KnowWETestWikiConnector;
import junit.framework.TestCase;

public class XMLSectionFinderTest extends TestCase {

	public void testXMLSectionFinder() {
		
		/**
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().getArticle("default_web", "Test_Article");
		
		/**
		 * Setup
		 */
		String content = this.readXMLFile("2");
		
		ArrayList<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		KnowWEArticle article = new KnowWEArticle(content, "Test_Article",
				types, "default_web");
		Section artSec = article.getSection();
		
		/**
		 * The Tests.
		 */
		XMLSectionFinder f;
		List<SectionFinderResult> findings;
		int start;
		int end;
		
		// Test 1
		f = new XMLSectionFinder("Start");
		findings = f.lookForSections(content, artSec);
		start = 0;
		end = 265;
		assertEquals("Element <Start> begin index wrong", start, findings.get(0).getStart());
		assertEquals("Element <Start> end index wrong", end, findings.get(0).getEnd());
		
		// Test2
		f = new XMLSectionFinder("SubSection");
		findings = f.lookForSections(content, artSec);
		start = 10;
		end = 199;
		assertEquals("Element <SubSection> begin index wrong", start, findings.get(0).getStart());
		assertEquals("Element <SubSection> end index wrong", end, findings.get(0).getEnd());
		
		// Test3
		f = new XMLSectionFinder("SubSubSection");
		findings = f.lookForSections(content, artSec);
		start = 64;
		end = 103;
		assertEquals("Element <SubSubSection> begin index wrong",start, findings.get(0).getStart());
		assertEquals("Element <SubSubSection> end index wrong", end, findings.get(0).getEnd());
		start = 105;
		end = 183;
		assertEquals("Element <SubSubSection> begin index wrong",start, findings.get(1).getStart());
		assertEquals("Element <SubSubSection> end index wrong", end, findings.get(1).getEnd());
		
		// Test4
		f = new XMLSectionFinder("Text2");
		findings = f.lookForSections(content, artSec);
		start = 126;
		end = 162;
		assertEquals("Element <Text2> begin index wrong", start, findings.get(0).getStart());
		assertEquals("Element <Text2> end index wrong", end, findings.get(0).getEnd());
		start = 200;
		end = 236;
		assertEquals("Element <Text2> begin index wrong", start, findings.get(1).getStart());
		assertEquals("Element <Text2> end index wrong", end, findings.get(1).getEnd());
		
		/**
		 * Tests for Generic XMLSectionFinder
		 */
		f = new XMLSectionFinder();
		findings = f.lookForSections(content, artSec);
		start = 0;
		end = 265;
		assertEquals("Generic SectionFinder failed", start, findings.get(0).getStart());
		assertEquals("Generic SectionFinder failed", end, findings.get(0).getEnd());
		
		/**
		 * Build a complete Article using GenericXMLObjectType 
		 */
		content = this.readXMLFile("0");
		types = new ArrayList<KnowWEObjectType>();
		types.add(new GenericXMLObjectType());
		article = new KnowWEArticle(content, "Test_Article2", types, "default_web");
		artSec = article.getSection();

		// Test children counts
		int expected = 3;
		Section artChild = artSec.getChildren().get(0);
		assertEquals("ArticleSection: Childcount wrong", expected, artChild.getChildren().size());
		
		artChild = artChild.getChildren().get(1);
		expected = 2;
		assertEquals("Wrong subtree count",expected, artChild.getChildren().size());
		
		// Test left subtree
		Section subRoot = artChild.getChildren().get(0);
		expected = 3;
		assertEquals("Error in Left subtree:", expected, subRoot.getChildren().size());
		
		subRoot = subRoot.getChildren().get(1);
		expected = 3;
		assertEquals("Error in Left subtree:", expected, subRoot.getChildren().size());
		
		expected = 3;
		assertEquals("Error in Left subtree:", expected, subRoot.getChildren().get(0).getChildren().size());
		
		expected = 3;
		assertEquals("Error in Left subtree:", expected, subRoot.getChildren().get(1).getChildren().size());
		
		expected = 3;
		subRoot = subRoot.getChildren().get(2);
		subRoot = subRoot.getChildren().get(1);
		subRoot = subRoot.getChildren().get(0);
		assertEquals("Error in Left subtree:", expected, subRoot.getChildren().size());
		
		// Test right subtree
		subRoot = artChild.getChildren().get(1);
		expected = 3;
		assertEquals("Error in right subtree", expected, subRoot.getChildren().size());
		
	}
	
	/**
	 * Reads the xml-date from the test-File.
	 * 
	 * @return
	 */
	private String readXMLFile(String number) {
		File f = new File(
				"src/test/resources/testXML" +number + ".txt");
		FileInputStream s;
		try {
			s = new FileInputStream(f);

			BufferedReader r = new BufferedReader(new InputStreamReader(s));
			String st = Utils.readBytes(r);
			s.close();
			r.close();
			return st;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
