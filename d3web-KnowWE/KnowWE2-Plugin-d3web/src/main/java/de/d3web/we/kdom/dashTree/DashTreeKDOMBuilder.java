package de.d3web.we.kdom.dashTree;

import java.util.List;
import java.util.Stack;

import de.d3web.KnOfficeParser.dashtree.DashTBuilder;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.Section;


public interface DashTreeKDOMBuilder extends DashTBuilder {

	public void reInit();

	public Section peek();

	public void setSections(Stack<Section> sections);

	public void setTopic(String topic);

	public String getTopic();

	public void setIdgen(IDGenerator idgen);
	
	public Stack<Section> getSections();

	@Override
	public void line(String text);
	
	@Override
	public void finishOldQuestionsandConditions(int dashes);

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext);	

	@Override
	public void newLine();

	@Override
	public void addNode(int dashes, String name, int line, String description, int order);

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext);

	@Override
	public void addInclude(String url, int line, String linetext);

}
