//package de.d3web.knowledgeExporter.tests.old;
// *
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
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Locale;
//
//import junit.framework.TestCase;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//import de.d3web.knowledgeExporter.testutils.HelperClass;
//import de.d3web.knowledgeExporter.xlsWriters.DiagnosisScoresTableWriter;
//
//
//public class DiagnosisScoreTableExportTest extends TestCase {
//	
//	private HelperClass hc = new HelperClass();	
//	private KnowledgeBase kb = null;
//	private KnowledgeManager manager;
//	private DiagnosisScoresTableWriter writer;
//	private String examplespath;
//	/**
//	 * Erstellt die KnowledgeBase.
//	 */
//	
//
//	
//	public void setUp() {
//		String basepath=System.getProperty("user.dir");
//		examplespath=basepath+File.separatorChar+"src"+File.separatorChar+"doc"+File.separatorChar+"examples";
//
//		String diagnosis = hc.readTxtFile(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisHierarchy.txt");
//		String initQuestion = hc.readTxtFile(examplespath + File.separator + "Baumerkennung" + File.separator + "QuestionClassHierarchy.txt");
//		String decisionTree = hc.readTxtFile(examplespath + File.separator + "Baumerkennung" + File.separator + "QuestionTree.txt");
//		//System.out.println(diagnosis + initQuestion + decisionTree);
//		kb = hc.createKnowledgeBase(diagnosis, initQuestion, decisionTree).getKnowledgeBase();
//		//System.out.println(kb.getQuestions());
//		
//		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
//		
//		URL url = null;
//		//AttributeConfigReader attrReader = null;
//		try {
//			url = new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls").toURL();
//		} catch (MalformedURLException e) {
//			fail("beim Laden der Dateien ist ein Fehler aufgetreten");
//		}
//		DecisionTableConfigReader cReader = new DecisionTableConfigReader();
//		SyntaxChecker sChecker = new DiagnosisScoreTableSyntaxChecker(cReader);
//		ValueChecker vChecker = new DiagnosisScoreTableValueChecker(cReader, kbm);
//		KnowledgeGenerator knowGen = new DiagnosisScoreTableRuleGenerator(kbm, true);
//		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
//				url, sChecker, vChecker, knowGen);
//		if (xlsParser.getReport().getErrorCount() == 0) {
//			xlsParser.insertKnowledge();
//		} else {
//			fail("Beim Einlesen der Tabelle tritt ein Fehler auf");
//		}
//		
//		//DataManager.getInstance().setBase(kb);
//		manager = new KnowledgeManager(kb);
//		manager.setLocale(Locale.GERMAN);
//		writer = new DiagnosisScoresTableWriter(manager);
//	}
//
//
//	
//	public void testCompareXLSTablesMethod() {
//		setUp();
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByCell(
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"), 
//				new File(examplespath+ File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls")));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByContent(
//				new File(examplespath+ File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"), 
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls")));
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"), 
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"),
//				minRes));
//	}
//	
//	
//	public void testDiagnosisScoresTabelExportByRows() {
//		setUp();
//		try {
//			writer.writeFile(new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport.xls"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		double minRes = 0.8;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"), 
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport.xls"),
//				minRes));
//
//	}
//	
//	public void testDiagnosisScoresTabelExportByRows2() {
//		setUp();
//		double minRes = 0.8;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport2.xls"), 
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport.xls"),
//				minRes));
//
//	}
//	
//	public void testDiagnosisScoresTabelExportByColumns() {
//		setUp();
//		try {
//			writer.writeFile(new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport.xls"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		double minRes = 0.3;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "diagnosescores.xls"), 
//				new File(examplespath + File.separator + "Baumerkennung" + File.separator + "DiagnosisScoreExport.xls"),
//				minRes));
//
//	}
//	
////	public void testPremadeTablesByColumns() {
////		double minRes = 0.3;
////		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
////				new File("exampleFiles\\Baumerkennung\\diagnosescores.xls"), 
////				new File("exampleFiles\\Baumerkennung\\DiagnosisScoresKnowME.xls"),
////				minRes));
////	}
////	
////	public void testPremadeTablesByRows() {
////		double minRes = 0.5;
////		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
////				new File("exampleFiles\\Baumerkennung\\diagnosescores.xls"), 
////				new File("exampleFiles\\Baumerkennung\\DiagnosisScoresKnowME.xls"),
////				minRes));
////	}
////	
//
//}
