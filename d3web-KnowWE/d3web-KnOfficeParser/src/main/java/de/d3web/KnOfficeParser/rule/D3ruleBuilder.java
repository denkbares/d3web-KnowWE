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

package de.d3web.KnOfficeParser.rule;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.D3webConditionBuilder;
import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.KnOfficeParser.util.Scorefinder;
import de.d3web.abstraction.formula.FormulaElement;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.formula.FormulaNumberElement;
import de.d3web.abstraction.formula.Operator;
import de.d3web.abstraction.formula.Operator.Operation;
import de.d3web.abstraction.formula.QNumWrapper;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.report.Message;
import de.d3web.scoring.Score;

/**
 * Adapterklasse um d3 Regeln zu erstellen
 * 
 * @author Markus Friedrich
 * 
 */
public class D3ruleBuilder implements KnOfficeParser, RuleBuilder {

	private final String file;
	private final List<Message> errors = new ArrayList<Message>();
	private Question currentquestion;
	private Solution currentdiag;
	private final Stack<FormulaNumberElement> formulaStack = new Stack<FormulaNumberElement>();
	private final D3webConditionBuilder cb;
	private boolean lazy = false;
	private boolean buildonlywith0Errors = false;
	private int rulecount;
	private List<MyRule> rules = new ArrayList<MyRule>();
	private final List<String> ruleIDs = new ArrayList<String>();
	private IDObjectManagement idom;

	private enum ruletype {
		indication, instantindication, contraindication, supress, setvalue, addvalue, heuristic,
	}

	private class MyRule {

		private final ruletype type;
		private Question question;
		private final Condition ifcond;
		private final Condition exceptcond;
		private Value value;
		private FormulaElement formula;
		private ArrayList<QASet> qcons;
		private Score score;
		private Solution diag;

		public MyRule(ruletype type, Question question,
				Condition ifcond, Condition exceptcond,
				Value answers, FormulaElement formula,
				ArrayList<QASet> qcons) {
			super();
			this.type = type;
			this.question = question;
			this.ifcond = ifcond;
			this.exceptcond = exceptcond;
			this.value = answers;
			this.formula = formula;
			this.qcons = qcons;
		}

		public MyRule(Solution diag, Score score, Condition ifcond,
				Condition exceptcond) {
			super();
			this.diag = diag;
			this.score = score;
			this.ifcond = ifcond;
			this.exceptcond = exceptcond;
			type = ruletype.heuristic;
		}

	}

	private void addRule(MyRule rule) {
		if (buildonlywith0Errors) {
			rules.add(rule);
		}
		else {
			generateRule(rule);
		}
	}

	private Rule generateRule(MyRule rule) {
		String newRuleID = idom.createRuleID();
		Rule newRule = null;
		if (rule.type == ruletype.indication) {
			Condition cond = rule.ifcond;
			if (cond instanceof CondDState) {
				CondDState statecond = (CondDState) cond;
				if (statecond.getStatus().hasState(State.ESTABLISHED)) {
					newRule = RuleFactory.createRefinementRule(newRuleID, rule.qcons,
							statecond.getSolution(), statecond,
							rule.exceptcond);
				}
				else if (statecond.getStatus().hasState(State.SUGGESTED)) {
					newRule = RuleFactory.createClarificationRule(newRuleID, rule.qcons,
							statecond.getSolution(), statecond,
							rule.exceptcond);
				}
				else {
					newRule = RuleFactory.createIndicationRule(newRuleID, rule.qcons,
							cond, rule.exceptcond);
				}
			}
			else {
				newRule = RuleFactory.createIndicationRule(newRuleID, rule.qcons, cond,
						rule.exceptcond);
			}
		}
		else if (rule.type == ruletype.instantindication) {
			newRule = RuleFactory.createInstantIndicationRule(newRuleID, rule.qcons,
					rule.ifcond, rule.exceptcond);
		}
		else if (rule.type == ruletype.contraindication) {
			newRule = RuleFactory.createContraIndicationRule(newRuleID, rule.qcons,
					rule.ifcond, rule.exceptcond);
		}
		else if (rule.type == ruletype.supress) {
			Choice[] theAnswers = null;
			if (rule.value instanceof MultipleChoiceValue) {
				Collection<?> col = (Collection<?>) rule.value.getValue();
				List<Choice> choices = new LinkedList<Choice>();
				for (Object o : col) {
					choices.add((Choice) o);
				}
				theAnswers = choices.toArray(new Choice[choices.size()]);
			}
			else {
				theAnswers = new Choice[] { (Choice) rule.value.getValue() };
			}
			newRule = RuleFactory.createSuppressAnswerRule(newRuleID,
					(QuestionChoice) rule.question, theAnswers, rule.ifcond,
					rule.exceptcond);
		}
		else if (rule.type == ruletype.setvalue) {
			if (rule.formula != null) {
				newRule = RuleFactory.createSetValueRule(newRuleID, rule.question,
						rule.formula, rule.ifcond, rule.exceptcond);
			}
			else if (rule.value != null) {
				newRule = RuleFactory.createSetValueRule(newRuleID, rule.question,
						rule.value, rule.ifcond, rule.exceptcond);
			}
			else {
				// TODO add error message
			}
		}
		else if (rule.type == ruletype.addvalue) {
			if (rule.formula != null) {
				// TODO add factory method so support addValue for formulas
				// RuleFactory.createAddValueRule(newRuleID, rule.question,
				// rule.formula, rule.ifcond, rule.exceptcond);
			}
			else if (rule.value != null) {
				newRule = RuleFactory.createSetValueRule(newRuleID, rule.question,
						rule.value, rule.ifcond, rule.exceptcond);
			}
			else {
				// TODO add error message
			}
		}
		else {
			newRule = RuleFactory.createHeuristicPSRule(newRuleID, rule.diag, rule.score,
					rule.ifcond, rule.exceptcond);
		}
		if (newRule != null) {
			ruleIDs.add(newRule.getId());
		}

		rulecount++;
		return newRule;
	}

