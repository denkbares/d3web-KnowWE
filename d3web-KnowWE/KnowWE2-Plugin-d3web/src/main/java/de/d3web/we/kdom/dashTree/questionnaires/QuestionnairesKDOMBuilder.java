package de.d3web.we.kdom.dashTree.questionnaires;

import java.util.List;
import java.util.Stack;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.LineBreak;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.DashTreeKDOMBuilder;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.decisionTree.AnswerLine;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.kdom.kopic.Dashes;

public class QuestionnairesKDOMBuilder implements DashTreeKDOMBuilder {
	private Stack<Section> sections = new Stack<Section>();
	private String topic;
	private IDGenerator idgen;

	public QuestionnairesKDOMBuilder(String topic, IDGenerator idgen) {
		this.topic = topic;
		this.idgen = idgen;
	}

	public void reInit() {
		sections = new Stack<Section>();
	}

	public static String makeDashes(int k) {
		StringBuilder dashes = new StringBuilder();
		for (int i = 0; i < k; i++) {
			dashes.append("-");
		}
		return dashes.toString() + (k > 0 ? " " : "");
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
		throw new AssertionError("should not be called");
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
		Section newLine = Section.createExpandedSection("\n", new TextLine(),
				null, sections.size() * (-1), topic, null, null, idgen);
		
		sections.push(newLine);
	}

	@Override
	public void addNode(int dashes, String name, int line, String description, int order) {
		
		String da = makeDashes(dashes);
		String linetext = name + (description != null ? " ~ " + description : "") + 
						  (order != 0 ? " [" + order + "]" : "") + "\r\n";
		
		Section qsection = Section.createExpandedSection(da + linetext, new QuestionnaireLine(), 
				null, sections.size() * (-1), topic, null, null, idgen);
		
		if (dashes > 0) {
			Section.createExpandedSection(da, new Dashes(), qsection, 0, topic, null, null, idgen);
		}
		
		Section.createExpandedSection(name, new QClassID(), qsection, da.length(), topic, null, null, idgen);

		
		if (description != null) {
			Section.createExpandedSection(" ~", new Tilde(), qsection,
					getOffset(qsection), topic, null, null, idgen);
			
			Section.createExpandedSection(" " + description, new QClassDescription(), qsection,
					getOffset(qsection), topic, null, null, idgen);
		}
		
		if (order != 0) {
			Section.createExpandedSection(" [" + order + "]", new QClassOrder(), qsection,
					getOffset(qsection), topic, null, null, idgen);
		}
		
		Section.createExpandedSection("\r\n", new LineBreak(), qsection,
				getOffset(qsection), topic, null, null, idgen);
	
		sections.push(qsection);
				
	}
	
	private int getOffset(Section father) {
		int i = 0;
		for (Section child:father.getChildren()) {
			i += child.getOriginalText().length();
		}
		return i;
	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		throw new AssertionError("should not be called");
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		throw new AssertionError("should not be called");
	}

}
