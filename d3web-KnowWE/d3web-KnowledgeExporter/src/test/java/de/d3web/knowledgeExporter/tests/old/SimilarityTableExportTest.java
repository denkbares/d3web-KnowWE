//package de.d3web.knowledgeExporter.tests.old;
///*
// * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
// *                    Computer Science VI, University of Wuerzburg
// *
// * This is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as
// * published by the Free Software Foundation; either version 3 of
// * the License, or (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this software; if not, write to the Free
// * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
// */
//
//package de.d3web.knowledgeExporter.tests;
//
//import java.io.File;
//import java.net.URL;
//
//import junit.framework.TestCase;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
//import de.d3web.kernel.domainModel.qasets.QuestionNum;
//import de.d3web.kernel.psMethods.shared.PSMethodShared;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumFuzzy;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//import de.d3web.knowledgeExporter.testutils.HelperClass;
//import de.d3web.textParser.decisionTable.DecisionTableParserManagement;
//import de.d3web.textParser.decisionTable.KnowledgeGenerator;
//import de.d3web.textParser.decisionTable.SimilarityTableKnowledgeGenerator;
//import de.d3web.textParser.decisionTable.SimilarityTableSyntaxChecker;
//import de.d3web.textParser.decisionTable.SimilarityTableValueChecker;
//import de.d3web.textParser.decisionTable.SyntaxChecker;
//import de.d3web.textParser.decisionTable.ValueChecker;
//
//
//public class SimilarityTableExportTest extends TestCase {
//	
//
//	private HelperClass hc = new HelperClass();
//	private KnowledgeBase kb = null;
//	private KnowledgeManager manager;
////	private SimilarityTableWriter writer;
//
//	public void testFuzzyNormal(){
//		this.createKB();
//		this.setUptestParse("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "Aehnlichkeitstabelle.xls");
//		
//		// Import
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		assertEquals("",1,qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).size());	
//		assertTrue(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0) instanceof QuestionComparatorNumFuzzy);
//		QuestionComparatorNumFuzzy fuzzy = (QuestionComparatorNumFuzzy)qu.
//		getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0);
//		assertEquals("Der Wert fuer increasingLeft stimmt nicht :  ",2.0,
//				fuzzy.getIncreasingLeft());
//		assertEquals("Der Wert fuer constLeft stimmt nicht :  ",
//				4.0,fuzzy.getConstLeft());
//		assertEquals("Der Wert fuer constRight stimmt nicht : ",6.0,
//				fuzzy.getConstRight());
//		assertEquals("Der Wert fuer decreasingRight stimmt nicht : ",9.0,
//				fuzzy.getDecreasingRight());
//		assertEquals("Die Methode stimmt nicht : ","absolute",
//				fuzzy.getInterpretationMethod());
//		
//		
//		//Export
////		try {
////			writer.writeFile(new File("exampleFiles\\decisionTable\\AehnlichkeitstabelleExport.xls"));
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "Aehnlichkeitstabelle.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleExport.xls"),
//				minRes));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "Aehnlichkeitstabelle.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleExport.xls"),
//				minRes));
//	}
//	
//	public void testFuzzyDecreasingRight(){
//		this.createKB();
//		this.setUptestParse("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDR.xls");
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		
//		// Import
//		assertEquals("",1,qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).size());	
//		assertTrue(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0) instanceof 
//				QuestionComparatorNumFuzzy);
//		QuestionComparatorNumFuzzy fuzzy = (QuestionComparatorNumFuzzy)qu.
//		getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0);
//		assertEquals("Der Wert fuer increasingLeft stimmt nicht :  ",1.0,
//				fuzzy.getIncreasingLeft());
//		assertEquals("Der Wert fuer constLeft stimmt nicht :  ",2.0,
//				fuzzy.getConstLeft());
//		assertEquals("Der Wert fuer constRight stimmt nicht : ",3.0,
//				fuzzy.getConstRight());
//		assertEquals("Der Wert fuer decreasingRight stimmt nicht : ",
//				Double.POSITIVE_INFINITY,fuzzy.getDecreasingRight());
//		
//		//Export
////		try {
////			writer.writeFile(new File("exampleFiles\\decisionTable\\AehnlichkeitstabelleDRExport.xls"));
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDR.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDRExport.xls"),
//				minRes));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDR.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDRExport.xls"),
//				minRes));
//	}
//	
//	public void testFuzzyIncreasingLeft(){
//		this.createKB();
//		this.setUptestParse("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleIL.xls");
//		
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		
//		// Import
//		assertEquals("",1,qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).size());	
//		assertTrue(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0) instanceof 
//				QuestionComparatorNumFuzzy);
//		QuestionComparatorNumFuzzy fuzzy = (QuestionComparatorNumFuzzy)qu.
//		getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0);
//		assertEquals("Der Wert fuer increasingLeft stimmt nicht :  ",
//				Double.NEGATIVE_INFINITY,fuzzy.getIncreasingLeft());
//		assertEquals("Der Wert fuer constLeft stimmt nicht :  ",2.0,
//				fuzzy.getConstLeft());
//		assertEquals("Der Wert fuer constRight stimmt nicht : ",3.0,
//				fuzzy.getConstRight());
//		assertEquals("Der Wert fuer decreasingRight stimmt nicht : ",4.0,
//				fuzzy.getDecreasingRight());
//		
//		//		Export
////		try {
////			writer.writeFile(new File("exampleFiles\\decisionTable\\AehnlichkeitstabelleILExport.xls"));
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleIL.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleILExport.xls"),
//				minRes));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleIL.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleILExport.xls"),
//				minRes));
//	}
//	
//	public void testInterpretationMethodPercentage(){
//		this.createKB();
//		this.setUptestParse("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleP.xls");
//		
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		
//		// Import
//		assertEquals("",1,qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).size());	
//		assertTrue(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0) instanceof 
//				QuestionComparatorNumFuzzy);
//		QuestionComparatorNumFuzzy fuzzy = (QuestionComparatorNumFuzzy)qu.
//		getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0);
//		assertEquals("Der Wert fuer increasingLeft stimmt nicht :  ",1.0,
//				fuzzy.getIncreasingLeft());
//		assertEquals("Der Wert fuer constLeft stimmt nicht :  ",2.0,
//				fuzzy.getConstLeft());
//		assertEquals("Der Wert fuer constRight stimmt nicht : ",3.0,
//				fuzzy.getConstRight());
//		assertEquals("Der Wert fuer decreasingRight stimmt nicht : ",4.0,
//				fuzzy.getDecreasingRight());
//		assertEquals("Die Methode stimmt nicht : ","percentage",
//				fuzzy.getInterpretationMethod());
//		
//		//		Export
////		try {
////			writer.writeFile(new File("exampleFiles\\decisionTable\\AehnlichkeitstabellePExport.xls"));
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleP.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabellePExport.xls"),
//				minRes));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleP.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabellePExport.xls"),
//				minRes));
//	}
//	
//	public void testFuzzyDoubles(){
//		this.createKB();
//		this.setUptestParse("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleD.xls");
//		
//		
//		// Import
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		assertEquals("",1,qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).size());	
//		assertTrue(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0) instanceof 
//				QuestionComparatorNumFuzzy);
//		QuestionComparatorNumFuzzy fuzzy = (QuestionComparatorNumFuzzy)qu.
//		getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY).get(0);
//		assertEquals("Der Wert fuer increasingLeft stimmt nicht :  ",1.22,
//				fuzzy.getIncreasingLeft());
//		assertEquals("Der Wert fuer constLeft stimmt nicht :  ",
//				2.123456789,fuzzy.getConstLeft());
//		assertEquals("Der Wert fuer constRight stimmt nicht : ",111.11,
//				fuzzy.getConstRight());
//		assertEquals("Der Wert fuer decreasingRight stimmt nicht : ",2000.0,
//				fuzzy.getDecreasingRight());
//		assertEquals("Die Methode stimmt nicht : ","percentage",
//				fuzzy.getInterpretationMethod());
//		
//		//		Export
////		try {
////			writer.writeFile(new File("exampleFiles\\decisionTable\\AehnlichkeitstabelleDExport.xls"));
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleD.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDExport.xls"),
//				minRes));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleD.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleDExport.xls"),
//				minRes));
//	}
//	
//	/**
//	 * Testet, ob falsche Werte abgefangen werden. 
//	 * In der Datei ist increasingLeft > constLeft. 
//	 *
//	 */
//	public void testFuzzyWrongValues(){
//		this.createKB();
//		
//		// Import
//		try{
//			URL xlsFile = new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleW.xls").toURL();
//			KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
//			SyntaxChecker sChecker = new SimilarityTableSyntaxChecker();
//			ValueChecker vChecker = new SimilarityTableValueChecker(kbm);
//			KnowledgeGenerator knowGen = new SimilarityTableKnowledgeGenerator(kbm);
//
//			DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
//					xlsFile, sChecker, vChecker, knowGen);
//			xlsParser.checkContent();
//			assertEquals("es wurde eine falsche Anzahl an Fehlern gefunden : ", 
//					1,xlsParser.getReport().getErrorCount());
//			xlsParser.insertKnowledge(); 
//		}catch(Exception ex){
//			fail("Es ist faelschlicherweise eine Exception aufgetreten!" 
//					+ ex.getMessage());
//		}
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		if(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY) != null){
//			if(qu.getKnowledge(PSMethodShared.class,
//					PSMethodShared.SHARED_SIMILARITY).size() == 1){
//				}
//			else{
//				fail("Es wurden falsche Werte eingetragen");
//			}
//		}
//	}
//	
//	/**
//	 * Testet ob ein Fehler angegeben wird, wenn das Feld fuer 
//	 * constLeft leer ist. 
//	 *
//	 */
//	public void testFuzzyEmptyConstLeftField(){
//		this.createKB();
//		
//		// Import
//		try{
//			URL xlsFile = new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "decisionTable" + File.separator + "AehnlichkeitstabelleWV.xls").toURL();
//			KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
//			SyntaxChecker sChecker = new SimilarityTableSyntaxChecker();
//			ValueChecker vChecker = new SimilarityTableValueChecker(kbm);
//			KnowledgeGenerator knowGen = new SimilarityTableKnowledgeGenerator(kbm);
//			DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
//					xlsFile, sChecker, vChecker, knowGen);
//			xlsParser.checkContent();
//			assertEquals("es wurde eine falsche Anzahl an Fehlern gefunden : ", 
//					1,xlsParser.getReport().getErrorCount());
//			xlsParser.insertKnowledge();
//		}catch(Exception ex){
//			fail("Es ist faelschlicherweise eine Exception aufgetreten!" 
//					+ ex.getMessage());
//		}
//		QuestionNum qu = (QuestionNum)kb.searchQuestions("Q1");
//		if(qu.getKnowledge(PSMethodShared.class,
//				PSMethodShared.SHARED_SIMILARITY) != null){
//			if(qu.getKnowledge(PSMethodShared.class,
//					PSMethodShared.SHARED_SIMILARITY).size() == 1){
//				}
//			else{
//				fail("Es wurden falsche Werte eingetragen");
//			}
//		}
//	}
//	
//	/**
//	 * Erstellt die KnowledgeBase.
//	 */
//	private void createKB() {
//		String diagnosisHierarchy = "diagnosis 1\n diagnosis 2";
//		String initQuestion = "start";
//		String[] decisionTreeFormatted = { 
//				"start", 
//				"- question1 [num]",
//				"- question2 [num]"
//				};
//		String decisionTree = hc.toString(decisionTreeFormatted);
//		kb = hc.createKnowledgeBase(diagnosisHierarchy, initQuestion, decisionTree)
//				.getKnowledgeBase();
//	}
//	
//	private void setUptestParse(String table){
//		try{
//			URL xlsFile = new File(table).toURL();
//			KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
//			SyntaxChecker sChecker = new SimilarityTableSyntaxChecker();
//			ValueChecker vChecker = new SimilarityTableValueChecker(kbm);
//			KnowledgeGenerator knowGen = new SimilarityTableKnowledgeGenerator(kbm);
//
//			DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
//					xlsFile, sChecker, vChecker, knowGen);
//			xlsParser.checkContent();
//			if (xlsParser.getReport().getErrorCount() == 0){
//				xlsParser.insertKnowledge();
//			} else {
//				fail("beim Parsen ist ein Fehler aufgetreten: " + xlsParser.getReport().getErrorMsg());
//			}
//			kb = kbm.getKnowledgeBase();
//			//DataManager.getInstance().setBase(kb);
//			manager = new KnowledgeManager(kb);
////			writer = SimilarityTableWriter.makeWriter(manager);
//		} catch (Exception ex) {
//			fail("Es ist faelschlicherweise eine Exception aufgetreten!" 
//					+ ex.getMessage());
//		}
//	}
//
//
//}
