package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class CheckOnSpecifiedBranchState implements State {

	public State onSpecifiedBranch;
	public State notOnSpecifiedBranch;

	private final String branch;

	public CheckOnSpecifiedBranchState(String branch) {
		this.branch = branch;
	}

	@Override
	public State next(GitWorkflowContext context) {
		String currentBranch = context.getGitConnector().currentBranch();

		if (currentBranch != null && currentBranch.equals(branch)) {
			return onSpecifiedBranch;
		}
		return notOnSpecifiedBranch;
	}

	@Override
	public String description() {
		return "Checks whether we are sitting a specified branch";
	}

	@Override
	public List<State> successors() {
		return List.of(onSpecifiedBranch, notOnSpecifiedBranch);
	}
}
