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

import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;
import de.knowwe.kdom.sectionFinder.OneOfStringUnquotedFinder;

/**
 * A type implementing a cond-num TerminalCondition {@link TerminalCondition} It
 * has a allowed list of comparators
 * 
 * syntax: <questionID> <comp> <number> e.g.: mileage evaluation >= 130
 * 
 * @author Jochen
 * 
 */
public class NumericalFinding extends D3webCondition<NumericalFinding> {

	private static String[] comparators = {
			"<=", ">=", "==", "=", "<", ">", };

	public NumericalFinding() {
		this.setSectionFinder(new NumericalFindingFinder());

		// comparator
		Comparator comparator = new Comparator();
		comparator.setSectionFinder(new OneOfStringUnquotedFinder(comparators));
		comparator.setRenderer(new StyleRenderer(StyleRenderer.OPERATOR, MaskMode.htmlEntities));
		this.addChildType(comparator);

		// question
		QuestionReference question = new QuestionNumReference();
		ConstraintSectionFinder questionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		question.setSectionFinder(questionFinder);
		this.addChildType(question);

		// answer
		Number num = new Number();
		num.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(num);
	}

	class NumericalFindingFinder implements SectionFinder {

		private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			for (String comp : comparators) {
				if (Strings.containsUnquoted(text, comp)) {

					return textFinder.lookForSections(text,
							father, type);
				}
			}

			return null;
		}

	}

	@Override
	protected Condition createCondition(D3webCompiler compiler, Section<NumericalFinding> s) {
		Section<QuestionReference> qRef = Sections.successor(s, QuestionReference.class);

		Section<Number> numberSec = Sections.successor(s, Number.class);

		if (numberSec == null) {
			Message error = Messages.invalidNumberError(
					"No number on right side of comparator.");
			Messages.storeMessage(compiler, s, getClass(), error);
			return null;
		}

		String comparator = Sections.successor(s, Comparator.class).getText();

		Double number = Number.getNumber(numberSec);

		if (number == null) {
			Message error = Messages.invalidNumberError(
					numberSec.getText());
			Messages.storeMessage(compiler, numberSec, getClass(), error);
			return null;
		}

		Question q = qRef.get().getTermObject(compiler, qRef);

		if (!(q instanceof QuestionNum)) {
			// TODO some reasonable error handling here!
			return null;
		}

		if (number != null && q != null && q instanceof QuestionNum) {

			return createCondNum(comparator, number, q);

		}
		return null;
	}

	/**
	 * 
	 * @created 08.08.2013
	 * @param comparator
	 * @param number
	 * @param q
	 */
	public static CondNum createCondNum(String comparator, Double number, Question q) {
		QuestionNum qnum = (QuestionNum) q;

		if (comparator.equals("<=")) {
			return new CondNumLessEqual(qnum, number);
		}
		else if (comparator.equals(">=")) {
			return new CondNumGreaterEqual(qnum, number);
		}
		else if (comparator.equals("<")) {
			return new CondNumLess(qnum, number);
		}
		else if (comparator.equals(">")) {
			return new CondNumGreater(qnum, number);
		}
		else if (comparator.equals("==")) {
			return new CondNumEqual(qnum, number);
		}
		else if (comparator.equals("=")) {
			return new CondNumEqual(qnum, number);
		}
		return null;
	}

	/**
	 * Helper class allowing to search the KDOM for sections of this type
	 * 
	 * @author Jochen
	 * @created 26.10.2010
	 */
	public static class Comparator extends AbstractType {
	}
}
