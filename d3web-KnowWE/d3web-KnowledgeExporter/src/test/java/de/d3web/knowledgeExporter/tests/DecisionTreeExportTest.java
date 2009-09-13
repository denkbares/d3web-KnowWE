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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.testutils.HelperClass;
import de.d3web.knowledgeExporter.txtWriters.DecisionTreeWriter;
import de.d3web.knowledgeExporter.xlsWriters.HeuristicDecisionTableWriter;
import de.d3web.report.Report;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.textParser.casesTable.TextParserResource;


public class DecisionTreeExportTest extends TestCase {
	
	private KBTextInterpreter kbTxtInterpreter;
	private Map<String, TextParserResource> input;
	private Map<String, Report> output;
	private KnowledgeBase kb;
	private KnowledgeManager manager;
	private DecisionTreeWriter writer;
	private HeuristicDecisionTableWriter tWriter;
	private HelperClass hc = new HelperClass(); 



	

	public void testCondOC() {

		String diagnosis = "answer 1\n answer 2\n answer 33\n";	
		String initQuestion = "start";
		String questions = "start\n"
			+ "- question oc [oc]\n"
			+ "-- yellow\n"
			+ "--- answer 1 (P7)\n"
			+ "--- answer 33 (P4)\n"
			+ "-- blau\n"
			+ "--- answer 2 (P7)\n";
		
		setUpKB(diagnosis, initQuestion, questions);
		setUpWriter();
		assertEquals("Wrong export: ", questions, writer.writeText());
	}

	public void testCondMC() {

		String diagnosis = "answer 1\n answer 2\n answer 3";
		String initQuestion = "start";
		String questions = "start\n"
			+ "- question mc [mc]\n"
			+ "-- yellow\n"
			+ "--- answer 1 (P7)\n"
			+ "-- blue\n"
			+ "--- answer 2 (P7)\n"
			+ "-- red\n"
			+ "--- answer 3 (P6)\n";
		
		setUpKB(diagnosis, initQuestion, questions);
		setUpWriter();
		assertEquals("Wrong export: ", questions, writer.writeText());

	}

	
	public void testCondNum() {

		String diagnosis = "answer 1\n answer 2";
		String initQuestion = "start";
		String questions = "start\n"
				+ "- question num [num] {Euro} (0 100) \n"
				+ "-- < 50\n"
				+ "--- answer 1 (P7)\n"
				+ "-- [50 100]\n"
				+ "--- answer 2 (P7)\n";
		setUpKB(diagnosis, initQuestion, questions);
		setUpWriter();
		assertEquals("Wrong export: ", questions, writer.writeText());
	}

	
	public void testCondYN() {

		String diagnosis = "answer 1\n answer 2";
		String initQuestion = "start";
		String questions = "start\n"
			+ "- qüestion yn [yn]\n"
			+ "-- Yes\n"
			+ "--- answer 1 (P7)\n"
			+ "-- No\n"
			+ "--- answer 2 (P7)\n";
		setUpKB(diagnosis, initQuestion, questions);
		setUpWriter();
		assertEquals("Wrong export: ", questions, writer.writeText());
		
	}
	
	public void testQuestionOrder() {
		String diagnosis = "dead\n skinny\n normal\n small\n big\n corpulent\n"
			+ "answer 81\n answer 2\n b answer 3\n answer 48\n answer 6\n answer 5";
		String initQuestion = "start";
		String questions = "start\n"
			+ "- BMI [num] {BMI} (10 60) \n"
			+ "-- < 13\n"
			+ "--- dead (P5)\n"
			+ "-- [13 20]\n"
			+ "--- skinny (P7)\n"
			+ "-- [21 25]\n"
			+ "--- normal (P7)\n"
			+ "-- > 25\n"
			+ "--- corpulent (P5)\n"
		
			+ "- weight [num] {kg} (0 500) \n"
			+ "-- < 50\n"
			+ "--- small (P5)\n"
			+ "-- [51 100]\n"
			+ "--- normal (P5)\n"
			+ "-- > 100\n"
			+ "--- big (P5)\n"
			
			+ "- height [num] {cm} (30 250) \n"
			+ "-- < 50\n"
			+ "--- small (P5)\n"
			+ "-- [51 100]\n"
			+ "--- normal (P5)\n"
			+ "-- > 100\n"
			+ "--- big (P7)\n"
			
			+ "- question 1 [mc]\n"
			+ "-- yellow\n"
			+ "--- answer 81 (P5)\n"
			+ "-- blue\n"
			+ "--- answer 2 (P7)\n"
			+ "-- red\n"
			+ "--- b answer 3 (P7)\n"
			+ "-- east\n"
			+ "--- answer 48 (P5)\n"
			+ "-- south\n"
			+ "--- answer 5 (P7)\n"
			+ "-- north\n"
			+ "--- answer 6 (P7)\n";
		
		setUpKB(diagnosis, initQuestion, questions);
		setUpWriter();
		assertEquals("Wrong export: ", questions, writer.writeText());
	}
	
