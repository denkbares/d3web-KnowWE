package de.d3web.knowledgeExporter.xlsWriters;

import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondQuestion;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.knowledgeExporter.KnowledgeManager;

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

		for (RuleComplex rc:manager.getAllRules()) {
			AbstractCondition cond = rc.getCondition();
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
				String d = getDiagnosis(rc).getText();
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
