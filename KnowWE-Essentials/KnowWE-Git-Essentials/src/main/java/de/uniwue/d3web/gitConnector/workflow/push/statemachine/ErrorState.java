package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class ErrorState implements State {

	public final String errorMessage;

	public ErrorState(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public State next(GitWorkflowContext context) {
		return null;
	}

	@Override
	public String description() {
		return "Represents an error state";
	}

	@Override
	public List<State> successors() {
		return List.of();
	}
}
