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

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import dummies.KnowWETestWikiConnector;
import junit.framework.TestCase;

/**
 * Class for testing some functionality of class Section.
 * @author Max Diez
 *
 */
public class SectionTest extends TestCase{
	

	
	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	/**
	 * Test for the methods collectTextsFromLeaves and setOriginalTextSetLeaf
	 * from de.d3web.we.kdom.Section. A small table is changed: A whole row
	 * at once and a row cell by cell
	 */
	public void testSetLeafAndCollect() {
		/**
		 * Initialize KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment env = KnowWEEnvironment.getInstance();
		final String web = env.DEFAULT_WEB;
		
		/**
		 * Build an Article and register it at env.
		 */
		String content = "!!Table\n\n<Table default=\"+,-,0\" width=\"100\" row=\"1\" column=\"1\" cell=\"2,2\">\n| |Apple|Lemon|Coconut\n|sweetness|+|-|hm\n|grows in central europe|+|+|-\n|size|-|+|+\n</Table>";
		env.saveArticle(web, "Test_Article", content, null);
		KnowWEArticle article = env.getArticle(web, "Test_Article");
		
		/**
		 * Beginning the test
		 */
		assertEquals(content, article.collectTextsFromLeaves());
		
		//First some initial sectionizing test
		Section headerSec = checkChildsTillLine(article, 0);
		checkCellContents(headerSec, new String[] {" ", "Apple", "Lemon", "Coconut"});
		Section lineSec = checkChildsTillLine(article, 1);
		checkCellContents(lineSec, new String[] {"sweetness", "+", "-", "hm"});

		
		//Testing setOriginalTextSetLeaf for a node being not a leaf
		String newHeader = "| |Football|Soccer|Rugby\n";
		article.getSection().setOriginalTextSetLeaf(headerSec.getId(), newHeader);
		Section newHeaderSec = checkChildsTillLine(article, 0);
		assertEquals("Childs of node weren't deleted", 0, newHeaderSec.getChildren().size());
		assertEquals("New text wasn't saved to orgingialtext from" + headerSec.getId(),
				newHeader, newHeaderSec.getOriginalText());
		
		article = KnowWEEnvironment.getInstance().getArticle(web, "Test_Article");
		assertEquals(content, article.getSection().getOriginalText());
		assertNotSame(content, article.collectTextsFromLeaves());
		
		
		//Testing setOriginalTextSetLeaf for nodes being a leaf
		String lineText = lineSec.getOriginalText();
		String[] newLine = new String[] {"|speed", "|0", "|+", "|+", "\n"};
		for (int i = 0; i < newLine.length; i++) {
			article.getSection().setOriginalTextSetLeaf(((Section) lineSec.getChildren().get(i)).getId(), newLine[i]);
		}
		assertEquals("OrignialText from parent changed by changing childs (befor save)",
				lineText, lineSec.getOriginalText());
		
		
		//Saving changes to article
		StringBuilder buddy = new StringBuilder();
		article.getSection().collectTextsFromLeaves(buddy);
		assertEquals("Two methdos collectTextsFromLeaves() with different results",
				article.collectTextsFromLeaves(), buddy.toString());
		env.getWikiConnector().saveArticle("Test_Article", buddy.toString(), null, true);
		article = env.getArticle(web, "Test_Article");
		assertNotSame(content, article.getSection().getOriginalText());
		assertTrue(article.getSection().getOriginalText().contains(newHeader));
		
		
		//Sectionizing test for new article
		headerSec = checkChildsTillLine(article, 0);
		checkCellContents(headerSec, new String[] {" ", "Football", "Soccer", "Rugby"});
		lineSec = checkChildsTillLine(article, 1);
		checkCellContents(lineSec, new String[] {"speed", "0", "+", "+"});	
	}
	
	/**
	 * Goes down through the KDOM and checks the right number of children.
	 * @param article Article
	 * @param line number of the table-row which should be returned
	 * @return Section of the 'line'th table-row
	 */
	private Section checkChildsTillLine (KnowWEArticle article, int line) {
		Section actSec = article.getSection();
		assertEquals(actSec + ": ", 1, actSec.getChildren().size());
		//RootType
		actSec = (Section) actSec.getChildren().get(0);
		assertEquals(actSec + ": ",2, actSec.getChildren().size());
		//TableXMLType
		actSec = (Section) actSec.getChildren().get(1);
		assertEquals(actSec + ": ",3, actSec.getChildren().size());
		//XMLContent
		actSec = (Section) actSec.getChildren().get(1);
		//XMLWrappedTable
		actSec = (Section) actSec.getChildren().get(0);
		assertEquals(actSec + ": ",4, actSec.getChildren().size());
		//TableHeaderLine
		actSec = (Section) actSec.getChildren().get(line);
		return actSec;
	}
	
	/**
	 * Checks whether the right values are visible
	 * @param sec The Section of a table row 
	 * @param cellValues Values which should be visible
	 */
	private void checkCellContents(Section sec, String[] cellValues) {
		for (int i = 0; i < cellValues.length; i++) {
			Section actSec = sec;
			
			assertEquals("Node with new content wasn't split up into childs",
					5, actSec.getChildren().size());
			//TableCell #i
			actSec = (Section) actSec.getChildren().get(i);
			assertEquals(actSec + ": ",2, actSec.getChildren().size());
			//TableContent
			actSec = (Section) actSec.getChildren().get(1);
			assertEquals("Actual cell values are wrong: ",
					cellValues[i], actSec.getOriginalText());
		}
	}
	
}
