package de.d3web.KnOfficeParser.scmcbr;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;

/**
 * Einfache Testklasse für den SCMCBR Parser
 * @author Markus Friedrich
 *
 */
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("examples\\coveringnew.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		D3SCMCBRBuilder builder = new D3SCMCBRBuilder(file.toString(), new SingleKBMIDObjectManager(kbm));
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
		List<Message> errors = (List<Message>) col;
		for (Message m : errors) {
			System.out.println(m);
		}
	}

}
