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

import de.d3web.knowledgeExporter.txtWriters.QClassHierarchyWriter;


public class QClassHierarchyExportTest extends KnowledgeExporterTest {

	private QClassHierarchyWriter writer;

	public void testRootQAset(){
		this.setUpKB(new String(), null, null, null, null);
		this.setUpKB("", "", "", "", "");
		assertEquals("Wrong export (Root should not get exported): ", "", writer.writeText());
	}
	
	

	public void testSingleInitQuestion() {

		String diagnosis = "yellow";
		String initQuestion = "questiongroup 1 [1]";
		
//		String questions = "questiongroup 1\n " 
//			+ "- question 1 [oc]\n "
//			+ "-- yellow (P7)";
//		
		setUpKB(diagnosis, initQuestion, null, null, null);

		assertEquals("Wrong export: " , initQuestion + "\n", writer.writeText());
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
		
		setUpKB(diagnosis, initQuestion, questions, null, null);
		
		assertEquals("Wrong export: ", initQuestion, writer.writeText());
		
		String initQuestion2 = "questiongroup 1\n"
			+ "- question 1\n"
		    + "- question 2\n"
		    + "questiongroup 2\n"
		    + "- question 3\n"
		    + "questiongroup 3\n"
		    + "- question 4\n";
		
		setUpKB(diagnosis, initQuestion2, questions, null, null);
		
		assertEquals("Wrong export: ", initQuestion2, writer.writeText());
		
		questions = "";
		
		setUpKB(diagnosis, initQuestion2, questions, null, null);
		
		assertEquals("Wrong export: ", initQuestion2, writer.writeText());
			
		}

//	private void setUpKB(String diagnosis, String initQuestion, String questions) {
//		initialize();
//		
//		TextParserResource ressource;
//
//		if (questions != null) {
//			ressource = new TextParserResource(getStream(questions));
//			input.put(KBTextInterpreter.QU_DEC_TREE, ressource);
//		}
//		if (diagnosis != null) {
//			ressource = new TextParserResource(getStream(diagnosis));
//			input.put(KBTextInterpreter.DH_HIERARCHY, ressource);
//		}
//		if (initQuestion != null) {
//			ressource = new TextParserResource(getStream(initQuestion));
//			input.put(KBTextInterpreter.QCH_HIERARCHY, ressource);
//		}
//
//		output = kbTxtInterpreter.interpreteKBTextReaders(input, "JUnit-KB",
//				false, false);
//		kb = kbTxtInterpreter.getKnowledgeBase();
//		
//		setUpWriter();
//	}
//	
//	public void initialize() {
//		kbTxtInterpreter = new KBTextInterpreter();
//		input = new HashMap<String, TextParserResource>();
//		output = new HashMap<String, Report>();
//	}
	
	@Override
	protected void setUpWriter() {
		writer = new QClassHierarchyWriter(manager);
	}
	
	public String trim(String string) {
		if (string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}
}
