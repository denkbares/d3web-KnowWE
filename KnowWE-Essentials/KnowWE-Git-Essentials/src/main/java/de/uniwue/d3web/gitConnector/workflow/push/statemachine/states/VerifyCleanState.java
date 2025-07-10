package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class VerifyCleanState implements State {

	private final State onSuccessState;
	private final State onErrorState;

	public VerifyCleanState(State onSuccessState, State onErrorState) {
		this.onSuccessState = onSuccessState;
		this.onErrorState = onErrorState;
	}

	@Override
	public State next(GitWorkflowContext context) {
		GitStatusCommandResult status = context.getGitConnector().status();
		if (status instanceof GitStatusResultSuccess success && success.isClean()) {
			context.addMessage(this, "Git repository is clean!");
			return onSuccessState;
		}
		else {
			context.addMessage(this, "Git repository is NOT clean, cannot continue.");
			return onErrorState;
		}
	}

	@Override
	public String description() {
		return "Executes 'git status' and verifies if working area is clean";
	}

	@Override
	public List<State> successors() {
		return List.of(onSuccessState, onErrorState);
	}
}

