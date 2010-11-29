package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.formula.FormulaNumberElement;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.kdom.rules.action.formula.CompositeFormula;
import de.d3web.we.kdom.rules.action.formula.KDOMFormulaNumberElementFactory;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.SplitUtility;

/**
 * An type for an action which calculates the value of a QuestionNum by a
 * formula
 * 
 * 
 * @author Jochen
 * @created 16.10.2010
 */
public class SetQNumFormulaAction extends D3webRuleAction<SetQuestionValue> {

	public SetQNumFormulaAction() {
		this.sectionFinder = new SetQuestionValueSectionFinder();
		Equals equals = new Equals();
		QuestionReference qr = new QuestionReference();
		qr.setSectionFinder(AllBeforeTypeSectionFinder.createFinder(equals));
		this.childrenTypes.add(equals);
		this.childrenTypes.add(qr);

		CompositeFormula formula = new CompositeFormula();
		// crate List of valid terminals
		List<KnowWEObjectType> terminals = new ArrayList<KnowWEObjectType>();
		// terminals may either be numbers...
		de.d3web.we.kdom.condition.Number number = new de.d3web.we.kdom.condition.Number();
		terminals.add(number);
		// or QuestionNums..
		QuestionNumReference qref = new QuestionNumReference();
		qref.setSectionFinder(new AllTextFinderTrimmed());
		terminals.add(qref);
		// set these terminals
		formula.setAllowedTerminalConditions(terminals);
		this.childrenTypes.add(formula);
	}

	private class SetQuestionValueSectionFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

			// take if right side starts with '('
			if (SplitUtility.containsUnquoted(text, "=")) {
				int indexOfEquals = SplitUtility.indexOfUnquoted(text, "=");
				String rightSide = text.substring(indexOfEquals + 1).trim();
				if (rightSide.startsWith("(")) {
					return new AllTextFinderTrimmed().lookForSections(
							text, father, type);
				}
			}

			return null;
		}
	}

	@Override
	protected PSAction createAction(KnowWEArticle article, Section s) {
		Section<QuestionReference> qref = s.findSuccessor(QuestionReference.class);
		Question q = qref.get().getTermObject(article, qref);
		Section<CompositeFormula> formulaSection = s.findSuccessor(CompositeFormula.class);
		if (formulaSection == null) return null;
		FormulaNumberElement formular = null;
		try {
			formular = KDOMFormulaNumberElementFactory.createExpression(
					article, formulaSection);
		}
		catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage());
			e.printStackTrace();
		}

		if (q != null && formular != null) {
			ActionSetValue a = new ActionSetValue();
			a.setQuestion(q);
			a.setValue(formular);
			return a;
		}
		return null;
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodAbstraction.class;
	}
}
