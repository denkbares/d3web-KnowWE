package de.d3web.KnOfficeParser;

import java.util.List;

/**
 * Interface, welches implementiert werden muss, um Conditionen aus Grammatiken zu erzeugen, welche ComplexCondition.g importieren
 * @author Markus Friedrich
 *
 */
public interface ConditionBuilder {
	void condition(int line, String linetext, String qname, String type, String op, String value);
	void condition(int line, String linetext, String qname, String type, double left, double right, boolean in);
	void knowncondition(int line, String linetext, String name, String type, boolean unknown);
	void notcond(String text);
	void andcond(String text);
	void orcond(String text);
	void minmax(int line, String linetext, int min, int max, int anzahlcond);
	void in(int line, String linetext, String question, String type, List<String> answers);
	void all(int line, String linetext, String question, String type, List<String> answers);
	void complexcondition(String text);
	
	
}
