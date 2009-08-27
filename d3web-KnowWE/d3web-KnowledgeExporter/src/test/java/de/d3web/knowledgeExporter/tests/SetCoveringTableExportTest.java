package de.d3web.knowledgeExporter.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.testutils.HelperClass;
import de.d3web.knowledgeExporter.xlsWriters.SetCoveringTableWriter;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;


public class SetCoveringTableExportTest extends TestCase {
	

	public void testWritingCoveringListTable() {
		KBTextInterpreter kbTxtInterpreter;
		Map<String, TextParserResource> input;
		Map<String, Report> output;
		KnowledgeBase kb;
		KnowledgeManager manager;
		SetCoveringTableWriter tableWriter;
		HelperClass hc = new HelperClass(); 
		
		kbTxtInterpreter = new KBTextInterpreter();
		input = new HashMap<String, TextParserResource>();
		output = new HashMap<String, Report>();
		//hole ressourcen
		TextParserResource ressource;
		
		String diagnosis = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Loesungen.txt");
		String initQuestion = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Frageklassen.txt");
		String questions = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Fragebaum.txt");
		String xcl = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "XCL.txt");
		

		if (questions != null) {
			ressource = new TextParserResource(new ByteArrayInputStream(questions.getBytes()));
			input.put(KBTextInterpreter.QU_DEC_TREE, ressource);
		}
		if (diagnosis != null) {
			ressource = new TextParserResource(new ByteArrayInputStream(diagnosis.getBytes()));
			input.put(KBTextInterpreter.DH_HIERARCHY, ressource);
		}
		if (initQuestion != null) {
			ressource = new TextParserResource(new ByteArrayInputStream(initQuestion.getBytes()));
			input.put(KBTextInterpreter.QCH_HIERARCHY, ressource);
		}
		if (xcl != null) {
			ressource = new TextParserResource(new ByteArrayInputStream(xcl.getBytes()));
			input.put(KBTextInterpreter.SET_COVERING_LIST, ressource);
		}

		output = kbTxtInterpreter.interpreteKBTextReaders(input, "JUnit-KB",
				false, false);
		//System.out.println(output.get(KBTextInterpreter.SET_COVERING_LIST).getAllMessagesAsString());
		kb = kbTxtInterpreter.getKnowledgeBase();
		//System.out.println(output);
		manager = new KnowledgeManager(kb);
		tableWriter = new SetCoveringTableWriter(manager);
		try {
			tableWriter.writeFile(new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "SetCoveringTable.xls"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void testCoveringTabelExportByRows() {
//		
//		double minRes = 0.5;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByRowContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "BaumwissensbasisCoveringLists" + File.separator + "Ueberdeckungstabelle.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "BaumwissensbasisCoveringLists" + File.separator + "UeberdeckungstabelleExport.xls"),
//				minRes));
//
//	}
//	
//	public void testCoveringTabelExportByColumns() {
//		
//		double minRes = 0.3;
//		assertEquals("Unterschiede zwischen den Tabellen: ", "", hc.compareXLSTablesByColumnContent(
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "BaumwissensbasisCoveringLists" + File.separator + "Ueberdeckungstabelle.xls"), 
//				new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "BaumwissensbasisCoveringLists" + File.separator + "UeberdeckungstabelleExport.xls"),
//				minRes));
//
//	}

}
