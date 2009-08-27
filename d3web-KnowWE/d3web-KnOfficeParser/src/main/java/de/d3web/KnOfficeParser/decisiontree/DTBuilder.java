package de.d3web.KnOfficeParser.decisiontree;
import java.util.List;

/**
 * Dieses Interface muss implementiert werden, um die Klasse dem Entscheidungsbaumparser mitzugeben
 * und somit den Output zu erzeugen
 * @author Markus Friedrich
 *
 */
public interface DTBuilder {
	
	void newLine();
	
	void line(String text);
	
	void addQuestionclass(String name, int line, String linetext, List<String> attributes, List<String> values);
	
	void addQuestion(int dashes, String name, String longname, boolean abs, String type, String ref, Double lowerbound, Double upperbound, String unit, List<String> syn, int line, String linetext, String idlink, List<String> attributes, List<String> values);
	
	void addAnswerOrQuestionLink(int dashes, String name, String ref, List<String> syn, boolean def, int line, String linetext, String idlink);
	
	void addDiagnosis(int dashes, List<String> diags, boolean set, String value, String link, String linkdes, int line, String linetext, String idlink);
	
	void addNumericAnswer(int dashes, Double a, Double b, String op, int line, String linetext);
	
	void addDescription(String id, String type, String des, String text, int line, String linetext, String language);
	
	void addInclude(String url, int line, String linetext);
	
	void addQuestionLink(int dashes, String name, int line, String linetext);

	void setallowedNames(List<String> allowedNames, int line, String linetext);
	
	public void finishOldQuestionsandConditions(int dashes);

	void addManyQuestionClassLink(int dashes, List<String> qcs, int line,
			String string);

}
