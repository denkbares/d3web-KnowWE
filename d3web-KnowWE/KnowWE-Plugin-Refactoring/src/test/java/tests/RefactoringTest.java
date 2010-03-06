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

import junit.framework.TestCase;

import org.junit.Test;

import session.RefactoringSessionTestImpl;
import utilities.Utilities;

import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.refactoring.dialog.RefactoringScript;
import de.d3web.we.refactoring.dialog.RefactoringSession;
import de.d3web.we.refactoring.script.DeleteCommentsT;
import de.d3web.we.refactoring.script.EstablishedSolutionsFindingsTraceToXCLT;
import de.d3web.we.refactoring.script.MergeXCLsT;
import de.d3web.we.refactoring.script.QuestionTreeToQuestionsSectionT;
import de.d3web.we.refactoring.script.QuestionsSectionToQuestionTreeT;
import de.d3web.we.refactoring.script.RenameAnswerT;
import de.d3web.we.refactoring.script.RenameArticleT;
import de.d3web.we.refactoring.script.RenameQuestionT;
import de.d3web.we.refactoring.script.RenameQuestionnaireT;
import de.d3web.we.refactoring.script.RenameSolutionT;
import de.d3web.we.refactoring.script.XCLToRulesT;
import dummies.KnowWETestWikiConnector;

public class RefactoringTest extends TestCase {
	private KnowWEArticleManager am;
	private KnowWEParameterMap params;
	private KnowWEObjectType type; 
	private KnowWEEnvironment ke;
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		InitPluginManager.init();
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke=KnowWEEnvironment.getInstance();
		am= ke.getArticleManager("default_web");
		type=ke.getRootType();
		params=new KnowWEParameterMap(KnowWEAttributes.WEB, "default_web");
	}
		
	@Test
	public void testXCLToRules() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/XCLToRules.txt");
