package de.d3web.KnOfficeParser.dashtree;
import java.util.List;

/**
 * @author Alex Legler
 */
public interface DashTBuilder {
	void newLine();
	
	void line(String text);

	void addDescription(String id, String type, String des, String text, int line, String linetext);
	
	void addInclude(String url, int line, String linetext);

	void setallowedNames(List<String> allowedNames, int line, String linetext);
	
	public void finishOldQuestionsandConditions(int dashes);

	void addNode(int dashes, String name, int line, String description, int order);

}
