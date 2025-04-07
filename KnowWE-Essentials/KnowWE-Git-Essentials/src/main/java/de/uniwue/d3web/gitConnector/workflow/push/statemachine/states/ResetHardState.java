package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandSuccess;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class ResetHardState implements State {

	public State onSuccess;
	public State onFailure;

	private final String commitToReset;

	public ResetHardState(String commitToReset) {
		this.commitToReset = commitToReset;
	}

	@Override
	public State next(GitWorkflowContext context) {

		if (commitToReset != null) {
			throw new NotImplementedException("TODO");
		}

		ResetCommandResult resetCommandResult = context.getGitConnector().resetToHEAD();

		if (resetCommandResult instanceof ResetCommandSuccess) {
			return onSuccess;
		}
		else {
			return onFailure;
		}
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
