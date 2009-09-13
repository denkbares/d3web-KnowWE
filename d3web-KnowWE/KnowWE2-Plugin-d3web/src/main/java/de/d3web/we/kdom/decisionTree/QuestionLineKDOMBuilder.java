/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.decisionTree;

import java.util.List;
import java.util.Stack;

import de.d3web.KnOfficeParser.decisiontree.DTBuilder;
import de.d3web.we.kdom.LineBreak;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.Dashes;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnaireLine;
import de.d3web.we.kdom.dashTree.solutions.SolutionLine;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;

public class QuestionLineKDOMBuilder implements DTBuilder {

	private Stack<ExpandedSectionFinderResult> sections = new Stack<ExpandedSectionFinderResult>();

	public QuestionLineKDOMBuilder() {
	}

	public void reInit() {
		sections = new Stack<ExpandedSectionFinderResult>();
	}

	@Override
	public void addAnswerOrQuestionLink(int dashes, String name, String ref,
			List<String> syn, boolean def, int line, String linetext,
			String idlink) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(da + linetext + "\r\n", new AnswerLine(), -1);
		lineS.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new AnswerDef(), da.length()));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), (da + linetext).length()));

		sections.push(lineS);

	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext, String language) {
		
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(linetext + "\r\n",
				new DescriptionLine(), sections.size() * (-1));
		
		//TODO: Not sure if this child is needed since it wasn't hooked in the KDOM before the interface change
		//lineS.addChild(new ExpandedSectionFinderResult(linetext, new Description(), 0));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), linetext.length()));

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
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(da
				+ linetext + "\r\n", new SolutionLine(), sections.size());
		lineS.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new SolutionID(), da.length()));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), (da + linetext).length()));

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
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(da + linetext + "\r\n", 
				new NumericCondLine(), sections.size()* (-1));
		lineS.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new NumericCond(), da.length()));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), (da + linetext).length()));

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
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(da
				+ linetext + "\r\n", new QuestionDefLine(), sections.size() * (-1));
		lineS.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new QuestionDef(), da.length()));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), (da + linetext).length()));
		sections.push(lineS);

	}

	@Override
	public void addQuestionLink(int dashes, String name, int line,
			String linetext) {
		if (linetext == null)
			return;
		String da = makeDashes(dashes);
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(da + linetext + "\r\n",
				new QuestionReferenceLine(), sections.size() * (-1));
		lineS.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new QuestionReference(), da.length()));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), (da + linetext).length()));
		sections.push(lineS);

	}

	@Override
	public void addQuestionclass(String name, int line, String linetext,
			List<String> attributes, List<String> values) {
		if (linetext == null)
			return;
		ExpandedSectionFinderResult lineS = new ExpandedSectionFinderResult(linetext + "\r\n",
				new QuestionnaireLine(), sections.size() * (-1));
		lineS.addChild(new ExpandedSectionFinderResult(linetext, new QClassID(), 0));
		lineS.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(), linetext.length()));
		

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

	public ExpandedSectionFinderResult peek() {
		if (sections.size() == 0)
			return null;
		return sections.peek();
	}

	public void setSections(Stack<ExpandedSectionFinderResult> sections) {
		this.sections = sections;
	}

//	public void setTopic(String topic) {
//		this.topic = topic;
//	}
//
//	public String getTopic() {
//		return topic;
//	}
//
//	public void setIdgen(IDGenerator idgen) {
//		this.idgen = idgen;
//	}

	@Override
	public void line(String text) {

	}

	public Stack<ExpandedSectionFinderResult> getSections() {
		return sections;
	}

	@Override
	public void newLine() {
		sections.push(new ExpandedSectionFinderResult("\r\n", new TextLine(), sections.size() * (-1)));

	}

}
