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

package de.d3web.KnOfficeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.d3web.KnOfficeParser.util.ConditionGenerator;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondKnown;
import de.d3web.core.inference.condition.CondMofN;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.CondUnknown;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.report.Message;

/**
 * Klasse um Conditionen in d3web zu erstellen
 * 
 * @author Markus Friedrich
 * 
 */
public class D3webConditionBuilder implements ConditionBuilder {

	private String file;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	private final Stack<Condition> condstack = new Stack<Condition>();
	private List<Message> errors = new ArrayList<Message>();
	private boolean lazy = false;
	private boolean lazyAnswers = false;
	private IDObjectManagement idom;

	private final Map<String, Question> questions = new HashMap<String, Question>();
	private boolean useQuestionmap = true;

	public boolean isUseQuestionmap() {
		return useQuestionmap;
	}

	public void setUseQuestionmap(boolean useQuestionmap) {
		this.useQuestionmap = useQuestionmap;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public boolean isLazyAnswers() {
		return lazyAnswers;
	}

	/**
	 * Sets the lazyAnswerFlag. If lazy is true, answers will be created lazy,
	 * even if this flag is false
	 * 
	 * @param lazyAnswers Flag
	 */
	public void setLazyAnswers(boolean lazyAnswers) {
		this.lazyAnswers = lazyAnswers;
	}

	public D3webConditionBuilder(String file, List<Message> errors,
			IDObjectManagement idom) {
		this.file = file;
		this.errors = errors;
		this.idom = idom;
	}

	public IDObjectManagement getIdom() {
		return idom;
	}

	public void setIdom(IDObjectManagement idom) {
		this.idom = idom;
	}

	public Condition pop() {
		if (!condstack.isEmpty()) {
			return condstack.pop();
		}
		else {
			return null;
		}
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			String op, String value) {
		if (value == null) {
			errors.add(MessageKnOfficeGenerator.createNaNException(file, line,
					linetext, "no value passed to condition_builder"));
			condstack.push(null);
			return;
		}
		Solution diag = idom.findSolution(qname);
		if (diag != null) {
			if (value.equalsIgnoreCase("established")
					|| value.equalsIgnoreCase("etabliert")) {
				condstack.push(new CondDState(diag, new Rating(State.ESTABLISHED)));
			}
			else if (value.equalsIgnoreCase("excluded")
					|| value.equalsIgnoreCase("ausgeschlossen")) {
				condstack.push(new CondDState(diag, new Rating(State.EXCLUDED)));
			}
			else if (value.equalsIgnoreCase("suggested")
					|| value.equalsIgnoreCase("verdächtigt")) {
				condstack.push(new CondDState(diag, new Rating(State.SUGGESTED)));
			}
			// else if
			// (value.equalsIgnoreCase("unclear")||value.equalsIgnoreCase("unklar")){
			// condstack.push(new CondDState(diag, DiagnosisState.UNCLEAR,
			// null));
			// }
			else {
				condstack.push(null);
				errors.add(MessageKnOfficeGenerator.createWrongDiagState(file,
						line, linetext, value));
			}
			return;
		}
		Question question = null;
		if (useQuestionmap) question = this.questions.get(qname);
		if (question == null) {
			question = idom.findQuestion(qname);
			if (question != null) {
				this.questions.put(qname, question);
			}
		}
		if (question == null) {
			if (lazy) {
				if (type != null) {
					question = D3webQuestionFactory.createQuestion(qname, null, type,
							idom);
					if (question == null) {
						errors.add(MessageKnOfficeGenerator
								.createTypeRecognitionError(file, line,
								linetext, qname, type));
						condstack.push(null);
						return;
					}
				}
				else if (op.equals("=")) {
					question = idom.createQuestionOC(qname, idom
							.getKnowledgeBase().getRootQASet(),
							new Choice[0]);
				}
				else {
					question = idom.createQuestionNum(qname, idom
							.getKnowledgeBase().getRootQASet());
				}
			}
			else {
				errors.add(MessageKnOfficeGenerator
						.createQuestionNotFoundException(file, line, linetext,
						qname));
				condstack.push(null);
				return;
			}
		}
		if (!D3webQuestionFactory.checkType(question, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file,
					line, linetext, qname, type));
		}
		TerminalCondition c;
		if (question instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) question;
			double d;
			try {
				d = Double.parseDouble(value);
			}
			catch (NumberFormatException e) {
				errors.add(MessageKnOfficeGenerator.createNaNException(file,
						line, linetext, value));
				condstack.push(null);
				return;
			}
			c = ConditionGenerator.condNum(qnum, op, d, errors, line, value,
					file);
		}
		else if (question instanceof QuestionChoice) {
			Choice answer = null;
			QuestionChoice qc = (QuestionChoice) question;
			if (qc instanceof QuestionYN) {
				QuestionYN qyn = (QuestionYN) qc;
				if ((value.equalsIgnoreCase("ja")) || (value.equalsIgnoreCase("yes"))) {
					answer = qyn.yes;
				}
				else if ((value.equalsIgnoreCase("nein")) || (value.equalsIgnoreCase("no"))) {
					answer = qyn.no;
				}
				else {
					errors.add(MessageKnOfficeGenerator.createWrongYNAnswer(
							file, line, linetext, qyn.getName()));
				}
			}
			else {
				answer = idom.findAnswerChoice(qc, value);
			}
			if (answer == null) {
				if (lazy || lazyAnswers) {
					answer = idom.addChoiceAnswer(qc, value);
					c = new CondEqual(qc, new ChoiceValue(answer));
				}
				else {
					errors.add(MessageKnOfficeGenerator
							.createAnswerNotFoundException(file, line,
							linetext, value, qc.getName()));
					c = null;
				}
			}
			else {
				c = new CondEqual(qc, new ChoiceValue(answer));
			}
		}
		else {
			errors.add(MessageKnOfficeGenerator.createNoAnswerAllowedException(
					file, line, linetext));
			c = null;
		}
		condstack.push(c);
	}

	@Override
	public void condition(int line, String linetext, String qname, String type,
			double left, double right, boolean in) {
		Question question = null;
		if (useQuestionmap) question = this.questions.get(qname);
		if (question == null) {
			question = idom.findQuestion(qname);
			if (question != null) {
				this.questions.put(qname, question);
			}
		}
		if (question == null) {
			if (lazy) {
				if (type != null) {
					question = D3webQuestionFactory.createQuestion(qname, null, type,
							idom);
					if (question == null) {
						errors.add(MessageKnOfficeGenerator
								.createTypeRecognitionError(file, line,
								linetext, qname, type));
						condstack.push(null);
						return;
					}
				}
				else {
					question = idom.createQuestionNum(qname, idom
							.getKnowledgeBase().getRootQASet());
				}
			}
			else {
				errors.add(MessageKnOfficeGenerator
						.createQuestionNotFoundException(file, line, linetext,
						qname));
				condstack.push(null);
				return;
			}
		}
		if (!D3webQuestionFactory.checkType(question, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file,
					line, linetext, qname, type));
		}
		TerminalCondition c;
		if (question instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) question;
			c = ConditionGenerator.condNum(qnum, left, right, errors, line,
					linetext, file);
		}
		else {
			errors.add(MessageKnOfficeGenerator.createIntervallQuestionError(
					file, line, linetext));
			c = null;
		}
		condstack.push(c);
	}

	@Override
	public void knowncondition(int line, String linetext, String name,
			String type, boolean unknown) {
		Question q = idom.findQuestion(name);

		// create q if not exists and lazy enabled
		if (q == null && lazy) {
			if (type != null) {
				q = D3webQuestionFactory.createQuestion(name, null, type, idom);
				if (q == null) {
					errors.add(MessageKnOfficeGenerator
							.createTypeRecognitionError(file, line,
							linetext, name, type));
					condstack.push(null);
					return;
				}
			}
			else {
				q = idom.createQuestionOC(name, idom.getKnowledgeBase()
						.getRootQASet(), new Choice[0]);
			}
		}

		// only add condition if question has been found or created lazy
		if (q == null) {
			// if q not exists, create an error
			errors.add(MessageKnOfficeGenerator
					.createQuestionNotFoundException(file, line, linetext,
					name));
		}
		else {
			if (!D3webQuestionFactory.checkType(q, type)) {
				errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file,
						line, linetext, name, type));
			}
			TerminalCondition c;
			if (unknown) {
				c = new CondUnknown(q);
			}
			else {
				c = new CondKnown(q);
			}
			condstack.add(c);
		}
	}

	@Override
	public void notcond(String text) {
		if (!condstack.isEmpty()) {
			Condition cond = condstack.pop();
			if (cond != null) {
				condstack.push(new CondNot(cond));
			}
			else {
				condstack.push(null);
			}
		}
	}

	@Override
	public void andcond(String text) {
		if (condstack.size() > 1) {
			List<Condition> clist = new ArrayList<Condition>();
			Condition cond = condstack.pop();
			Condition cond2 = condstack.pop();
			if (cond != null && cond2 != null) {
				clist.add(cond);
				clist.add(cond2);
				condstack.push(new CondAnd(clist));
			}
			else {
				condstack.push(null);
			}
		}
	}

	@Override
	public void orcond(String text) {
		if (condstack.size() > 1) {
			List<Condition> clist = new ArrayList<Condition>();
			Condition cond = condstack.pop();
			Condition cond2 = condstack.pop();
			if (cond != null && cond2 != null) {
				clist.add(cond);
				clist.add(cond2);
				condstack.push(new CondOr(clist));
			}
			else {
				condstack.push(null);
			}
		}
	}

	@Override
	public void minmax(int line, String linetext, int min, int max,
			int anzahlcond) {
		List<Condition> condlist = new ArrayList<Condition>();
		boolean failure = false;
		for (int i = 0; i < anzahlcond; i++) {
			Condition cond = condstack.pop();
			condlist.add(cond);
			if (cond == null) {
				failure = true;
			}
		}
		if (!condlist.isEmpty()) {
			if (failure) {
				condstack.push(null);
			}
			else {
				condstack.push(new CondMofN(condlist, min, max));
			}
		}
		else {
			errors.add(MessageKnOfficeGenerator.createNoValidCondsException(
					file, line, linetext));
			condstack.push(null);
		}
	}

	@Override
	public void in(int line, String linetext, String question, String type,
			List<String> answers) {
		List<Condition> conds = condList(line, linetext, question,
				type, answers);
		if (conds.contains(null)) {
			condstack.push(null);
		}
		else {
			condstack.push(new CondOr(conds));
		}
	}

	@Override
	public void all(int line, String linetext, String question, String type,
			List<String> answers) {
		List<Condition> conds = condList(line, linetext, question,
				type, answers);
		if (conds.contains(null)) {
			condstack.push(null);
		}
		else {
			condstack.push(new CondAnd(conds));
		}
	}

	private List<Condition> condList(int line, String linetext,
			String question, String type, List<String> answers) {
		List<Condition> conds = new ArrayList<Condition>();
		for (String s : answers) {
			condition(line, linetext, question, type, "=", s);
			conds.add(condstack.pop());
		}
		return conds;
	}

	@Override
	public void complexcondition(String text) {
		// wird für d3web nicht benötigt
	}
}
