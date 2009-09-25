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


import java.util.ArrayList;

import objectTypes.SplitObjectType;
import objectTypes.WordObjectType;


import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import dummies.KnowWETestWikiConnector;
import junit.framework.TestCase;

/**
 * Sectionizer Test Class.
 * 
 * @author Johannes Dienst
 *
 */
public class SectionizerTest extends TestCase {

	public void testKdom() {
		
		/**
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().getArticle("default_web", "Test_Article");
		
		/**
		 * Build an Article.
		 */
		String content = "aaa bbb -ababba- aba - bbbaa-abba aab";
		ArrayList<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new SplitObjectType());
		types.add(new WordObjectType());
		
		KnowWEArticle article = new KnowWEArticle(content, "Test_Article", types, "default_web");
		
		/**
		 * The real tests
		 * 1. Check some children-Counts
		 */
		int expected = 12;
		Section root = article.getSection();
		assertEquals(expected , root.getChildren().size());
		
		ArrayList<Section> children = new ArrayList<Section>(root.getChildren());
		
		expected = 1;
		Section child = children.get(0);
		assertEquals("Wrong children count", expected, child.getChildren().size());
		
		expected = 3;
		Section child2 =child.getChildren().get(0);
		assertEquals("Wrong children count",expected, child2.getChildren().size());		
		
		child = children.get(2);
		expected = 1;
		assertEquals("Wrong children count", expected, child.getChildren().size());
		
		expected = 3;
		child2 = child.getChildren().get(0);
		assertEquals("Wrong children count", expected, child2.getChildren().size());		
		
		expected = 7;
		child = children.get(4);
		assertEquals("Wrong children count", expected, child.getChildren().size());
		
		/**
		 * 2. Test content of kdom-Nodes.
		 */
		// WordordObjectType Content deep
		child = children.get(0);
		String cont = child.getOriginalText();
		assertEquals("WordObjectType expected", "aaa", cont);
		child = child.getChildren().get(0);
		cont = child.getOriginalText();
		assertEquals("AStarObjectType expected", "aaa", cont);
		child = child.getChildren().get(1);
		cont = child.getOriginalText();
		assertEquals("AObjectType expected", "a", cont);
		
		// PlainText
		child = children.get(1);
		cont = child.getOriginalText();
		assertEquals("Expected PlainText", " ", cont);
		
		// WordObjectType lazy
		child = children.get(2);
		cont = child.getOriginalText();
		assertEquals("WordObjectType expected", "bbb", cont);
		
		// PlainText
		child = children.get(3);
		cont = child.getOriginalText();
		assertEquals("Expected PlainText", " ", cont);
		
		// SplitObjectType deep
		child = children.get(4);
		cont = child.getOriginalText();
		assertEquals("Expected SplitObjectType", "-ababba-", cont);
		ArrayList<Section> childs = new ArrayList<Section>(child.getChildren());
		cont = childs.get(0).getOriginalText();
		assertEquals("Error in SplitObjectType", "-", cont);
		cont = childs.get(1).getOriginalText();
		assertEquals("Error in SplitObjectType", "a", cont);
		cont = childs.get(2).getOriginalText();
		assertEquals("Error in SplitObjectType", "b", cont);
		cont = childs.get(3).getOriginalText();
		assertEquals("Error in SplitObjectType", "a", cont);
		cont = childs.get(4).getOriginalText();
		assertEquals("Error in SplitObjectType", "bb", cont);
		cont = childs.get(5).getOriginalText();
		assertEquals("Error in SplitObjectType", "a", cont);
		cont = childs.get(6).getOriginalText();
		assertEquals("Error in SplitObjectType", "-", cont);
		
		/**
		 * Test Some Object-Types
		 */
//		// PlainText
//		assertEquals(WordObjectType.getInstance(), children.get(0));
//		Section typeSec = children.get(1);
//		assertEquals(PlainText.getInstance() ,typeSec.getObjectType());
//		
//		typeSec = children.get(3);
//		assertEquals(PlainText.getInstance() ,typeSec.getObjectType());
//		
//		// WordObjectType with AStarChildren
//		typeSec = children.get(0);
//		assertEquals(WordObjectType.getInstance() ,typeSec.getObjectType());
//		
//		typeSec = typeSec.getChildren().get(0);
//		assertEquals(AStarObjectType.getInstance() ,typeSec.getObjectType());
//		
//		typeSec = typeSec.getChildren().get(0);
//		assertEquals(AObjectType.getInstance() ,typeSec.getObjectType());
//		
//		// WOT with BStarChildren
//		typeSec = children.get(2).getChildren().get(0);
//		assertEquals(BStarObjectType.getInstance() ,typeSec.getObjectType());
//		
//		typeSec = typeSec.getChildren().get(0);
//		assertEquals(AObjectType.getInstance() ,typeSec.getObjectType());
//		
//		// SplitObjectType
//		typeSec = children.get(4);
//		assertEquals(SplitObjectType.getInstance(), typeSec.getObjectType());
//		
//		ArrayList<Section> splitChildren = new ArrayList<Section>(typeSec.getChildren());
//		child = splitChildren.get(0);		
	}
}
