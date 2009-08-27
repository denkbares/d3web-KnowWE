/**
 * 
 */
package de.d3web.textParser.decisionTable;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.report.Report;
import de.d3web.textParser.Utils.KBUtils;

/**
 * @author Andreas Klar
 *
 */
public class ReplacementTableKnowledgeGenerator extends KnowledgeGenerator {

	public ReplacementTableKnowledgeGenerator(KnowledgeBaseManagement kbm) {
		super(kbm);
	}

	@Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
		int overallCount = 0;
		int answerCount = 0;
		int diagnosisCount = 0;
		int qContainerCount = 0;
		int questionCount = 0;
		
		for (int i=0; i<table.rows(); i++) {
			if (!table.get(i,0).equals("") && !table.get(i,1).equals("")) {
				if (!table.get(i,2).equals("")) {
					Question q = kbm.findQuestion(table.get(i,2));
					if (q!=null && q instanceof QuestionChoice) {
						AnswerChoice a = kbm.findAnswerChoice((QuestionChoice)q, table.get(i,0));
						if (a!=null) {
							a.setText(table.get(i,1));
							answerCount++;
							overallCount++;
						}
					}
				}
				else {
					NamedObject obj = KBUtils.findNamedObject(kbm, table.get(i,0));
					if (obj!=null) {
						obj.setText(table.get(i,1));
						if (obj instanceof Diagnosis) diagnosisCount++;
						else if (obj instanceof Question) questionCount++;
						else if (obj instanceof QContainer) qContainerCount++;
						overallCount++;
					}
				}
			}
		}
		report.note(MessageGenerator.renamedObjects(
				table.getSheetName()+":"+table.getTableNumber(),
				qContainerCount,questionCount, answerCount, diagnosisCount));
		return report;
	}
}
