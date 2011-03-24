package de.d3web.we.kdom.rules.action;

import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.SplitUtility;

/**
 * 
 * An action to assign a value to a QuestionNum:
 * Example:
 * Ergebniswert = 5
 * 
 * @author Jochen
 * @created 13.01.2011 
 */
public class SetQuestionNumValueAction extends D3webRuleAction<SolutionValueAssignment> {

		public SetQuestionNumValueAction() {

			this.sectionFinder = new SetNumValueActionSectionFinder();
			de.d3web.we.kdom.basic.Number number = new de.d3web.we.kdom.basic.Number();
			Equals equ = new Equals();
			QuestionReference qRef = new QuestionNumReference();
			this.childrenTypes.add(equ);
			this.childrenTypes.add(number);
			this.childrenTypes.add(qRef);
		qRef.setSectionFinder(new AllBeforeTypeSectionFinder(equ));
		}


		/**
		 * Searches the pattern qNum = <NUMBER>.
		 */
		private class SetNumValueActionSectionFinder implements SectionFinder {


			@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				// check for comparator
				if (SplitUtility.containsUnquoted(text, Equals.SIGN)) {
					// get right hand side of comparator
					int index = SplitUtility.indexOfUnquoted(text,  Equals.SIGN);
					String rightHandSide = text.substring(index + 1).trim();
					try {
					Double.parseDouble(rightHandSide);
					return new AllTextFinderTrimmed().lookForSections(text, father,
							type);
					} catch (Exception e) {
						return null;
					}

				}

				return null;
			}

		}

		@Override
		public PSAction createAction(KnowWEArticle article, Section<SolutionValueAssignment> s) {
		Section<QuestionReference> qRef = Sections.findSuccessor(s, QuestionReference.class);
		Section<de.d3web.we.kdom.basic.Number> numberSec = Sections.findSuccessor(s,
				de.d3web.we.kdom.basic.Number.class);
			if (numberSec == null || qRef == null) return null;

			Question q = qRef.get().getTermObject(article, qRef);
			
		Double num = de.d3web.we.kdom.basic.Number.getNumber(numberSec);
		
			
			if(! (q instanceof QuestionNum)) return null;
			QuestionNum qnum = (QuestionNum)q;
			if (qnum == null || num == null) return null;
			ActionSetValue a = new ActionSetValue();
			a.setQuestion(q);
			a.setValue(new FormulaNumber(num));
			return a;
		}

		@Override
		public Class<? extends PSMethod> getActionPSContext() {
			return PSMethodAbstraction.class;
		}
	

}
