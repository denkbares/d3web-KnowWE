package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.formula.FormulaNumberElement;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.kdom.action.formula.CompositeFormula;
import de.d3web.we.kdom.action.formula.FormulaNumberElementFactory;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

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
		this.setSectionFinder(new SetQuestionValueSectionFinder());
		Equals equals = new Equals();
		QuestionNumReference qr = new QuestionNumReference();
		qr.setSectionFinder(new AllBeforeTypeSectionFinder(equals));
		this.addChildType(equals);
		this.addChildType(qr);

		CompositeFormula formula = new CompositeFormula();
		// crate List of valid terminals
		List<Type> terminals = new ArrayList<>();
		// terminals may either be numbers...
		de.knowwe.core.kdom.basicType.Number number = new de.knowwe.core.kdom.basicType.Number();
		terminals.add(number);
		// or QuestionNums..
		QuestionNumReference qref = new QuestionNumReference();
		qref.setSectionFinder(new AllTextFinderTrimmed());
		terminals.add(qref);
		// set these terminals
		formula.setAllowedTerminalConditions(terminals);
		this.addChildType(formula);
	}

	private class SetQuestionValueSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			// take if right side starts with '('
			if (Strings.containsUnquoted(text, "=")) {
				int indexOfEquals = Strings.indexOfUnquoted(text, "=");
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
	protected PSAction createAction(D3webCompiler compiler, Section<SetQuestionValue> s) {
		Section<QuestionReference> qref = Sections.successor(s, QuestionReference.class);
		Question q = qref.get().getTermObject(compiler, qref);
		Section<CompositeFormula> formulaSection = Sections.successor(s, CompositeFormula.class);
		if (formulaSection == null) return null;
		FormulaNumberElement formular = null;
		try {
			formular = FormulaNumberElementFactory.createExpression(
					compiler, formulaSection);
		}
		catch (Exception e) {
			Log.severe("Unexpected error", e);
			Messages.storeMessage(compiler, s, this.getClass(), Messages.error("Could not create FormulaNumberElement: "+e.getMessage()));
		}

		if (q != null && formular != null) {
			ActionSetQuestion a = new ActionSetQuestion();
			a.setQuestion(q);
			a.setValue(formular);
			return a;
		}
		return null;
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodAbstraction.class;
	}
}
