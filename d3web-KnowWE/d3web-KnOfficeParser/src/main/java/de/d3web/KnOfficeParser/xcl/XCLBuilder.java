package de.d3web.KnOfficeParser.xcl;


/**
 * Interface, welches XCL Builder implementieren müssen
 * @author Markus Friedrich
 *
 */
public interface XCLBuilder {
	
	void solution(int line, String linetext, String name);
	void finding(String weight);
	void threshold(int line, String linetext, String type, Double value);
}
