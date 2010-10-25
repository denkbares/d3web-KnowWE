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

package de.d3web.KnOfficeParser.table;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.report.Message;

/**
 * Builder um d3web Wissen mithilfe des TableParsers zu generieren
 * 
 * @author Markus Friedrich
 * 
 */
public class D3webBuilder implements Builder, KnOfficeParser {

	private boolean lazy = false;
	private boolean lazydiag = false;
	private final List<Message> errors = new ArrayList<Message>();
	private QContainer currentqclass;
	private final String file;
	private Question currentquestion;
	private Choice currentanswer;
	private final CellKnowledgeBuilder ckb;
	private TableParser tb;
	private int counter = 0;
	private int errorcount = 0;
	private IDObjectManagement idom;

	public D3webBuilder(String file, CellKnowledgeBuilder ckb, IDObjectManagement idom) {
		this(file, ckb, 0, 0, idom);
	}

	public D3webBuilder(String file, CellKnowledgeBuilder ckb, int startcolumn, int startrow, IDObjectManagement idom) {
		this(file, ckb, startcolumn, startrow, null, idom);
		tb = new TableParser(this, startcolumn, startrow);
	}

	public D3webBuilder(String file, CellKnowledgeBuilder ckb, int startcolumn, int startrow, TableParser tbin, IDObjectManagement idom) {
		this.file = file;
		this.ckb = ckb;
		this.idom = idom;
		tb = tbin;
		tb.startcolumn = startcolumn;
		tb.startrow = startrow;
		tb.builder = this;
	}

