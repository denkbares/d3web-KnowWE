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

import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.SplitUtility;

public class NumericalIntervallFinding extends D3webCondition<NumericalFinding> {

	public NumericalIntervallFinding() {
		this.setSectionFinder(new NumericalIntervallFinder());

		this.addChildType(new Intervall());
		QuestionReference questionRef = new QuestionReference();
		questionRef.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(questionRef);
	}

	@Override
	protected Condition createCondition(KnowWEArticle article, Section<NumericalFinding> s) {
		Section<QuestionReference> qRef = s.findSuccessor(QuestionReference.class);

		Section<Intervall> intervall = s.findSuccessor(Intervall.class);

		Double number1 = intervall.get().getFirstNumber(intervall);
		Double number2 = intervall.get().getSecondNumber(intervall);

		Question q = qRef.get().getTermObject(article, qRef);

		if (!(q instanceof QuestionNum)) {
			// TODO some reasonable error handling here!
		}

		if (number1 != null && number2 != null && q != null && q instanceof QuestionNum) {
			return new CondNumIn((QuestionNum) q, number1, number2);
		}
		return null;
	}

	class NumericalIntervallFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

			// has to end with ']'
			if (text.trim().endsWith("]")) {
				int bracketsStart = SplitUtility.lastIndexOfUnquoted(text, "[");
				if (bracketsStart == -1) return null;
				// get the content in brackets
				String brackets = text.substring(bracketsStart).trim();
				String content = brackets.substring(1, brackets.length() - 1);

				// find out whether there are exactly 2 chains of characters
				// separated by 1 one more spaces
				String[] nonEmptyParts = SplitUtility.getCharacterChains(content);

				// ..if so, take it all
				if (nonEmptyParts.length == 2) {
					return new AllTextFinderTrimmed().lookForSections(text, father, type);
				}

			}
			return null;
		}

	}

	class Intervall extends DefaultAbstractKnowWEObjectType {

		public Double getFirstNumber(Section<Intervall> s) {
			String text = s.getOriginalText();
			String content = text.substring(1, text.length() - 1);
			String[] parts = SplitUtility.getCharacterChains(content);
			if (parts.length == 2) {
				String firstNumber = parts[0];
				Double d = null;
				try {
					d = Double.parseDouble(firstNumber);
				}
				catch (NumberFormatException f) {

				}
				return d;
			}
			return null;
		}

		public Double getSecondNumber(Section<Intervall> s) {
			String text = s.getOriginalText();
			String content = text.substring(1, text.length() - 1);
			String[] parts = SplitUtility.getCharacterChains(content);
			if (parts.length == 2) {
				String secondNumber = parts[1];
				Double d = null;
				try {
					d = Double.parseDouble(secondNumber);
				}
				catch (NumberFormatException f) {

				}
				return d;
			}
			return null;
		}

		public Intervall() {
			this.setSectionFinder(new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
					if (text.trim().endsWith("]")) {
						int bracketsStart = SplitUtility.lastIndexOfUnquoted(text, "[");
						int bracketsEnd = SplitUtility.lastIndexOfUnquoted(text, "]");

						return SectionFinderResult.createSingleItemList(new SectionFinderResult(
								bracketsStart,
								bracketsEnd + 1));

					}
					return null;
				}
			});
		}
	}

}
