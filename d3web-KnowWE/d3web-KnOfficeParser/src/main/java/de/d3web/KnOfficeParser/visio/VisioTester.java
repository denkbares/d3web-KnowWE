package de.d3web.KnOfficeParser.visio;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import de.d3web.report.Message;


public class VisioTester {

//	private String calc(String s, double d) {
//		Double d2=Double.parseDouble(s)-d;
//		return ""+d2;
//	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, RecognitionException {
		FileReader reader = new FileReader("examples\\Beispiel.vdx");
		VisioParserCaller caller = new VisioParserCaller("examples\\Beispiel.vdx");
		Collection<Message> col = caller.addKnowledge(reader, null, null);
		List<Message> errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
		caller.writeToFile("examples\\Beispiel.xml");
	}

}