	private void finish() {
		if (errors.size() == 0) {
			errors.add(MessageKnOfficeGenerator.createXLSFileParsed(file, counter));
		}
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public void setLazyDiag(boolean lazy) {
		this.lazydiag = lazy;
	}

	@Override
	public void addKnowledge(String question, String answer, String solution,
			String value, int line, int column) {
		counter++;
		errorcount = errors.size();
		if (answer != null) answer = answer.trim();
		boolean typedef = false;
		String type = "";
		if (question.endsWith("]")) {
			type = question.substring(question.lastIndexOf('[') + 1, question.length() - 1);
			if (type.equals("y/n")) {
				type = "yn";
			}
			question = question.substring(0, question.lastIndexOf('[')).trim();
			typedef = true;
		}
		if (currentquestion == null || !currentquestion.getName().equals(question)) {
			currentquestion = idom.findQuestion(question);
			if (currentquestion == null) {
				if (lazy) {
					if (currentqclass == null) {
						currentqclass = (QContainer) idom.getKnowledgeBase().getRootQASet();
					}
					if (typedef) {
						currentquestion = D3webQuestionFactory.createQuestion(idom, currentqclass,
								question, null, type);
					}
					else if (answer == null) {
						currentquestion = idom.createQuestionYN(question, currentqclass);
					}
					else if (answer.startsWith("<") || answer.startsWith("[")
							|| answer.startsWith(">") || answer.startsWith("=")) {
						currentquestion = idom.createQuestionNum(question, currentqclass);
					}
					else {
						currentquestion = idom.createQuestionOC(question, currentqclass,
								new Choice[0]);
					}
				}
				else {
					errors.add(MessageKnOfficeGenerator.createQuestionNotFoundException(file, line,
							column, "", question));
					return;
				}
			}
			else {
				if (!D3webQuestionFactory.checkType(currentquestion, type)) {
					errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file, line,
							column, "", currentquestion.getName(), type));
				}
			}
		}
		Solution diag = idom.findSolution(solution);
		if (diag == null) {
			if (lazydiag) {
				diag = idom.createSolution(solution, idom.getKnowledgeBase().getRootSolution());
			}
			else {
				errors.add(MessageKnOfficeGenerator.createSolutionNotFoundException(file, line,
						column, "", solution));
				return;
			}
		}
		TerminalCondition cond;
		if (currentquestion instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) currentquestion;
			String s;
			if (answer.startsWith("<=")) {
				s = answer.substring(2).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumLessEqual(qnum, d);
			}
			else if (answer.startsWith("<")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumLess(qnum, d);
			}
			else if (answer.startsWith("=")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumEqual(qnum, d);
			}
			else if (answer.startsWith(">=")) {
				s = answer.substring(2).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumGreaterEqual(qnum, d);
			}
			else if (answer.startsWith(">")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumGreater(qnum, d);
			}
			else if (answer.startsWith("[")) {
				s = answer.substring(1, answer.length() - 1).trim();
				int i = s.lastIndexOf(' ');
				Double d1;
				Double d2;
				try {
					d1 = Double.parseDouble(s.substring(0, i));
					d2 = Double.parseDouble(s.substring(i + 1));
				}
				catch (NumberFormatException e) {
					errors.add(MessageKnOfficeGenerator.createAnswerNotNumericException(file, line,
							column, "", s));
					return;
				}
				cond = new CondNumIn(qnum, d1, d2);
			}
			else {
				cond = null;
				errors.add(MessageKnOfficeGenerator.createAnswerNotNumericException(file, line,
						column, "", answer));
				return;
			}
		}
		else if (currentquestion instanceof QuestionYN) {
			QuestionYN qyn = (QuestionYN) currentquestion;
			Choice ac;
			if (answer == null || answer.equalsIgnoreCase("ja") || answer.equalsIgnoreCase("yes")) {
				ac = qyn.getAnswerChoiceYes();
			}
			else if (answer.equalsIgnoreCase("nein") || answer.equalsIgnoreCase("no")) {
				ac = qyn.getAnswerChoiceNo();
			}
			else {
				errors.add(MessageKnOfficeGenerator.createAnswerNotYNException(file, line, column,
						"", answer));
				return;
			}
			cond = new CondEqual(qyn, new ChoiceValue(ac));
		}
		else if (currentquestion instanceof QuestionChoice) {
			QuestionChoice qc = (QuestionChoice) currentquestion;
			if (currentanswer == null || !currentanswer.getName().equals(answer)) {
				currentanswer = idom.findAnswerChoice(qc, answer);
				if (currentanswer == null) {
					if (lazy) {
						if (answer == null) {
							errors.add(MessageKnOfficeGenerator.createAnswerCreationUnambiguousException(
									file, line, column, "", answer));
							return;
						}
						else {
							currentanswer = idom.addChoiceAnswer(qc, answer);
						}
					}
					else {
						errors.add(MessageKnOfficeGenerator.createAnswerNotFoundException(file,
								line, column, "", answer, answer));
						return;
					}
				}
			}
			cond = new CondEqual(qc, new ChoiceValue(currentanswer));
		}
		else {
			cond = null;
			errors.add(MessageKnOfficeGenerator.createQuestionTypeNotSupportetException(file, line,
					column, "", question));
			return;
		}
		boolean errorOccured = false;
		if (errorcount != errors.size()) {
			errorOccured = true;
		}
		Message msg = ckb.add(idom, line, column, file, cond, value, diag, errorOccured);
		if (msg != null) {
			errors.add(msg);
		}
	}

	@Override
	public void setQuestionClass(String name, int line, int column) {
		currentqclass = idom.findQContainer(name);
		if (currentqclass == null) {
			if (lazy) {
				currentqclass = idom.createQContainer(name, idom.getKnowledgeBase().getRootQASet());
				if (idom.getKnowledgeBase().getInitQuestions().isEmpty()) {
					ArrayList<QASet> tmp = new ArrayList<QASet>();
					tmp.add(currentqclass);
					idom.getKnowledgeBase().setInitQuestions(tmp);
				}
			}
			else {
				errors.add(MessageKnOfficeGenerator.createQuestionClassNotFoundException(file,
						line, column, "", name));
			}
		}
	}

	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		this.idom = idom;
		tb.parse(new File(file));
		finish();
		return errors;
	}

	@Override
	public List<Message> checkKnowledge() {
		finish();
		return errors;
	}

	@Override
	public void addXlsError() {
		errors.add(MessageKnOfficeGenerator.createNoXlsFileException(file, 0, ""));
	}

	@Override
	public void addNoDiagsError(int startrow) {
		errors.add(MessageKnOfficeGenerator.createNoDiagsError(file, startrow));
	}

	@Override
	public void addNoQuestionError(int i, int j) {
		errors.add(MessageKnOfficeGenerator.createNoQuestionOnStack(file, i, j, ""));
	}

}
