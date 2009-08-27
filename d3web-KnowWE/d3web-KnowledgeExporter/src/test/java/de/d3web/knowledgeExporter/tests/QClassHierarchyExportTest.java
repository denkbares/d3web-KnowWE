package de.d3web.knowledgeExporter.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.QClassHierarchyWriter;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;


public class QClassHierarchyExportTest extends TestCase {
	
	private KBTextInterpreter kbTxtInterpreter;
	private Map<String, TextParserResource> input;
	private Map<String, Report> output;
	private KnowledgeBase kb;
	private KnowledgeManager manager;
	private QClassHierarchyWriter writer;

	public void testRootQAset(){
		
		setUpKB(null, null, null);
		
		assertEquals("Wrong export (Root should not get exported): ", "", writer.writeText());
	}
	
	

	public void testSingleInitQuestion() {

		String diagnosis = "yellow";
		String initQuestion = "questiongroup 1";
		
		String questions = "questiongroup 1\n " 
			+ "- question 1 [oc]\n "
			+ "-- yellow (P7)";
		
		setUpKB(diagnosis, initQuestion, questions);

		assertEquals("Wrong export: " , "questiongroup 1 [1]\n", writer.writeText());
	}
	

	public void testComplexInitQuestion() {
		
		String diagnosis = "yellow";
		String initQuestion = "questiongroup 1 [1]\n"
			+ "- question 1\n"
		    + "- question 2\n"
		    + "questiongroup 2\n"
		    + "- question 3\n"
		    + "questiongroup 3\n"
		    + "- question 4\n";
		
		String questions = "questiongroup 1\n " 
			+ "- question 1 [oc]\n "
			+ "-- yellow (P7)";
		
		setUpKB(diagnosis, initQuestion, questions);
		
		assertEquals("Wrong export: ", initQuestion, writer.writeText());
		
		String initQuestion2 = "questiongroup 1\n"
			+ "- question 1\n"
		    + "- question 2\n"
		    + "questiongroup 2\n"
		    + "- question 3\n"
		    + "questiongroup 3\n"
		    + "- question 4\n";
		
		setUpKB(diagnosis, initQuestion2, questions);
		
		assertEquals("Wrong export: ", initQuestion, writer.writeText());
		
		questions = "";
		
		setUpKB(diagnosis, initQuestion2, questions);
		
		assertEquals("Wrong export: ", initQuestion2, writer.writeText());
			
		}
	

	private InputStream getStream(String ressource) {
		InputStream stream;
		stream = new ByteArrayInputStream(ressource.getBytes());
		return stream;
	}

	private void setUpKB(String diagnosis, String initQuestion, String questions) {
		initialize();
		
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
		writer = new QClassHierarchyWriter(manager);
	}
	
	public String trim(String string) {
		if (string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}
}