	private void generateSavedRules() {
		for (MyRule r : rules) {
			generateRule(r);
		}
		rules = new ArrayList<MyRule>();
	}

	private void finish() {
		if (errors.size() == 0) {
			generateSavedRules();
			errors.add(MessageKnOfficeGenerator.createRulesFinishedNote(file,
					rulecount));
		}
	}

	public List<String> getRuleIDs() {
		return this.ruleIDs;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
		cb.setLazy(lazy);
	}

	public boolean isBuildonlywith0Errors() {
		return buildonlywith0Errors;
	}

	public void setBuildonlywith0Errors(boolean buildonlywith0Errors) {
		this.buildonlywith0Errors = buildonlywith0Errors;
	}

	public D3ruleBuilder(String file, boolean lazy, IDObjectManagement idom) {
		this.file = file;
		this.idom = idom;
		cb = new D3webConditionBuilder(file, errors, idom);
		this.lazy = lazy;
		cb.setLazy(lazy);
	}

	public List<Message> getErrors() {
		return errors;
	}

	@Override
	public void indicationrule(int line, String linetext, List<String> names,
			List<String> types, boolean except, boolean instant, boolean not) {
		// Herraussuchen der Frageblätter/Fragen
		ArrayList<QASet> qcons = new ArrayList<QASet>();
		int i = 0;
		String type;
		for (String s : names) {
			type = types.get(i);
			QASet qcon = idom.findQContainer(s);
			if (qcon != null) {
				qcons.add(qcon);
			}
			else if (!instant) {
				qcon = idom.findQuestion(s);
				if (qcon != null) {
					qcons.add(qcon);
					if (!D3webQuestionFactory.checkType((Question) qcon, type)) {
						errors.add(MessageKnOfficeGenerator
								.createTypeMismatchWarning(file, line,
										linetext, s, type));
					}
				}
				else {
					if (lazy) {
						if (type != null) {
							qcon = D3webQuestionFactory.createQuestion(s, null, type,
									idom);
							if (qcon != null) {
								qcons.add(qcon);
							}
							else {
								errors.add(MessageKnOfficeGenerator
										.createTypeRecognitionError(file, line,
												linetext, s, type));
							}
						}
						else {
							qcons.add(idom.createQContainer(s, idom
									.getKnowledgeBase().getRootQASet()));
						}
					}
					else {
						errors
								.add(MessageKnOfficeGenerator
										.createQuestionClassorQuestionNotFoundException(
												file, line, linetext, s));
					}
				}
			}
			else {
				if (lazy) {
					qcons.add(idom.createQContainer(s, idom.getKnowledgeBase()
							.getRootQASet()));
				}
				else {
					errors.add(MessageKnOfficeGenerator
							.createQuestionClassNotFoundException(file, line,
									linetext, s));
				}
			}
			i++;
		}
		if (!qcons.isEmpty()) {
			Condition ifcond;
			Condition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null) return;
			}
			else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null) return;
			ruletype rtype;
			if (!instant && !not) {
				rtype = ruletype.indication;
			}
			else if (instant) {
				rtype = ruletype.instantindication;
			}
			else {
				rtype = ruletype.contraindication;
			}
			addRule(new MyRule(rtype, null, ifcond, exceptcond, null, null,
					qcons));
		}
		else {
			errors.add(MessageKnOfficeGenerator
					.createNoValidQuestionsException(file, line, linetext));
			finishCondstack(except);
		}
	}

	@Override
	public void suppressrule(int line, String linetext, String qname,
			String type, List<String> anames, boolean except) {
		Question q = idom.findQuestion(qname);
		if (!D3webQuestionFactory.checkType(q, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file,
					line, linetext, qname, type));
		}
		if (q == null) {
			if (lazy) {
				if (type != null) {
					q = D3webQuestionFactory.createQuestion(qname, null, type, idom);
				}
				else {
					q = idom.createQuestionOC(qname, idom.getKnowledgeBase()
							.getRootQASet(), new Choice[0]);
				}
			}
			else {
				errors.add(MessageKnOfficeGenerator
						.createQuestionNotFoundException(file, line, linetext,
								qname));
				finishCondstack(except);
			}
		}
		else {
			if (q instanceof QuestionChoice) {
				QuestionChoice qc = (QuestionChoice) q;
				ArrayList<Value> alist = new ArrayList<Value>();
				for (String s : anames) {
					Value a = idom.findValue(qc, s);
					if (a != null) {
						alist.add(a);
					}
					else {
						errors.add(MessageKnOfficeGenerator
								.createAnswerNotFoundException(file, line,
										linetext, s, qc.getName()));
					}
				}
				if (alist.size() >= 2) {

					errors.add(new Message("Rule expects to suppress exactly 1 answer on question "
							+ qc.getName()));
					return;
				}

				if (!alist.isEmpty()) {
					Condition ifcond;
					Condition exceptcond;
					if (except) {
						exceptcond = cb.pop();
						ifcond = cb.pop();
						if (exceptcond == null) return;
					}
					else {
						ifcond = cb.pop();
						exceptcond = null;
					}
					if (ifcond == null) return;

					addRule(new MyRule(ruletype.supress, qc, ifcond,
							exceptcond, alist.get(0), null, null));
				}
				else {
					errors
							.add(MessageKnOfficeGenerator
									.createNoValidAnswerException(file, line,
											linetext));
					finishCondstack(except);
				}
			}
			else {
				errors.add(MessageKnOfficeGenerator.createSupressError(file,
						line, linetext));
				finishCondstack(except);
			}
		}
	}

	@Override
	public void numValue(int line, String linetext, boolean except, String op) {
		if (currentquestion == null) {
			finishCondstack(except);
			return;
		}
		if (currentquestion instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) currentquestion;

			FormulaElement formula;
			if (op.equals("=")) {
				formula = formulaStack.pop();
			}
			else if (op.equals("+=")) {
				formula = new Operator(new QNumWrapper(qnum),
						formulaStack.pop(), Operation.Add);
			}
			else {
				formula = null;
				errors.add(MessageKnOfficeGenerator
						.createWrongOperatorInAbstractionRule(file, line,
								linetext));
			}
			Condition ifcond;
			Condition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null) return;
			}
			else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null) return;
			addRule(new MyRule(ruletype.setvalue, qnum, ifcond, exceptcond,
					null, formula, null));
		}
	}

	@Override
	public void questionOrDiagnosis(int line, String linetext, String s,
			String type) {
		currentquestion = idom.findQuestion(s);
		if (currentquestion == null) {
			currentdiag = idom.findSolution(s);
			if (currentdiag == null) {
				if (lazy) {
					if (type != null) {
						currentquestion = D3webQuestionFactory.createQuestion(s, null,
								type, idom);
					}
					else {
						currentquestion = D3webQuestionFactory.createQuestion(s, null,
								"oc", idom);
					}
				}
				else {
					errors.add(MessageKnOfficeGenerator
							.createQuestionOrSolutionNotFoundException(file,
									line, linetext, s));
				}
			}
		}
		else {
			currentdiag = null;
			if (!D3webQuestionFactory.checkType(currentquestion, type)) {
				errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(
						file, line, linetext, s, type));
			}
			if (!((currentquestion instanceof QuestionNum) || (currentquestion instanceof QuestionChoice))) {
				errors
						.add(MessageKnOfficeGenerator
								.createOnlyNumOrChoiceAllowedError(file, line,
										linetext));
			}
		}
	}

	@Override
	public void choiceOrDiagValue(int line, String linetext, String op,
			String value, boolean except) {
		if (currentquestion == null) {
			if (currentdiag == null) {
				finishCondstack(except);
				return;
			}
			else {
				if (!op.equals("=")) {
					errors.add(MessageKnOfficeGenerator
							.createWrongOperatorForDiag(file, line, linetext));
					finishCondstack(except);
					return;
				}
				else {
					Score score = Scorefinder.getScore(value);
					if (score == null) {
						errors.add(MessageKnOfficeGenerator
								.createScoreDoesntExistError(file, line,
										linetext, value));
						finishCondstack(except);
						return;
					}
					else {
						Condition ifcond;
						Condition exceptcond;
						if (except) {
							exceptcond = cb.pop();
							ifcond = cb.pop();
							if (exceptcond == null) return;
						}
						else {
							ifcond = cb.pop();
							exceptcond = null;
						}
						if (ifcond == null) return;
						addRule(new MyRule(currentdiag, score, ifcond,
								exceptcond));
					}
				}
			}
		}
		if (currentquestion instanceof QuestionChoice) {
			boolean add;
			if (op.equals("=")) {
				add = false;
			}
			else if (op.equals("+=")) {
				add = true;
			}
			else {
				errors.add(MessageKnOfficeGenerator
						.createWrongOperatorforChoiceQuestionsException(file,
								line, linetext));
				finishCondstack(except);
				return;
			}
			QuestionChoice qc = (QuestionChoice) currentquestion;
			Value answer = idom.findValue(qc, value);

			if (answer == null) { // answer is not present
				if (lazy) { // create it if lazy
					idom.addChoiceAnswer(qc, value);

					// try finding the value again with the new answer present
					answer = idom.findValue(qc, value);
				}
				else {
					errors.add(MessageKnOfficeGenerator
							.createAnswerNotFoundException(file, line,
									linetext, value, qc.getName()));
					finishCondstack(except);
					return;
				}
			}

			Condition ifcond;
			Condition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null) return;
			}
			else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null) return;
			if (add) {
				addRule(new MyRule(ruletype.addvalue, currentquestion, ifcond,
						exceptcond, answer, null, null));

			}
			else {
				addRule(new MyRule(ruletype.setvalue, currentquestion, ifcond,
						exceptcond, answer, null, null));
			}
		}
	}

	@Override
	public void formula(int line, String linetext, String value) {
		Question q = idom.findQuestion(value);
		FormulaNumberElement num;
		if (q != null) {
			if (q instanceof QuestionNum) {
				QuestionNum qnumValue = (QuestionNum) q;
				num = new QNumWrapper(qnumValue);
			}
			else {
				num = null;
				errors.add(MessageKnOfficeGenerator
						.createOnlyNumInFormulaError(file, line, linetext));
			}
		}
		else {
			Double d = null;
			try {
				d = Double.parseDouble(value);
			}
			catch (NumberFormatException e) {
				if (lazy) {
					q = idom.createQuestionNum(value, idom.getKnowledgeBase()
							.getRootQASet());
				}
				else {
					errors.add(MessageKnOfficeGenerator
							.createOnlyNumOrDoubleError(file, line, linetext));
					formulaStack.push(null);
					return;
				}
			}
			if (d != null) {
				num = new FormulaNumber(d);
			}
			else {
				num = new QNumWrapper((QuestionNum) q);
			}
		}
		formulaStack.push(num);
	}

	@Override
	public void formulaAdd() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Operator(f1, f2, Operation.Add));
	}

	@Override
	public void formulaSub() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Operator(f1, f2, Operation.Sub));
	}

	@Override
	public void formulaMult() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Operator(f1, f2, Operation.Mult));
	}

	@Override
	public void formulaDiv() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Operator(f1, f2, Operation.Div));
	}

	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		this.idom = idom;
		cb.setIdom(idom);
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
		try {
			istream = new ANTLRInputStream(input);
		}
		catch (IOException e1) {
			errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0,
					""));
		}
		DefaultLexer lexer = new DefaultLexer(istream,
				new DefaultD3webLexerErrorHandler(errors, file));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Complexrules parser = new Complexrules(tokens, this,
				new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"),
				cb);
		try {
			parser.knowledge();
		}
		catch (RecognitionException e) {
			e.printStackTrace();
		}
		finish();
		return errors;
	}

	@Override
	public List<Message> checkKnowledge() {
		finish();
		return errors;
	}

	private void finishCondstack(boolean except) {
		cb.pop();
		if (except) cb.pop();
	}
}
