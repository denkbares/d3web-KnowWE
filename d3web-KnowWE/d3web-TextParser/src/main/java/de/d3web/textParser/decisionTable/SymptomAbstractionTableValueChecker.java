package de.d3web.textParser.decisionTable;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.report.Report;

/**
 * @author  Andreas Klar
 */
public class SymptomAbstractionTableValueChecker extends DecisionTableValueChecker {
    
    public SymptomAbstractionTableValueChecker(DecisionTableConfigReader cReader, KnowledgeBaseManagement kbm) {
        super(cReader, kbm);
    }

    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
     */
    @Override
	public Report checkValues(DecisionTable table) {
        Report report = new Report();
       
        report.addAll(checkQuestions(table, 1, table.rows()));
        report.addAll(checkAnswers(table, 2, table.rows()));
        report.addAll(checkHeadQuestions(table));
        report.addAll(checkSetValues(table));
        return report;
    }
    
	private Report checkHeadQuestions(DecisionTable table) {
		Report report = new Report();
		for (int j=2; j<table.columns(); j++) {
			if (!table.get(0,j).equals(""))
				if (kbm.findQuestion(table.get(0,j))==null)
					report.error(MessageGenerator.invalidQuestion(0,j,table.get(0,j)));
		}
		return report;
	}
	
	private Report checkSetValues(DecisionTable table) {
		Report report = new Report();
		for (int j=2; j<table.columns(); j++) {
			Question q = kbm.findQuestion(table.get(0,j));
			if (q!=null && (q instanceof QuestionChoice)) {
				for (int i=2; i<table.rows(); i++) {
					if (!table.get(i,j).equals("")) {
						AnswerChoice a = kbm.findAnswerChoice((QuestionChoice)q, table.get(i,j));
						if (a==null)
							report.error(MessageGenerator.invalidAnswer(i,j,q.getText(),table.get(i,j)));
					}
				}
			}
		}
		return report;
	}
    
}
