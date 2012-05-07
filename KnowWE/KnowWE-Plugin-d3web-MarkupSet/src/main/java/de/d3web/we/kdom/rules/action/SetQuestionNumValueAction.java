package de.d3web.we.kdom.rules.action;

import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.UnknownValueType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * 
 * An action to assign a value to a QuestionNum: Example: Ergebniswert = 5
 * 
 * @author Jochen
 * @created 13.01.2011
 */
public class SetQuestionNumValueAction extends D3webRuleAction<SolutionValueAssignment> {

	public SetQuestionNumValueAction() {

		this.sectionFinder = new SetNumValueActionSectionFinder();
		de.knowwe.core.kdom.basicType.Number number = new de.knowwe.core.kdom.basicType.Number();
		Equals equ = new Equals();
		QuestionReference qRef = new QuestionNumReference();
		this.childrenTypes.add(equ);
		this.childrenTypes.add(new UnknownValueType());
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
			if (Strings.containsUnquoted(text, Equals.SIGN)) {
				// get right hand side of comparator
				int index = Strings.indexOfUnquoted(text, Equals.SIGN);
				String rightHandSide = text.substring(index + 1).trim();
				try {
					Double.parseDouble(rightHandSide);
					return new AllTextFinderTrimmed().lookForSections(text, father,
							type);
				}
				catch (Exception e) {
					return null;
				}

			}

			return null;
		}

	}

	@Override
	public PSAction createAction(Article article, Section<SolutionValueAssignment> s) {
		Object value;

		if (Sections.findSuccessor(s, UnknownValueType.class) != null) {
			value = Unknown.getInstance();
		}
		else {
			Section<de.knowwe.core.kdom.basicType.Number> numberSec = Sections
					.findSuccessor(s, de.knowwe.core.kdom.basicType.Number.class);

			if (numberSec == null) return null;

			value = new FormulaNumber(
					de.knowwe.core.kdom.basicType.Number.getNumber(numberSec));
		}

		Section<QuestionReference> qRef = Sections.findSuccessor(s, QuestionReference.class);

		if (qRef == null) return null;

		Question q = qRef.get().getTermObject(article, qRef);

		if (!(q instanceof QuestionNum)) return null;
		QuestionNum qnum = (QuestionNum) q;

		if (qnum == null || value == null) return null;

		ActionSetValue a = new ActionSetValue();
		a.setQuestion(q);
		a.setValue(value);

		return a;
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodAbstraction.class;
	}

}
