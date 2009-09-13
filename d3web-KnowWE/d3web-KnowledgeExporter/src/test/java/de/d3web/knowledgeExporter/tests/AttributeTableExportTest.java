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

package de.d3web.knowledgeExporter.tests;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.testutils.HelperClass;
import de.d3web.knowledgeExporter.xlsWriters.AttributeTableWriter;
import de.d3web.report.Report;
import de.d3web.textParser.decisionTable.AttributeConfigReader;
import de.d3web.textParser.decisionTable.AttributeTableKnowledgeGenerator;
import de.d3web.textParser.decisionTable.AttributeTableSyntaxChecker;
import de.d3web.textParser.decisionTable.AttributeTableValueChecker;
import de.d3web.textParser.decisionTable.DecisionTableParserManagement;
import de.d3web.textParser.decisionTable.KnowledgeGenerator;
import de.d3web.textParser.decisionTable.SyntaxChecker;
import de.d3web.textParser.decisionTable.ValueChecker;


public class AttributeTableExportTest extends TestCase {
	
	private HelperClass hc = new HelperClass();	
	private KnowledgeBase kb = null;
	private KnowledgeManager manager;
	private AttributeTableWriter writer;

	/**
	 * Erstellt die KnowledgeBase.
	 */
	

	
	public void setUp() {

		String diagnosis = hc.readTxtFile("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "DiagnosisHierarchy.txt");
		String initQuestion = hc.readTxtFile("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "QuestionClassHierarchy.txt");
		String decisionTree = hc.readTxtFile("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "QuestionTree.txt");
		
		kb = hc.createKnowledgeBase(diagnosis, initQuestion, decisionTree).getKnowledgeBase();
		
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
		
		URL url = null;
		AttributeConfigReader attrReader = null;
		try {
			url = new File("exampleFiles" + File.separator + "decisionTable" + File.separator + "config_AT.txt").toURL();
			attrReader = new AttributeConfigReader(url);
			url = new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "Attributtabelle.xls").toURL();
		} catch (MalformedURLException e) {
			fail("beim Laden der Dateien ist ein Fehler aufgetreten");
		}
		Report r = attrReader.getReport();
		assertEquals("Beim Lesen der Attributtabelle tritt ein Fehler auf :",
				0, r.getErrorCount());
		SyntaxChecker sChecker = new AttributeTableSyntaxChecker();
		ValueChecker vChecker = new AttributeTableValueChecker(attrReader, kbm);
		KnowledgeGenerator knowGen = new AttributeTableKnowledgeGenerator(kbm,
				attrReader, true);
		DecisionTableParserManagement xlsParser = new DecisionTableParserManagement(
				url, sChecker, vChecker, knowGen);
		if (xlsParser.getReport().getErrorCount() == 0) {
			xlsParser.insertKnowledge();
		} else {
			fail("Beim Einlesen der Attributtabelle tritt ein Fehler auf");
		}
		
		//DataManager.getInstance().setBase(kb);
		manager = new KnowledgeManager(kb);
		manager.setLocale(Locale.GERMAN);
		writer = new AttributeTableWriter(manager);
	}

	
	
	public void testAttributeTabelExportByRows() {
		setUp();
		try {
			writer.writeFile(new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "AttributeTableExport.xls"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		double minRes = 0.5;
		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
				new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "Attributtabelle.xls"), 
				new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "AttributeTableExport.xls"),
				minRes));

	}
	
	public void testAttributeTabelExportByColumns() {
		setUp();
		try {
			writer.writeFile(new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "AttributeTableExport.xls"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		double minRes = 0.3;
		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
				new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "Attributtabelle.xls"), 
				new File("exampleFiles" + File.separator + "Baumerkennung" + File.separator + "AttributeTableExport.xls"),
				minRes));

	}

}
