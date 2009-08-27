package de.d3web.knowledgeExporter;

import java.io.File;
import java.io.IOException;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.domainModel.ruleCondition.CondQuestion;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.questionSetter.ActionQuestionSetter;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;

public abstract class KnowledgeWriter {
	
	
	protected ConditionVerbalizer verbalizer;
	
	protected KnowledgeManager manager;
	
	protected KnowledgeWriter(KnowledgeManager manager) {
		this.manager = manager;
		this.verbalizer = new ConditionVerbalizer();
		this.verbalizer.setLocale(KnowledgeManager.getLocale());
	}

	
	protected Diagnosis getDiagnosis(RuleComplex r) {
		ActionHeuristicPS action = (ActionHeuristicPS) r.getAction();
		return action.getDiagnosis();
	}


	protected Score getScore(RuleComplex element) {
		if (element.getAction() instanceof ActionHeuristicPS) {
			ActionHeuristicPS action = (ActionHeuristicPS) (element.getAction());
			return action.getScore();
		}
		return null;
	}


	/*
	 * prüft ob die Regel vollständig ist, also ob keine komponenten null sind.
	 * [TODO] Noch nicht vollständig!
	 */
	protected boolean isValidRule(RuleComplex r) {
		RuleAction a = r.getAction();
		if (a == null) {
			return false;
		}
		if (a instanceof ActionHeuristicPS) {
			Diagnosis d = ((ActionHeuristicPS) a).getDiagnosis();
			if (d == null) {
				return false;
			}
		}
		if (a instanceof ActionQuestionSetter) {
			Question q = ((ActionQuestionSetter) a).getQuestion();
			if (q == null) {
				return false;
			}
		}

		AbstractCondition cond = r.getCondition();
		if (cond == null) {
			return false;
		}
		if (cond instanceof TerminalCondition) {
			if (cond instanceof CondDState) {
				Diagnosis d = ((CondDState) cond).getDiagnosis();
				if (d == null) {
					return false;
				}
			}
			if (cond instanceof CondQuestion) {
				Question q = ((CondQuestion) cond).getQuestion();
				if (q == null) {
					return false;
				}
			}
		}

		return true;
	}
	
	protected String trimNum(String s) {
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}
	
	public abstract void writeFile(File output) throws IOException; 
	


}
