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

/*
 * Created on 21.06.2005
 */
package de.d3web.textParser.decisionTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.Utils.QuestionNotInKBError;

/**
 * @author Andreas Klar
 */
public abstract class DecisionTableValueChecker implements ValueChecker {

	protected DecisionTableConfigReader cReader;

	protected KnowledgeBaseManagement kbm;

	public DecisionTableValueChecker(DecisionTableConfigReader cReader,
			KnowledgeBaseManagement kbm) {
		this.cReader = cReader;
		this.kbm = kbm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	public abstract Report checkValues(DecisionTable table);

	protected Report checkScores(DecisionTable table, int startRowIx,
			int endRowIx, int startColumnIx, int endColumnIx,
			String defaultString) {
		Report report = new Report();
		for (int i = startRowIx; i < endRowIx; i++) {
			for (int j = startColumnIx; j < endColumnIx; j++) {
				if (table.get(i, j).equals("") && defaultString != null) {
					table.getTableData()[i][j] = defaultString;
					report.note(MessageGenerator.usingDefault(i, j,
							defaultString));
				}
				if (!table.get(i, j).equals("")
						&& !this.cReader.isValid(
								DecisionTableConfigReader.SCORE, table
										.get(i, j))) {
					// Begin Change
					if (!checkData(table, i, j)) {
						report.error(MessageGenerator.invalidScore(i, j, table
								.get(i, j)));
					} else {
						// Die Antwort passt -> Sonderfall und kein Report
					}
					// End Change
				}

			}
		}
		return report;
	}

	protected Report checkLogicalOperatorsColumn(DecisionTable table,
			int startColumnIx, int endColumnIx, int line, String defaultString) {
		Report report = new Report();

		for (int j = startColumnIx; j < endColumnIx; j++) {
			// bei nicht vorhandenem Operator wird default wert gesetzt
			if (table.isEmptyCell(line, j) && defaultString != null) {
				table.getTableData()[line][j] = defaultString;
				report.note(MessageGenerator.usingDefault(line, j, defaultString));
			}
			if (!table.isEmptyCell(line, j)
					&& !cReader.isValid(
							DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN,
							table.get(line, j))) {
				// check special case "minmax"
				String logOp = table.get(line, j);
				String[] l = logOp
						.split(DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN_MINMAX);
				if (l.length != 2)
					report.error(MessageGenerator.invalidLogicalOperator(line, j,
							logOp));
				else {
					try {
						int min = Integer.parseInt(l[0]);
						int max = Integer.parseInt(l[1]);
						if (min > max)
							report.error(MessageGenerator.invalidMinMax(line, j,
									logOp));
					} catch (NumberFormatException e) {
						report.error(MessageGenerator.invalidLogicalOperator(line,
								j, logOp));
					}
				}
			}
		}
		return report;
	}

	// Methode fï¿½r Extension Parser
	protected boolean checkData(DecisionTable table, int i, int j) {
		String question = this.getLastQuestion(table, i);
		Question theQuestion = kbm.findQuestion(question);
		if (theQuestion instanceof QuestionNum) {
			Pattern p = Pattern
					.compile("(<|<=|>|>=|=)?[ ]*[0-9]+(\\.|,)?[0-9]*");
			Matcher m = p.matcher(table.get(i, j));
			boolean b = m.matches();
			if (!b) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	// Methode um die letzte Frage zu finden
	protected String getLastQuestion(DecisionTable table, int row) {
		while (table.get(row, 0).equals("")) {
			row--;
		}
		return table.get(row, 0);
	}

	protected Report checkQuestions(DecisionTable table, int startRowIx,
			int endRowIx) {
		Report report = new Report();
		for (int i = startRowIx; i < endRowIx; i++) {
			if (!table.isEmptyCell(i, 0)) {
				String questionText = table.get(i, 0);
				if (kbm.findQuestion(questionText) == null
						&& kbm.findQuestion(QuestionNotInKBError
								.deleteQTag(questionText)) == null) {
					int type = tryFindOutQuestionType(i, 0, table);
					Message errorMess = MessageGenerator.invalidQuestion(i, 0,
							table.get(i, 0), new Integer(type));

					report.error(errorMess);
				}
			}
		}
		return report;
	}

	protected int tryFindOutQuestionType(int i, int j, DecisionTable table) {
		int type = -1;

		String firstAnswer = table.get(i + 1, j + 1);
		if (QuestionNotInKBError.isYesOrNo(firstAnswer)) {
			return QuestionNotInKBError.TYPE_YN;
		}

		if (QuestionNotInKBError.isNum(firstAnswer)) {
			return QuestionNotInKBError.TYPE_NUM;
		}

		return -1;
	}

	protected Report checkAnswers(DecisionTable table, int startRowIx,
			int endRowIx) {
		return checkAnswers(table, startRowIx, endRowIx, 1, false);
	}

	protected Report checkAnswers(DecisionTable table, int startRowIx,
			int endRowIx, int columnIx, boolean tolerateEmpty) {
		Report report = new Report();

		for (int i = startRowIx; i < endRowIx; i++) {
			if (tolerateEmpty) {
				if (table.isEmptyCell(i, columnIx)) {
					continue;
				}
			}
			String answerText = table.get(i, columnIx);
			String questionName = table.getQuestionText(i);

			// to tolerate qTag Question annotation syntax :e.g.
			// 'myquestionname[mc]'
			String nameWithOutQTag = QuestionNotInKBError
					.deleteQTag(questionName);

			Question theQuestion = kbm.findQuestion(nameWithOutQTag);
			if (!(answerText.equals("")
					|| answerText.equalsIgnoreCase("unbekannt") || answerText
					.equalsIgnoreCase("unknown"))
					&& theQuestion != null) {
				try {
					// error handling for QuestionNum
					if (theQuestion instanceof QuestionNum) {
						report.addAll(checkAnswerNum(answerText, i));
					}
					// error handling for QuestionYN
					else if (theQuestion instanceof QuestionYN) {
						report.addAll(checkAnswerYN((QuestionYN) theQuestion,
								answerText, i));

					}
					// error handling for QuestionChoice
					else if (kbm.findAnswerChoice((QuestionChoice) theQuestion,
							table.get(i, columnIx)) == null)
						report.error(MessageGenerator.invalidAnswer(i,
								columnIx, questionName, table.get(i, columnIx),
								theQuestion));
				} catch (Exception e) {
					report.error(MessageGenerator.invalidAnswer(i, columnIx,
							questionName, table.get(i, columnIx), theQuestion));
				}
			}
		}
		return report;
	}

	protected Report checkAnswerNum(String answerText, int row) {
		Report report = new Report();
		String origAnswerText = answerText;
		answerText = answerText.trim();
		if (answerText.startsWith("\"") && answerText.endsWith("\""))
			answerText = answerText.substring(1, answerText.length() - 1)
					.trim();
		if (answerText.startsWith("[") && answerText.endsWith("]")) {
			answerText = answerText.substring(1, answerText.length() - 1)
					.trim();
			String[] numbers = answerText.split(" ");
			if (numbers.length == 2) {
				try {
					Double.parseDouble(numbers[0]);
				} catch (NumberFormatException e) {
					report.error(MessageGenerator.invalidAnswerNum(row, 0,
							numbers[0]));
				}
				try {
					Double.parseDouble(numbers[1]);
				} catch (NumberFormatException e) {
					report.error(MessageGenerator.invalidAnswerNum(row, 0,
							numbers[1]));
				}
			} else
				report.error(MessageGenerator.invalidAnswerNumInOperator(row,
						0, origAnswerText));
		} else {
			if (answerText.startsWith("N") || answerText.startsWith("P")) {
			} else {
				if (answerText.startsWith(">=") || answerText.startsWith("<="))
					answerText = answerText.substring(2);
				else if (answerText.startsWith("=")
						|| answerText.startsWith("<")
						|| answerText.startsWith(">"))
					answerText = answerText.substring(1);
				answerText = answerText.replaceAll("[,]", ".").trim();
				try {
					Double.parseDouble(answerText);
				} catch (NumberFormatException e) {
					report.error(MessageGenerator
							.invalidAnswerNumNumberOrOperator(row, 0,
									origAnswerText));
				}
			}
		}
		return report;
	}

	protected Report checkAnswerYN(QuestionYN theQuestion, String answerText,
			int row) {
		Report report = new Report();
		final String yes = "Yes";
		final String no = "No";
		if (((answerText.equalsIgnoreCase("yes") || answerText
				.equalsIgnoreCase("ja")) && (kbm.findAnswerChoice(theQuestion,
				yes) == null))
				|| ((answerText.equalsIgnoreCase("no") || answerText
						.equalsIgnoreCase("nein")) && (kbm.findAnswerChoice(
						theQuestion, no) == null)))
			report.error(MessageGenerator.invalidAnswer(row, 1, theQuestion
					.getText(), answerText));

		return report;
	}

	protected Report checkDiagnoses(DecisionTable table, int startColumnIx,
			int endColumnIx) {
		Report report = new Report();
		for (int j = startColumnIx; j < endColumnIx; j++) {
			if (!table.get(0, j).equals(""))
				// if (!tChecker.isDiagnose(tableData[0][j]))
				if (kbm.findDiagnosis(table.get(0, j)) == null)
					report.error(MessageGenerator.invalidDiagnosis(0, j, table
							.get(0, j)));
		}
		return report;
	}
}