//		final String refactoring = Utils.readTxtFile("src/test/resources/refactoring/XCLToRules.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/XCLToRules.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "XCLToRules",
				type, "default_web");
		am.saveUpdatedArticle(art);
		final Section<?> articleSection = am.getArticle("XCLToRules").getSection();
		
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new XCLToRulesT(articleSection);
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("XCLToRules");
		assertCREquals("The Article 'XCLToRules' was not correctly refactored: "
				, expectedAfter, art.toString());
	}
	
	@Test
	public void testEstablishedSolutionsFindingsTraceToXCL() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/EstablishedSolutionsFindingsTraceToXCL.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/EstablishedSolutionsFindingsTraceToXCL.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/EstablishedSolutionsFindingsTraceToXCL.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "EstablishedSolutionsFindingsTraceToXCL",
				type, "default_web");
		am.saveUpdatedArticle(art);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params) {
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new EstablishedSolutionsFindingsTraceToXCLT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("EstablishedSolutionsFindingsTraceToXCL");
		assertCREquals("The Article 'EstablishedSolutionsFindingsTraceToXCL' was not correctly refactored: "
				, expectedAfter, art.toString());
	}
	
	@Test
	public void testMergeXCLs() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/MergeXCLs.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/MergeXCLs.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/MergeXCLs.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "MergeXCLs",
				type, "default_web");
		am.saveUpdatedArticle(art);
				
		RefactoringSessionTestImpl rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new MergeXCLsT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("MergeXCLs");
		assertCREquals("The Article 'MergeXCLs' was not correctly refactored: "
				, expectedAfter, art.toString());
	}
	
	@Test
	public void testDeleteComments() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/DeleteComments.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/DeleteComments.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/DeleteComments.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "DeleteComments",
				type, "default_web");
		am.saveUpdatedArticle(art);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new DeleteCommentsT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("DeleteComments");
		assertCREquals("The Article 'DeleteComments' was not correctly refactored: "
				, expectedAfter, art.toString());
	}
	
	

	@Test
	public void testQuestionTreeToQuestionsSection() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/QuestionTreeToQuestionsSection.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/QuestionTreeToQuestionsSection.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/QuestionTreeToQuestionsSection.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "QuestionTreeToQuestionsSection",
				type, "default_web");
		am.saveUpdatedArticle(art);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new QuestionTreeToQuestionsSectionT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("QuestionTreeToQuestionsSection");
		assertCREquals("The Article 'QuestionTreeToQuestionsSection' was not correctly refactored: "
				, expectedAfter, art.toString());
	}
	
	@Test
	public void testQuestionsSectionToQuestionTree() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/QuestionsSectionToQuestionTree.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/QuestionsSectionToQuestionTree.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/QuestionsSectionToQuestionTree.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "QuestionsSectionToQuestionTree",
				type, "default_web");
		am.saveUpdatedArticle(art);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new QuestionsSectionToQuestionTreeT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("QuestionsSectionToQuestionTree");
		assertCREquals("The Article 'QuestionsSectionToQuestionTree' was not correctly refactored: "
				, expectedAfter, art.toString());
	}

	@Test
	public void testRenameQuestionnaire() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/RenameObject.txt");
		String content2 = Utilities.readTxtFile("src/test/resources/wiki/RenameObjectUNA.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/Rename.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/RenameQuestionnaire/RenameObject.txt");
		String expectedAfter2 = Utilities.readTxtFile("src/test/resources/reference/RenameQuestionnaire/RenameObjectUNA.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "RenameObject",
				type, "default_web");
		am.saveUpdatedArticle(art);
		
		KnowWEArticle art2 = new KnowWEArticle(content2, "RenameObjectUNA",
				type, "default_web");
		am.saveUpdatedArticle(art2);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new RenameQuestionnaireT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("RenameObject");
		assertCREquals("The Article 'RenameObject' was not correctly refactored: "
				, expectedAfter, art.toString());
		art2 = am.getArticle("RenameObjectUNA");
		assertCREquals("The Article 'RenameObjectUNA' was not correctly refactored: "
				, expectedAfter2, art2.toString());
	}
	
	@Test
	public void testRenameQuestion() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/RenameObject.txt");
		String content2 = Utilities.readTxtFile("src/test/resources/wiki/RenameObjectUNA.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/Rename.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/RenameQuestion/RenameObject.txt");
		String expectedAfter2 = Utilities.readTxtFile("src/test/resources/reference/RenameQuestion/RenameObjectUNA.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "RenameObject",
				type, "default_web");
		am.saveUpdatedArticle(art);
		
		KnowWEArticle art2 = new KnowWEArticle(content2, "RenameObjectUNA",
				type, "default_web");
		am.saveUpdatedArticle(art2);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new RenameQuestionT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("RenameObject");
		assertCREquals("The Article 'RenameObject' was not correctly refactored: "
				, expectedAfter, art.toString());
		art2 = am.getArticle("RenameObjectUNA");
		assertCREquals("The Article 'RenameObjectUNA' was not correctly refactored: "
				, expectedAfter2, art2.toString());
	}
	
	@Test
	public void testRenameSolution() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/RenameObject.txt");
		String content2 = Utilities.readTxtFile("src/test/resources/wiki/RenameObjectUNA.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/Rename.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/RenameSolution/RenameObject.txt");
		String expectedAfter2 = Utilities.readTxtFile("src/test/resources/reference/RenameSolution/RenameObjectUNA.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "RenameObject",
				type, "default_web");
		am.saveUpdatedArticle(art);
		
		KnowWEArticle art2 = new KnowWEArticle(content2, "RenameObjectUNA",
				type, "default_web");
		am.saveUpdatedArticle(art2);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new RenameSolutionT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("RenameObject");
		assertCREquals("The Article 'RenameObject' was not correctly refactored: "
				, expectedAfter, art.toString());
		art2 = am.getArticle("RenameObjectUNA");
		assertCREquals("The Article 'RenameObjectUNA' was not correctly refactored: "
				, expectedAfter2, art2.toString());
	}
	
	@Test
	public void testRenameAnswer() throws ProviderException {
	
		/*
		 * Setup
		 */
		String content = Utilities.readTxtFile("src/test/resources/wiki/RenameObject.txt");
		String content2 = Utilities.readTxtFile("src/test/resources/wiki/RenameObjectUNA.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/Rename.txt");
		String expectedAfter = Utilities.readTxtFile("src/test/resources/reference/RenameAnswer/RenameObject.txt");
		String expectedAfter2 = Utilities.readTxtFile("src/test/resources/reference/RenameAnswer/RenameObjectUNA.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(content, "RenameObject",
				type, "default_web");
		am.saveUpdatedArticle(art);
		
		KnowWEArticle art2 = new KnowWEArticle(content2, "RenameObjectUNA",
				type, "default_web");
		am.saveUpdatedArticle(art2);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new RenameAnswerT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("RenameObject");
		assertCREquals("The Article 'RenameObject' was not correctly refactored: "
				, expectedAfter, art.toString());
		art2 = am.getArticle("RenameObjectUNA");
		assertCREquals("The Article 'RenameObjectUNA' was not correctly refactored: "
				, expectedAfter2, art2.toString());
	}
	
	@Test
	public void testRenameArticle() throws ProviderException {
	
		/*
		 * Setup
		 */
		String contentR = Utilities.readTxtFile("src/test/resources/wiki/Rename.txt");
//		final String refactoring = Utilities.readTxtFile("src/test/resources/refactoring/Rename.txt");
		String expectedAfterR = Utilities.readTxtFile("src/test/resources/reference/Rename.txt");
		
		String contentRA = Utilities.readTxtFile("src/test/resources/wiki/RenameA.txt");
		String expectedAfterRTest = Utilities.readTxtFile("src/test/resources/reference/RenameTest.txt");
		
		String contentRB = Utilities.readTxtFile("src/test/resources/wiki/RenameB.txt");
		String expectedAfterRB = Utilities.readTxtFile("src/test/resources/reference/RenameB.txt");
		
		/*
		 * Init first Article
		 */
		KnowWEArticle art = new KnowWEArticle(contentR, "Rename",
				type, "default_web");
		am.saveUpdatedArticle(art);
		
		KnowWEArticle artA = new KnowWEArticle(contentRA, "RenameA",
				type, "default_web");
		am.saveUpdatedArticle(artA);
		
		KnowWEArticle artB = new KnowWEArticle(contentRB, "RenameB",
				type, "default_web");
		am.saveUpdatedArticle(artB);
				
		RefactoringSession rst = new RefactoringSessionTestImpl(params){
			@Override
			protected RefactoringScript findRefactoringScript() {
				return new RenameArticleT();
			}
		};
		rst.setRefManager("default_web");
		rst.runSession();
		art = am.getArticle("Rename");
		assertCREquals("The Article 'Rename' was not correctly refactored: "
				, expectedAfterR, art.toString());
		
		// Das Umbenennen des Artikelnamens kann leider nicht mit dem Wiki emuliert werden.
		artA = am.getArticle("RenameA");
		assertCREquals("The Article 'RenameA' was not correctly refactored: "
				, expectedAfterRTest, artA.toString());
		
		artB = am.getArticle("RenameB");
		assertCREquals("The Article 'RenameB' was not correctly refactored: "
				, expectedAfterRB, artB.toString());
	}
	
	
	
	/**
	 * Asserts that two strings are equal when replacing all occurrences of \r\n with \n in both strings.
	 * (carriage return escaping) 
	 */
	public static void assertCREquals(String message, String expected, String actual) {
		String regex = "\r\n";
		String replacement = "\n";
		assertEquals(message, expected.replaceAll(regex,replacement), actual.replaceAll(regex, replacement));
	}
	
	/**
	 * Asserts that two strings are equal when replacing all occurrences of \r\n with \n in both strings.
	 * (carriage return escaping) 
	 */
	static public void assertCREquals(String expected, String actual) {
	    assertCREquals(null, expected, actual);
	}
	
	
	
}
