package de.d3web.KnOfficeParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.decisiontree.D3DTBuilder;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;


/**
 * Testklasse um den DecisionTree2Parser zu testen
 * @author Markus Friedrich
 *
 */
public class Tester {

		
	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("examples\\car.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		D3DTBuilder builder = new D3DTBuilder(file.toString(), new SingleKBMIDObjectManager(kbm));
		kbm.createDiagnosis("Leerlaufsystem defekt", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Ansaugsystem undicht", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Luftfiltereinsatz verschmutzt", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Zündeinstellung falsch", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Batterie leer", kbm.getKnowledgeBase().getRootDiagnosis());
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
		List<Message> errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
		file = new File("examples\\carrule.txt");
		D3ruleBuilder builder2 = new D3ruleBuilder(file.toString(), false, new SingleKBMIDObjectManager(kbm));
		builder2.setLazy(true);
//		builder2.setBuildonlywith0Errors(true);
		r = new FileReader(file);
		col = builder2.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
		errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
	}
}
