package de.d3web.KnOfficeParser.xcl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;

/**
 * Einfache Testklasse für den XCL Parser
 * @author Markus Friedrich
 *
 */
public class BasicTester {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException, RecognitionException {
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		File file = new File("examples\\modelle - edited.txt");
		SingleKBMIDObjectManager idom = new SingleKBMIDObjectManager(kbm);
		XCLd3webBuilder builder = new XCLd3webBuilder(file.toString(),true, true, idom);
		builder.setCreateUncompleteFindings(false);
		List<Message> errors=(List<Message>) builder.addKnowledge(new FileReader(file), idom, null);
		for (Message m: errors) {
			System.out.println(m);
		}
	}

}
