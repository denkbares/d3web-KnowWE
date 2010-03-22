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

package de.d3web.knowledgeExporter.xlsWriters;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleAction;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.CondQuestion;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.scoring.ActionHeuristicPS;

public class DiagnosisScoresTableWriter extends QDTableWriter {
	
	public DiagnosisScoresTableWriter(KnowledgeManager manager) {
		super(manager);
		verbalizer = new ConditionVerbalizer();
	}
	
	
	/*
	 * Schreibt die einfachen Heuristischen Regeln raus
	 */
	@Override
	protected void getKnowledge() {

		for (Rule rc:manager.getAllRules()) {
			Condition cond = rc.getCondition();
			RuleAction raction =  rc.getAction();
			if (rc.getException() == null 
					&& rc.getContext() == null 
					&& isValidRule(rc)
					&& !manager.isDone(rc)
					&& raction instanceof ActionHeuristicPS 
					&& cond instanceof CondQuestion) {
				
				ActionHeuristicPS action = (ActionHeuristicPS) raction;
				TerminalCondVerbalization tCondVerb = (TerminalCondVerbalization) 
					verbalizer.createConditionVerbalization(cond);
				String d = getDiagnosis(rc).getName();
				String a = tCondVerb.getAnswer();
				String q = tCondVerb.getQuestion();
				String s = action.getScore().toString();
//				d.getKnowledge(PSMHeuristic.class, MethodKind.BACKWARD);
				if (d != null && q != null && s != null) {
					if (!diagnosisList.contains(d)) {
						diagnosisList.add(d);
					}
					addEntry(d, q, a, s);
				}
			}
		}
		splitDiagnosisList();
	}
	
}
