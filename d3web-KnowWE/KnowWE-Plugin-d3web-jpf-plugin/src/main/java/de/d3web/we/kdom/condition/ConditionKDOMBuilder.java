/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.condition;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.KnOfficeParser.ConditionBuilder;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.condition.old.Conjunct;
import de.d3web.we.kdom.rules.RuleCondLine;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;

/**
 * Klasse um den KDOM Tree mithilfe des ANTLR Parsers zu Erstellen
 * 
 * @author Markus Friedrich
 * 
 */
public class ConditionKDOMBuilder implements ConditionBuilder {

	private final Stack<ExpandedSectionFinderResult> sections = new Stack<ExpandedSectionFinderResult>();

	@Override
	public void all(int line, String linetext, String question, String type,
			List<String> answers) {
		if (linetext == null) return;

	}

	@Override
	public void andcond(String text) {
		if (text == null) return;
		if (sections.size() >= 2) {
			ExpandedSectionFinderResult disjunct = new ExpandedSectionFinderResult(text,
					new Conjunct(), -1);
			ExpandedSectionFinderResult second = sections.pop();
			disjunct.addChild(sections.pop());

			Pattern andPattern = Pattern.compile("( +AND +)" + Pattern.quote(second.getText()));
			Matcher m = andPattern.matcher(text);
			String and = " AND ";
			int offset = 0;
			if (m.find()) {
				and = m.group(1);
				offset = m.start();
			}

			disjunct.addChild(new ExpandedSectionFinderResult(and, new AndOperator(), offset));
			disjunct.addChild(second);
			sections.push(disjunct);
		}
	}

	@Override
	public void orcond(String text) {
		if (text == null) return;
		if (sections.size() >= 2) {
			ExpandedSectionFinderResult cf = new ExpandedSectionFinderResult(text,
					new ComplexFinding(), -1);
			ExpandedSectionFinderResult second = sections.pop();
			cf.addChild(sections.pop());

			Pattern orPattern = Pattern.compile("( +OR +)" + Pattern.quote(second.getText()));
			Matcher m = orPattern.matcher(text);
			String or = " OR ";
			int offset = 0;
			if (m.find()) {
				or = m.group(1);
				offset = m.start();
			}

			cf.addChild(new ExpandedSectionFinderResult(or, new OrOperator(), offset));
			cf.addChild(second);
			sections.push(cf);
		}
	}

	@Override
	public void complexcondition(String text) {
		if (sections.size() == 0) {
			return;
		}
		String content = sections.peek().getText();
		Pattern bracedPattern = Pattern.compile("( *\\( *)(" + Pattern.quote(content)
				+ ")( *\\) *)");
		Matcher m = bracedPattern.matcher(text);
		if (m.find()) {
			ExpandedSectionFinderResult bracedCond = new ExpandedSectionFinderResult(text,
					new ComplexFindingBraced(), -1);
			bracedCond.addChild(new ExpandedSectionFinderResult(m.group(1),
					new ConditionBracketOpen(), m.start(1)));
			ExpandedSectionFinderResult cond = sections.pop();
			bracedCond.addChild(cond);
			bracedCond.addChild(new ExpandedSectionFinderResult(m.group(3),
					new ConditionBracketClose(), m.start(3)));
			sections.push(bracedCond);
		}
	}

