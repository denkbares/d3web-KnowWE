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
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.SplitUtility;
import de.knowwe.kdom.renderer.StyleRenderer;

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
		Section<QuestionReference> qRef = Sections.findSuccessor(s, QuestionReference.class);

		Section<Intervall> intervall = Sections.findSuccessor(s, Intervall.class);

		Double number1 = intervall.get().getFirstNumber(intervall);
		Double number2 = intervall.get().getSecondNumber(intervall);

		Question q = qRef.get().getTermObject(article, qRef);

		if (!(q instanceof QuestionNum)) {
			Messages.storeMessage(article, s, this.getClass(), Messages.error(
					"The question '" + qRef.get().getTermIdentifier(qRef) + "' must be numerical."));
		}
		else if (number1 != null && number2 != null && q != null && q instanceof QuestionNum) {
			Messages.clearMessages(article, s, this.getClass());
			return new CondNumIn((QuestionNum) q, number1, number2);
		}
		return null;
	}

	class NumericalIntervallFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

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

	class Intervall extends AbstractType {

		public Double getFirstNumber(Section<Intervall> s) {
			String text = s.getText();
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
			String text = s.getText();
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
			this.setCustomRenderer(StyleRenderer.NUMBER);
			this.setSectionFinder(new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
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
