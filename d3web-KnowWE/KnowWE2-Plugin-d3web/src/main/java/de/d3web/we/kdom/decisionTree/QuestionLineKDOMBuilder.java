package de.d3web.we.kdom.decisionTree;

import java.util.List;
import java.util.Stack;

import de.d3web.KnOfficeParser.decisiontree.DTBuilder;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.LineBreak;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnaireLine;
import de.d3web.we.kdom.dashTree.solutions.SolutionLine;
import de.d3web.we.kdom.kopic.Dashes;

public class QuestionLineKDOMBuilder implements DTBuilder {

	private Stack<Section> sections = new Stack<Section>();

	private String topic;

	private IDGenerator idgen;

	public QuestionLineKDOMBuilder(String topic, IDGenerator idgen) {
		this.topic = topic;
		this.idgen = idgen;
	}

	public void reInit() {
		sections = new Stack<Section>();
	}

	@Override
	public void addAnswerOrQuestionLink(int dashes, String name, String ref,
			List<String> syn, boolean def, int line, String linetext,
			String idlink) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		Section lineS = Section.createExpandedSection(da + linetext + "\r\n", new AnswerLine(), null, -1, topic, null,
				null, idgen);
		Section.createExpandedSection(da, new Dashes(), lineS, 0, topic, null, null, idgen);
		Section.createExpandedSection(linetext, new AnswerDef(), lineS, da.length(), topic, 
				null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, (da + linetext).length(), topic, null, null, idgen);

		sections.push(lineS);

	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext, String language) {
		Section lineS = Section.createExpandedSection(linetext + "\r\n",
				new DescriptionLine(), null, sections.size() * (-1), topic, null,
				null, idgen);
		Section.createExpandedSection(linetext, new Description(), null, 0, topic, null,
				null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, linetext.length(), topic, null, null, idgen);

		//TODO language?
		sections.push(lineS);
	}

	public static String makeDashes(int k) {
		StringBuilder dashes = new StringBuilder();
		for (int i = 0; i < k; i++) {
			dashes .append("-");
		}
		return dashes.toString() + " ";
	}

	@Override
	public void addDiagnosis(int dashes, List<String> diags, boolean set,
			String value, String link, String linkdes, int line,
			String linetext, String idlink) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		Section lineS = Section.createExpandedSection(da
				+ linetext + "\r\n", new SolutionLine(), null, sections.size()
				* (-1), topic, null, null, idgen);
		Section.createExpandedSection(da, new Dashes(), lineS, 0,
				topic, null, null, idgen);
		Section.createExpandedSection(linetext, new SolutionID(), lineS, da.length(),
				topic, null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, (da + linetext).length(),
				topic, null, null, idgen);

		sections.push(lineS);
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		String blubb = "bla";
		blubb += "";
	}

	@Override
	public void addManyQuestionClassLink(int dashes, List<String> qcs,
			int line, String string) {
		String blubb = "bla";
		blubb += "";
	}

	@Override
	public void addNumericAnswer(int dashes, Double a, Double b, String op,
			int line, String linetext) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		Section lineS = Section.createExpandedSection(da + linetext + "\r\n", 
				new NumericCondLine(), null, sections.size()
				* (-1), topic, null, null, idgen);
		Section.createExpandedSection(da, new Dashes(), lineS, 0,
				topic, null, null, idgen);
		Section.createExpandedSection(linetext, new NumericCond(), lineS, da.length(),
				topic, null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, (da + linetext).length(),
				topic, null, null, idgen);

		sections.push(lineS);
	}

	@Override
	public void addQuestion(int dashes, String name, String longname,
			boolean abs, String type, String ref, Double lowerbound,
			Double upperbound, String unit, List<String> syn, int line,
			String linetext, String idlink, List<String> attributes,
			List<String> values) {
		if (linetext == null)
			return;
		
		String da = makeDashes(dashes);
		Section lineS = Section.createExpandedSection(da
				+ linetext + "\r\n", new QuestionDefLine(), null, sections.size()
				* (-1), topic, null, null, idgen);
		Section.createExpandedSection(da, new Dashes(), lineS, 0,
				topic, null, null, idgen);
		Section.createExpandedSection(linetext, new QuestionDef(), lineS, da.length(), topic, null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, (da + linetext).length(),
				topic, null, null, idgen);
		sections.push(lineS);

	}

	@Override
	public void addQuestionLink(int dashes, String name, int line,
			String linetext) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		Section lineS = Section.createExpandedSection(da + linetext + "\r\n",
				new QuestionReferenceLine(), null, sections.size() * (-1), topic,
				null, null, idgen);
		Section.createExpandedSection(da, new Dashes(), lineS, 0,
				topic, null, null, idgen);
		Section.createExpandedSection(linetext, new QuestionReference(), lineS, da.length(),
				topic, null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, (da + linetext).length(),
				topic, null, null, idgen);
		sections.push(lineS);

	}

	@Override
	public void addQuestionclass(String name, int line, String linetext,
			List<String> attributes, List<String> values) {
		if (linetext == null)
			return;
		Section lineS = Section.createExpandedSection(linetext + "\r\n",
				new QuestionnaireLine(), null, sections.size() * (-1), topic, null,
				null, idgen);
		Section.createExpandedSection(linetext, new QClassID(), lineS, 0, topic, null, null, idgen);
		Section.createExpandedSection("\r\n", new LineBreak(), lineS, linetext.length(), topic, null,
				null, idgen);
		

		sections.push(lineS);
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
		String blubb = "bla";
		blubb += "";
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
		String blubb = "bla";
		blubb += "";
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

}
