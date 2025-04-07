package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.push.statemachine.PushContext;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.CherryPickCompatibilityStateMachine;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.ExamineCherryPickContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class ExamineCommitsForCherryPickCompatibilityState implements State {
	@Override
	public State next(GitWorkflowContext context) {
		if (context instanceof PushContext pushContext) {
			for (String commit : pushContext.task.commits) {
				ExamineCherryPickContext cherryPickContext = new CherryPickCompatibilityStateMachine(commit, context.getGitConnector()).transduce();
			}
		}
		return null;
	}



	@Override
	public String description() {
		return "Examine commits of task for cherry pick readiness";
	}

	@Override
	public List<State> successors() {
		return List.of();
	}
}
