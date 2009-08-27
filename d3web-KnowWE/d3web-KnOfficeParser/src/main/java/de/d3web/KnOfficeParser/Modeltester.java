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
import de.d3web.KnOfficeParser.xcl.XCLd3webBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;


/**
 * Testklasse um den DecisionTree2Parser zu testen
 * @author Markus Friedrich
 *
 */
public class Modeltester {

		
	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("examples\\models\\Entscheidungsbaum.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		SingleKBMIDObjectManager idom = new SingleKBMIDObjectManager(kbm);
		D3DTBuilder builder = new D3DTBuilder(file.toString(), idom);
		kbm.createDiagnosis("Schwanger", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Übergewicht", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Normalgewicht", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Gliederschmerzen", kbm.getKnowledgeBase().getRootDiagnosis());
		kbm.createDiagnosis("Verteilte Schmerzen", kbm.getKnowledgeBase().getRootDiagnosis());
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, idom, null);
		List<Message> errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
		file = new File("examples\\models\\komplexeRegeln.txt");
		D3ruleBuilder builder2 = new D3ruleBuilder(file.toString(), false, idom);
//		builder2.setLazy(true);
//		builder2.setBuildonlywith0Errors(true);
		r = new FileReader(file);
		col = builder2.addKnowledge(r, idom, null);
		errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
		file = new File("examples\\models\\XCL.txt");
		XCLd3webBuilder builder3 = new XCLd3webBuilder(file.toString(),false, false, idom);
		builder3.setCreateUncompleteFindings(false);
		errors=(List<Message>) builder3.addKnowledge(new FileReader(file), idom, null);
		for (Message m: errors) {
			System.out.println(m);
		}
	}
}
