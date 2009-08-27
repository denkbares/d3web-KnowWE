package de.d3web.KnOfficeParser.util;

public interface TerminologyTester {
	
	boolean checkQuestion(String term);
	
	boolean checkAnswer(String question, String answer);

}
