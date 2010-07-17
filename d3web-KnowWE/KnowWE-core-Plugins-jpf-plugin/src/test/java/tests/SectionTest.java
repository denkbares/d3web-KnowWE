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
		Section headerSec = checkChildsTillLine(article, 0, false);
		checkCellContents(headerSec, new String[] {" ", "Apple", "Lemon", "Coconut"});
		Section lineSec = checkChildsTillLine(article, 1, false);
		checkCellContents(lineSec, new String[] {"sweetness", "+", "-", "hm"});

		
		//Testing setOriginalTextSetLeaf for a node being not a leaf
		String newHeader = "| |Football|Soccer|Rugby\n";
		article.getSection().setOriginalTextSetLeaf(headerSec.getID(), newHeader);
		Section newHeaderSec = checkChildsTillLine(article, 0, false);
		assertEquals("Childs of node weren't deleted", 0, newHeaderSec.getChildren().size());
		assertEquals("New text wasn't saved to orgingialtext from" + headerSec.getID(),
				newHeader, newHeaderSec.getOriginalText());
		
		article = KnowWEEnvironment.getInstance().getArticle(web, "Test_Article");
		assertEquals(content, article.getSection().getOriginalText());
		assertNotSame(content, article.collectTextsFromLeaves());
		checkChildsTillLine(article, 0, true); //isDirty = true?!
		
		
		//not being leafs (go down once more)
		//Testing setOriginalTextSetLeaf for nodes being a leaf
		String lineText = lineSec.getOriginalText();
		String[] newLine = new String[] {"speed", "0", "+", "+"};
		for (int i = 0; i < newLine.length; i++) {
			article.getSection().setOriginalTextSetLeaf(((Section)((Section) lineSec.getChildren().get(i)).getChildren().get(1)).getID(), newLine[i]);
		}
		article.getSection().setOriginalTextSetLeaf(((Section) lineSec.getChildren().get(newLine.length)).getID(), "\n");
		assertEquals("OrignialText from parent changed by changing childs (befor save)",
				lineText, lineSec.getOriginalText());
		checkChildsTillLine(article, 1, true); //isDirty = true?!
		
		
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
		headerSec = checkChildsTillLine(article, 0, false);
		checkCellContents(headerSec, new String[] {" ", "Football", "Soccer", "Rugby"});
		lineSec = checkChildsTillLine(article, 1, false);
		checkCellContents(lineSec, new String[] {"speed", "0", "+", "+"});	
	}
	
	/**
	 * Goes down through the KDOM and checks the right number of children.
	 * @param article Article
	 * @param line number of the table-row which should be returned
	 * @param dirtyCheck TODO
	 * @return Section of the 'line'th table-row
	 */
	private Section checkChildsTillLine (KnowWEArticle article, int line, boolean dirtyCheck) {
		Section actSec = article.getSection();
		assertEquals(actSec + ": ", 1, actSec.getChildren().size());
		//RootType
		actSec = getChild(actSec, 0, 2, dirtyCheck);
		//TableXMLType
		actSec = getChild(actSec, 1, 3, dirtyCheck);
		//XMLContent
		actSec = getChild(actSec, 1, 1, dirtyCheck);
		//XMLWrappedTable
		actSec = getChild(actSec, 0, 4, dirtyCheck);
		//TableHeaderLine
		actSec = (Section) actSec.getChildren().get(line);
		return actSec;
	}
	
	/**
	 * Little help for testing the KDOM. Returns a child of the given 
	 * section, checks the right number of childs of this child and whether
	 * the isDirty flag is set correctly.
	 * @param actSec The given Section to get the child from
	 * @param childPos Position in the children list of actSec
	 * @param childCount Chosen child's expected number of children
	 * @param dirtyCheck Chosen child's isDirty flag should have this value
	 * @return The Section with position childPos in the actSec's children list
	 */
	private Section getChild (Section actSec, int childPos, int childCount, boolean dirtyCheck) {
		actSec = (Section) actSec.getChildren().get(childPos);
		assertEquals(actSec + ": ", childCount, actSec.getChildren().size());
		if (dirtyCheck) {
			assertEquals("The section's flag isDirty should be " +
					"true because its orignialText was " +
					"changed", true, actSec.isDirty());
		}
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
