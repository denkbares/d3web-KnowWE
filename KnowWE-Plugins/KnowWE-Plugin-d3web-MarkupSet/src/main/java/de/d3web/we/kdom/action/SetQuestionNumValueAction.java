package de.d3web.we.kdom.action;

import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.session.values.Unknown;
import com.denkbares.strings.Strings;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.UnknownValueType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
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

		this.setSectionFinder(new SetNumValueActionSectionFinder());
		de.knowwe.core.kdom.basicType.Number number = new de.knowwe.core.kdom.basicType.Number();
		Equals equ = new Equals();
		QuestionReference qRef = new QuestionNumReference();
		this.addChildType(equ);
		this.addChildType(new UnknownValueType());
		this.addChildType(number);
		this.addChildType(qRef);
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
	public PSAction createAction(D3webCompiler compiler, Section<SolutionValueAssignment> s) {
		Object value;

		if (Sections.successor(s, UnknownValueType.class) != null) {
			value = Unknown.getInstance();
		}
		else {
			Section<de.knowwe.core.kdom.basicType.Number> numberSec = Sections
					.successor(s, de.knowwe.core.kdom.basicType.Number.class);

			if (numberSec == null) return null;

			value = new FormulaNumber(
					de.knowwe.core.kdom.basicType.Number.getNumber(numberSec));
		}

		Section<QuestionReference> qRef = Sections.successor(s, QuestionReference.class);

		if (qRef == null) return null;

		Question q = qRef.get().getTermObject(compiler, qRef);

		if (!(q instanceof QuestionNum)) return null;
		QuestionNum qnum = (QuestionNum) q;

		if (qnum == null || value == null) return null;

		ActionSetQuestion a = new ActionSetQuestion();
		a.setQuestion(q);
		a.setValue(value);

		return a;
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodAbstraction.class;
	}

}
