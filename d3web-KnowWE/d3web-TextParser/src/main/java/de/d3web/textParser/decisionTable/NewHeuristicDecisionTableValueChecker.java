package de.d3web.textParser.decisionTable;

import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.report.Message;
import de.d3web.textParser.Utils.QuestionNotInKBError;
import de.d3web.report.Report;

public class NewHeuristicDecisionTableValueChecker extends
		DecisionTableValueChecker {
	private NewDecisionTableParserManagement man;

	public NewHeuristicDecisionTableValueChecker(
			DecisionTableConfigReader cReader, KnowledgeBaseManagement kbm) {
		super(cReader, kbm);

	}

	public void setParserManagement(NewDecisionTableParserManagement man) {
		this.man = man;
	}
	
	@Override
	protected Report checkQuestions(DecisionTable table, int startRowIx,
			int endRowIx) {
		Report report = new Report();
		for (int i = startRowIx; i < endRowIx; i++) {
			if (!table.isEmptyCell(i, 0)) {
				String questionText = table.get(i, 0);
				if (kbm.findQuestion(questionText) == null) {
					
					int type = tryFindOutQuestionType(i, 0, table);
					
					Message errorMess = MessageGenerator.invalidQuestion(i, 0,
							table.get(i, 0), new Integer(type));
					
//					int castType = man.getQuestionType(questionText);
//					if(castType != -1 && errorMess instanceof QuestionNotInKBError) {
//						((QuestionNotInKBError)errorMess).setTypeCasted(castType);
//					}

					report.error(errorMess);
				}
			}
		}
		return report;
	}
	
	@Override
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

			answerText = cutNegation(answerText).trim();
			
		
			Question theQuestion = kbm.findQuestion(questionName);
			if (!(answerText.equals("")
					|| answerText.equalsIgnoreCase("unbekannt") || answerText
					.equalsIgnoreCase("unknown"))
					&& theQuestion != null) {
//				try {
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
							answerText) == null)
						report.error(MessageGenerator.invalidAnswer(i,
								columnIx, questionName, answerText,
								theQuestion));
//				} catch (Exception e) {
//					report.error(MessageGenerator.invalidAnswer(i, columnIx,
//							questionName, table.get(i, columnIx), theQuestion));
//				}
			}
		}
		return report;
	}
	
	public static String cutNegation(String answerText) {
		String [] nots = {"NOT", "Not", "not", "Nicht", "NICHT","nicht"};
		for (int i = 0; i < nots.length; i++) {
			String string = nots[i];
			if(answerText.startsWith(string)) {
				return answerText.substring(string.length()+1);
			}
		}
		return answerText;
	}

	@Override
	public Report checkValues(DecisionTable table) {
		Report report = new Report();
		ResourceBundle rb = ResourceBundle
				.getBundle("properties.DecisionTableMessages");
		int dataStartLine = man.getDataStartLine();
		if (man.getConjunctorLine() != -1) {
			report.addAll(checkLogicalOperatorsColumn(table, man
					.getConjunctorLine(), table.columns(),man.getConjunctorLine(), rb
					.getString("default.value.operator")));
		}

		if (man.getScoreLine() != -1) {
			report.addAll(checkScores(table, man.getScoreLine(), man
					.getScoreLine() + 1, 1, table
					.columns(), rb.getString("default.value.score")));
		}

		report.addAll(checkQuestions(table, dataStartLine, table.rows()));
		for (int j = 1; j < table.columns(); j++) {
			report.addAll(checkAnswers(table, dataStartLine, table.rows(), j,
					true));
		}
		report.addAll(checkDiagnoses(table, 0, table.columns()));

		
		
		return report;
	}
	
	

	@Override
	protected int tryFindOutQuestionType(int i, int j, DecisionTable table) {
		
		String question = table.get(i, j);
		if(man.getQuestionType(question) != -1) {
			return man.getQuestionType(question);
		}

		for(int col = j+1; col < table.columns(); col++ ) {
		String firstAnswer = table.get(i, col);
		if (QuestionNotInKBError.isYesOrNo(firstAnswer)) {
			return QuestionNotInKBError.TYPE_YN;
		}

		if (QuestionNotInKBError.isNum(firstAnswer)) {
			return QuestionNotInKBError.TYPE_NUM;
		}
		}
		return -1;
	}

}
