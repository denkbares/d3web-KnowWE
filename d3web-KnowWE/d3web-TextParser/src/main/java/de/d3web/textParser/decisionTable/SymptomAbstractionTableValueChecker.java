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
