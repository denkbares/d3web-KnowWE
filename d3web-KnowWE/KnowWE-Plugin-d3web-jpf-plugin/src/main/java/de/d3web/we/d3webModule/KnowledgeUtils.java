/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.d3webModule;

import java.util.ArrayList;
import java.util.List;

import de.d3web.KnOfficeParser.table.CellKnowledgeBuilder;
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
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.report.Message;

public class KnowledgeUtils {
	
	
	public static String[] QTYPES = {"num", "oc", "mc", "jn", "yn", "date"};
	
	public static String getQuestionTypeFromDeclaration(String declaration) {
		for(String type : QTYPES) {
			if(declaration.contains("["+type+"]")) {
				return type;
			}
		}
		return null;
		
	}
	
	public static String getQuestionNameFromDeclaration(String declaration) {
		String type = getQuestionTypeFromDeclaration(declaration);
		String name = declaration.replaceAll("\\["+type+"\\]", "");
		name = name.replaceAll("__", "");
		return name.trim();
	}

	public static List<Message> addKnowledge(IDObjectManagement idom,
			String question, String answer, String solution, String value,
			int line, int column, boolean lazy, String kdomid, CellKnowledgeBuilder ckb) {
		List<Message> errors = new ArrayList<Message>();
		if (answer != null)
			answer = answer.trim();
		boolean typedef = false;
		String type = "";
		if (question.endsWith("]")) {
			type = question.substring(question.lastIndexOf('[') + 1, question
					.length() - 1);
			if (type.equals("y/n")) {
				type = "yn";
			}
			question = question.substring(0, question.lastIndexOf('[')).trim();
			typedef = true;
		}
		Question currentquestion = idom.findQuestion(question);
		if (currentquestion == null) {
			errors.add(MessageKnOfficeGenerator
					.createQuestionNotFoundException(kdomid, line, column, "",
							question));
			return errors;

		} else {
			if (!D3webQuestionFactory.checkType(currentquestion, type)) {
				errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(
						kdomid, line, column, "", currentquestion.getName(),
						type));
			}
		}
		Solution diag = idom.findSolution(solution);
		if (diag == null) {
			errors.add(MessageKnOfficeGenerator
					.createSolutionNotFoundException(kdomid, line, column, "",
							solution));
			return errors;
		}
		TerminalCondition cond;
		if (currentquestion instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) currentquestion;
			cond = tryBuildCondNum(answer, line, column, kdomid, errors, qnum);
			if(cond == null) return errors;
		} else if (currentquestion instanceof QuestionYN) {
			QuestionYN qyn = (QuestionYN) currentquestion;
			Choice ac;
			if (answer == null || answer.equalsIgnoreCase("ja")
					|| answer.equalsIgnoreCase("yes")) {
				ac = qyn.yes;
			} else if (answer.equalsIgnoreCase("nein")
					|| answer.equalsIgnoreCase("no")) {
				ac = qyn.no;
			} else {
				errors.add(MessageKnOfficeGenerator.createAnswerNotYNException(
						kdomid, line, column, "", answer));
				return errors;
			}
			cond = new CondEqual(qyn, new ChoiceValue(ac));
		} else if (currentquestion instanceof QuestionChoice) {
			QuestionChoice qc = (QuestionChoice) currentquestion;
			Choice currentanswer = idom.findAnswerChoice(qc, answer);
			if (currentanswer == null) {
				errors.add(MessageKnOfficeGenerator
						.createAnswerNotFoundException(kdomid, line, column,
								"", answer, answer));

			}

			cond = new CondEqual(qc, new ChoiceValue(currentanswer));
		} else {
			cond = null;
			errors.add(MessageKnOfficeGenerator
					.createQuestionTypeNotSupportetException(kdomid, line,
							column, "", question));
			return errors;
		}
		Message msg = ckb.add(idom, line, column, kdomid, cond, value, diag,
				errors.size() > 0);
		if (msg != null) {
			errors.add(msg);
		}
		return errors;
	}

	public static TerminalCondition tryBuildCondNum(String answer, int line,
			int column, String kdomid, List<Message> errors, QuestionNum qnum) {
		TerminalCondition cond = null;
		String s = null;
		try {
			if (answer.startsWith("<=")) {
				s = answer.substring(2).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumLessEqual(qnum, d);
			} else if (answer.startsWith("<")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumLess(qnum, d);
			} else if (answer.startsWith("=")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumEqual(qnum, d);
			} else if (answer.startsWith(">=")) {
				s = answer.substring(2).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumGreaterEqual(qnum, d);
			} else if (answer.startsWith(">")) {
				s = answer.substring(1).trim();
				Double d = Double.parseDouble(s);
				cond = new CondNumGreater(qnum, d);
			} else if (answer.startsWith("[")) {
				s = answer.substring(1, answer.length() - 1).trim();
				int i = s.lastIndexOf(' ');
				Double d1 = null;
				Double d2 = null;
				try {
					d1 = Double.parseDouble(s.substring(0, i));
					d2 = Double.parseDouble(s.substring(i + 1));
				} catch (NumberFormatException e) {
					errors.add(MessageKnOfficeGenerator
							.createAnswerNotNumericException(kdomid, line,
									column, "", s));
				}
				cond = new CondNumIn(qnum, d1, d2);
			} else {
				cond = null;
				errors.add(MessageKnOfficeGenerator
						.createAnswerNotNumericException(kdomid, line, column,
								"", answer));
			}
		} catch (NumberFormatException e) {
			errors.add(MessageKnOfficeGenerator
					.createAnswerNotNumericException(kdomid, line,
							column, "", s));
		}
		return cond;
	}
		
}
