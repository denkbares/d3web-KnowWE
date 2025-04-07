package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class PushBranchState implements State {

	private final String branch;
	private final State onFailure;
	private final State onSuccess;

	public PushBranchState(String branch,State onSuccess, State onFailure) {
		this.branch = branch;
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
	}

	@Override
	public State next(GitWorkflowContext context) {
		boolean success = context.getGitConnector().pushBranch(branch);

		if (success) {
			return onSuccess;
		}
		return onFailure;
	}


	@Override
	public String description() {
		return "Pushes the branch " + branch + " to origin";
	}

	@Override
	public List<State> successors() {
		return List.of(onSuccess, onFailure);
	}
}
