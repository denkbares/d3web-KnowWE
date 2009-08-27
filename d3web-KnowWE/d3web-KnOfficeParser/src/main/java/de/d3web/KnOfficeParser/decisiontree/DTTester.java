package de.d3web.KnOfficeParser.decisiontree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;


/**
 * Testklasse um den DecisionTree2Parser zu testen
 * @author Markus Friedrich
 *
 */
public class DTTester {

		
	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("examples\\testbogen.txt");
		D3DTBuilder builder = new D3DTBuilder(file.toString(), new SingleKBMIDObjectManager(null));
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		kbm.createDiagnosis("Rheumaerkrankung eher wahrscheinlich", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Rheumaerkrankung möglich", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Rheumaerkrankung eher unwahrscheinlich", kbm.getKnowledgeBase().getRootDiagnosis());
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
		List<Message> errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
	}
}