	public void testExampleFiles() {
		
		String diagnosis = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "DiagnosisHierarchy.txt");
		String initQuestion = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "QuestionClassHierarchy.txt");
		String decisionTree = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "QuestionTree.txt");
		
		KnowledgeManager.setLocale(Locale.GERMAN);
		setUpKB(diagnosis, initQuestion, decisionTree);
		setUpWriter();
		assertEquals("Wrong export: ", decisionTree, writer.writeText());
		
		
	}
	
	public void testDecisionTableWriter() {
		String diagnosis = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "DiagnosisHierarchy.txt");
		String initQuestion = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "QuestionClassHierarchy.txt");
		String decisionTree = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "QuestionTree.txt");
		
		KnowledgeManager.setLocale(Locale.GERMAN);
		setUpKB(diagnosis, initQuestion, decisionTree);
		setUpTableWriter();
		try {
			tWriter.writeFile(new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "Baumerkennung" + File.separator + "HeuristicDecisionTable.xls"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TODO: Mehr Tests zu Referenzen (siehe ExtDecisionTreeTest @ d3web-TextParser-Test)
	public void testReferences() {
		String[] diagnosis = {
				"diagnosis 1", 
				"diagnosis 2"
				};
		
		String[] initQuestion = {"start"};
		String[] decisionTreeFormatted = { 
				"start", 
				"- question ref [yn]", 
				"-- Yes @Yes",
				"--- diagnosis 1 (P7)", 
				"-- No", 
				"--- diagnosis 2 (P7)",
				"| @yes | url | Lange Erkl\u00e4rung | http://www.yes.de |" 
				};
		KnowledgeManager.setLocale(Locale.ENGLISH);
		setUpKB2(diagnosis, initQuestion, decisionTreeFormatted);
		assertEquals("Wrong export: ", hc.toString(decisionTreeFormatted), writer.writeText());
		
	}



	private InputStream getStream(String ressource) {
		InputStream stream;
		stream = new ByteArrayInputStream(ressource.getBytes());
		return stream;
	}

	private void setUpKB(String diagnosis, String initQuestion, String questions) {
		
		initialize();
		//hole ressourcen
		TextParserResource ressource;

		if (questions != null) {
			ressource = new TextParserResource(getStream(questions));
			input.put(KBTextInterpreter.QU_DEC_TREE, ressource);
		}
		if (diagnosis != null) {
			ressource = new TextParserResource(getStream(diagnosis));
			input.put(KBTextInterpreter.DH_HIERARCHY, ressource);
		}
		if (initQuestion != null) {
			ressource = new TextParserResource(getStream(initQuestion));
			input.put(KBTextInterpreter.QCH_HIERARCHY, ressource);
		}

		output = kbTxtInterpreter.interpreteKBTextReaders(input, "JUnit-KB",
				false, false);
		kb = kbTxtInterpreter.getKnowledgeBase();
		
	}
	
	private void setUpKB2(String[] diagnosis, String[] initQuestion, String[] decTree) {
		String diag = hc.toString(diagnosis);
		String initQ = hc.toString(initQuestion);
		String dT = hc.toString(decTree);
		
		kb = hc.createKnowledgeBase(diag, initQ,dT).getKnowledgeBase();
		setUpWriter();
	}
	
	public void initialize() {
		kbTxtInterpreter = new KBTextInterpreter();
		input = new HashMap<String, TextParserResource>();
		output = new HashMap<String, Report>();
	}
	
	public void setUpWriter() {
		//DataManager.getInstance().setBase(kb);
		manager = new KnowledgeManager(kb);
		writer = new DecisionTreeWriter(manager);
		//writer.setExportDecisionTreeID(true);
	}
	
	public void setUpTableWriter() {
		//DataManager.getInstance().setBase(kb);
		manager = new KnowledgeManager(kb);
		tWriter = new HeuristicDecisionTableWriter(manager);
		//writer.setExportDecisionTreeID(true);
	}
}
