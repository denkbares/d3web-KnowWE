package de.d3web.KnOfficeParser.table;
/**
 * Interface, welches Builder implementieren müssen, die vom TableParser ausgerufen werden sollen
 * @author Markus Friedrich
 *
 */
public interface Builder {
	void addKnowledge(String question, String answer, String solution, String value, int line, int column);
	
	void setQuestionClass(String name, int line, int column);

	void addXlsError();

	void addNoDiagsError(int startrow);

	void addNoQuestionError(int i, int j);
}