	public ExpandedSectionFinderResult peek() {
		if (sections.size() == 0) return null;
		return sections.peek();
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			String op, String value) {
		if (linetext == null) return;
		ExpandedSectionFinderResult cond = new ExpandedSectionFinderResult(linetext, new Finding(),
				-1);
		sections.add(cond);
		if (qname == null || op == null || value == null) return;

		Pattern condPattern = Pattern.compile("\\A(\"?" + Pattern.quote(qname) + "\"?)( +"
				+ Pattern.quote(op)
				+ " +)(\"?" + Pattern.quote(value) + "\"?)");
		Matcher m = condPattern.matcher(linetext);

		int offset1 = 1;
		int offset2 = 2;
		int offset3 = 3;

		if (m.find()) {
			offset1 = m.start(1);
			qname = m.group(1);

			offset2 = m.start(2);
			op = m.group(2);

			offset3 = m.start(3);
			value = m.group(3);
		}

		cond.addChild(new ExpandedSectionFinderResult(qname, new FindingQuestion(), offset1));
		cond.addChild(new ExpandedSectionFinderResult(op, new FindingComparator(), offset2));
		cond.addChild(new ExpandedSectionFinderResult(value, new FindingAnswer(), offset3));
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			double left, double right, boolean in) {

		ExpandedSectionFinderResult cond = new ExpandedSectionFinderResult(linetext,
				new RuleCondLine(), -1);
		sections.add(cond);

		int offset1 = 1;

		int offset2 = 2;
		String inString = " IN ";

		int offset3 = 3;
		String bracketOpen = "[";

		int offset4 = 4;
		String bracketClose = "]";

		int offset5 = 5;
		String leftBorder = trimZeros(Double.toString(left));

		int offset6 = 6;
		String rightBorder = trimZeros(Double.toString(right));

		Pattern condPattern = Pattern.compile("\\A( *\"" + Pattern.quote(qname)
				+ "\")( +IN +)?(\\[ *)("
				+ leftBorder + " +)(" + rightBorder + " *)(\\] *)");
		Matcher m = condPattern.matcher(linetext);

		if (m.find()) {
			offset1 = m.start(1);
			qname = m.group(1);

			if (m.group(2) != null) {
				offset2 = m.start(2);
				inString = m.group(2);
			}

			offset3 = m.start(3);
			bracketOpen = m.group(3);

			offset4 = m.start(4);
			leftBorder = m.group(4);

			offset5 = m.start(5);
			rightBorder = m.group(5);

			offset6 = m.start(6);
			bracketClose = m.group(6);

		}

		cond.addChild(new ExpandedSectionFinderResult(qname, new FindingQuestion(), offset1));

		// TODO: Bedeutung von type? Überflüssig?
		// if (type!=null) Section.createExpandedSection(type, null, cond,
		// fatherindex++, topic, null, null, idgen);

		if (in) cond.addChild(new ExpandedSectionFinderResult(inString, new IN(), offset2));
		cond.addChild(new ExpandedSectionFinderResult(bracketOpen, new IntervallBracketOpen(),
				offset3));
		cond.addChild(new ExpandedSectionFinderResult(leftBorder, new IntervallLeftBorderValue(),
				offset4));
		cond.addChild(new ExpandedSectionFinderResult(rightBorder, new IntervallRightBorderValue(),
				offset5));
		cond.addChild(new ExpandedSectionFinderResult(bracketClose, new IntervallBracketClose(),
				offset6));
	}

	@Override
	public void in(int line, String linetext, String question, String type,
			List<String> answers) {
		if (linetext == null) return;

	}

	@Override
	public void knowncondition(int line, String linetext, String name,
			String type, boolean unknown) {
		if (name == null) return;
		ExpandedSectionFinderResult s = new ExpandedSectionFinderResult(linetext, new CondKnown(),
				-1);
		s.addChild(new ExpandedSectionFinderResult(
				linetext.substring(0, linetext.indexOf("[") + 1), new CondKnownSyntax(), 1));
		Pattern q = Pattern.compile("\"?" + Pattern.quote(name) + "\"?");
		Matcher m = q.matcher(linetext);
		if (m.find()) {
			name = m.group();
		}
		s.addChild(new ExpandedSectionFinderResult(name, new FindingQuestion(), 2));
		s.addChild(new ExpandedSectionFinderResult("]", new CondKnownSyntax(), 3));
		sections.push(s);

	}

	@Override
	public void minmax(int line, String linetext, int min, int max,
			int anzahlcond) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notcond(String text) {
		if (text == null) return;

		if (sections.size() >= 1) {
			String not = "NOT ";
			Matcher m = Pattern.compile(" *(NOT|NICHT) *").matcher(text);
			if (m.find()) {
				not = m.group();
			}
			ExpandedSectionFinderResult notCond = new ExpandedSectionFinderResult(text,
					new NegatedFinding(), -1);
			notCond.addChild(new ExpandedSectionFinderResult(not, new NOT(), 0));
			notCond.addChild(sections.pop());
			sections.push(notCond);
		}
	}

	private String trimZeros(String s) {
		s = s.replaceAll("\\.0", "");
		return s;
	}

}
