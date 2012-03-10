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

package de.knowwe.table.test;

import java.io.IOException;

import junit.framework.TestCase;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import dummies.TestWikiConnector;

/**
 * Class for testing some functionality of class Section.
 * 
 * @author Max Diez
 * 
 */
public class TableTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	/**
	 * Test for the methods collectTextsFromLeaves and setOriginalTextSetLeaf
	 * from de.d3web.we.kdom.Section. A small table is changed: A whole row at
	 * once and a row cell by cell
	 */
	public void testSetLeafAndCollect() {
		/**
		 * Initialize Environment
		 */
		Environment.initKnowWE(new TestWikiConnector());
		Environment env = Environment.getInstance();
		final String web = Environment.DEFAULT_WEB;

		/**
		 * Build an Article and register it at env.
		 */
		String content = "!!Table\n\n%%Table\n| |Apple|Lemon|Coconut\n|sweetness|+|-|hm\n|grows in central europe|+|+|-\n|size|-|+|+\n%";
		String title = "Test_Article";
		env.buildAndRegisterArticle(content, title, web, false);
		Article article = env.getArticle(web, title);

		/**
		 * Beginning the test
		 */
		assertEquals(content, article.collectTextsFromLeaves());

		// First some initial sectionizing test
		Section<?> headerSec = checkChildsTillLine(article, 0, false);
		checkCellContents(headerSec, new String[] {
				" ", "Apple", "Lemon", "Coconut" });
		Section<?> lineSec = checkChildsTillLine(article, 1, false);
		checkCellContents(lineSec, new String[] {
				"sweetness", "+", "-", "hm" });

		// Testing setOriginalTextSetLeaf for a node being not a leaf
		String newHeader = "| |Football|Soccer|Rugby\n";
		article.getSection().setOriginalTextSetLeaf(headerSec.getID(), newHeader);
		Section<?> newHeaderSec = checkChildsTillLine(article, 0, false);
		assertEquals("Childs of node weren't deleted", 0, newHeaderSec.getChildren().size());
		assertEquals("New text wasn't saved to orgingialtext from" + headerSec.getID(),
				newHeader, newHeaderSec.getText());

		article = Environment.getInstance().getArticle(web, title);
		assertEquals(content, article.getSection().getText());
		assertNotSame(content, article.collectTextsFromLeaves());
		checkChildsTillLine(article, 0, true); // isDirty = true?!

		// not being leafs (go down once more)
		// Testing setOriginalTextSetLeaf for nodes being a leaf
		String lineText = lineSec.getText();
		String[] newLine = new String[] {
				"speed", "0", "+", "+" };
		for (int i = 0; i < newLine.length; i++) {
			article.getSection().setOriginalTextSetLeaf(
					lineSec.getChildren().get(i).getChildren().get(1).getID(),
					newLine[i]);
		}
		assertEquals("OrignialText from parent changed by changing childs (befor save)",
				lineText, lineSec.getText());
		checkChildsTillLine(article, 1, true); // isDirty = true?!

		// Saving changes to article
		StringBuilder buddy = new StringBuilder();
		article.getSection().collectTextsFromLeaves(buddy);
		assertEquals("Two methdos collectTextsFromLeaves() with different results",
				article.collectTextsFromLeaves(), buddy.toString());
		env.buildAndRegisterArticle(buddy.toString(), title,
				web);
		article = env.getArticle(web, title);
		assertNotSame(content, article.getSection().getText());
		assertTrue(article.getSection().getText().contains(newHeader));

		// Sectionizing test for new article
		headerSec = checkChildsTillLine(article, 0, false);
		checkCellContents(headerSec, new String[] {
				" ", "Football", "Soccer", "Rugby" });
		lineSec = checkChildsTillLine(article, 1, false);
		checkCellContents(lineSec, new String[] {
				"speed", "0", "+", "+" });
	}

	/**
	 * Goes down through the KDOM and checks the right number of children.
	 * 
	 * @param article Article
	 * @param line number of the table-row which should be returned
	 * @param dirtyCheck TODO
	 * @return Section of the 'line'th table-row
	 */
	private Section<?> checkChildsTillLine(Article article, int line, boolean dirtyCheck) {
		Section<?> actSec = article.getSection();
		assertEquals(actSec + ": ", 1, actSec.getChildren().size());
		// RootType
		actSec = getChild(actSec, 0, 2, dirtyCheck);
		// TableXMLType
		actSec = getChild(actSec, 1, 3, dirtyCheck);
		// XMLContent
		actSec = getChild(actSec, 1, 1, dirtyCheck);
		// XMLWrappedTable
		actSec = getChild(actSec, 0, 4, dirtyCheck);
		// TableHeaderLine
		actSec = actSec.getChildren().get(line);
		return actSec;
	}

	/**
	 * Little help for testing the KDOM. Returns a child of the given section,
	 * checks the right number of childs of this child and whether the isDirty
	 * flag is set correctly.
	 * 
	 * @param actSec The given Section to get the child from
	 * @param childPos Position in the children list of actSec
	 * @param childCount Chosen child's expected number of children
	 * @param dirtyCheck Chosen child's isDirty flag should have this value
	 * @return The Section with position childPos in the actSec's children list
	 */
	private Section<?> getChild(Section<?> actSec, int childPos, int childCount, boolean dirtyCheck) {
		actSec = actSec.getChildren().get(childPos);
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
	 * 
	 * @param sec The Section of a table row
	 * @param cellValues Values which should be visible
	 */
	private void checkCellContents(Section<?> sec, String[] cellValues) {
		for (int i = 0; i < cellValues.length; i++) {
			Section<?> actSec = sec;

			assertEquals("Node with new content wasn't split up into childs",
					4, actSec.getChildren().size());

			int expectedChildCount;
			if (i == cellValues.length - 1) {
				expectedChildCount = 3;
			}
			else {
				expectedChildCount = 2;
			}
			// TableCell #i
			actSec = actSec.getChildren().get(i);
			assertEquals(actSec + ": ", expectedChildCount, actSec.getChildren().size());
			// TableContent
			actSec = actSec.getChildren().get(1);
			assertEquals("Actual cell values are wrong: ",
					cellValues[i], actSec.getText());
		}
	}

}
