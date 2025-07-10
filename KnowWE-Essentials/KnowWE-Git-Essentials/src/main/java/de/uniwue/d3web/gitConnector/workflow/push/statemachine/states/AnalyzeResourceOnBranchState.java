package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.push.statemachine.ExamineCherryPickContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class AnalyzeResourceOnBranchState implements State {

	public State onFailure;
	public State onSuccess;

	private final String resource;

	public AnalyzeResourceOnBranchState(String resource) {
		this.resource = resource;
	}

	@Override
	public State next(GitWorkflowContext context) {
		if (context instanceof ExamineCherryPickContext cpc) {


		}

		return null;
	}

	@Override
	public String description() {
		return "Checks whether a commit is empty, this means there are no files that are changed in this commit! If so passed into an endstate, otherwise pass back to previous state";
	}

	@Override
	public List<State> successors() {
		return List.of(onFailure, onSuccess);
	}
}
