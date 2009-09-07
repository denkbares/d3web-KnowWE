package de.d3web.we.kdom.dashTree.solutions;

import java.util.List;
import java.util.Stack;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.DashTreeKDOMBuilder;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.decisionTree.SolutionID;

public class SolutionsKDOMBuilder implements DashTreeKDOMBuilder {

	private Stack<Section> sections = new Stack<Section>();

	private String topic;

	private IDGenerator idgen;

	public SolutionsKDOMBuilder(String topic, IDGenerator idgen) {
		this.topic = topic;
		this.idgen = idgen;
	}

	public void reInit() {
		sections = new Stack<Section>();
	}

	public static String makeDashes(int k) {
		String dashes = "";
		for (int i = 0; i < k; i++) {
			dashes += "-";
		}

		return dashes + " ";
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
	}

	public Section peek() {
		if (sections.size() == 0)
			return null;
		return sections.peek();
	}

	public void setSections(Stack<Section> sections) {
		this.sections = sections;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

	public void setIdgen(IDGenerator idgen) {
		this.idgen = idgen;
	}

	@Override
	public void line(String text) {

	}

	public Stack<Section> getSections() {
		return sections;
	}

	@Override
	public void newLine() {
		
		Section newLine = Section.createExpandedSection("\r\n", new TextLine(),
				null, sections.size() * (-1), topic, null, null, idgen);
		
		sections.push(newLine);

	}

	@Override
	public void addNode(int dashes, String name, int line, String description, int order) {
			
			// Generate SolutionLine Object
			Section father = Section.createExpandedSection(((dashes != 0) ? makeDashes(dashes) : "" ) +
							 name + ((description != null) ? " ~ " + description : "" ) + "\r\n", 
							 new SolutionLine(), null, sections.size() * (-1), topic, null, null, idgen);
			
			sections.push(father);
			
			// Generate Child for each SolutionLine-Element
			generateSection(dashes, name, line, description, order, father);

	}

	private void generateSection(int dashes, String name, int line, String description, int order, Section father) {
		
		Section s;
		
		if (dashes == 0) { // root diagnosis (not P000!)
			
			s = Section.createExpandedSection(name + ((description == null) ? "\r\n" : "" ),
				new RootSolution(), father, sections.size() * (-1), topic, null, null, idgen);
			
		} else { // normal diagnosis
			
			s = Section.createExpandedSection(makeDashes(dashes) + name + 
				((description == null) ? "\r\n" : "" ),	new SolutionID(), 
				father, sections.size() * (-1), topic, null, null, idgen);			
			
		}
		
		if (description != null) { // save tilde and description
 			
			// Tilde ("~")
			s = Section.createExpandedSection(" ~ ", new Tilde(), father, 
				sections.size() * (-1), topic, null, null, idgen);	
			
			// Description			
			s = Section.createExpandedSection(description + "\r\n",	new SolutionDescription(), 
				father, sections.size() * (-1), topic, null, null, idgen);	
			
		}
		
	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		// not necessary in this builder!
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		// TODO Auto-generated method stub
		
	}

}
