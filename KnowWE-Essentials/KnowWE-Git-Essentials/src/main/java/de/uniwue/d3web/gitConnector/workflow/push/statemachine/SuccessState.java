package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class SuccessState implements State {

	public final String successMessage;

	public SuccessState(String errorMessage) {
		this.successMessage = errorMessage;
	}

	@Override
	public State next(GitWorkflowContext context) {
		return null;
	}

	@Override
	public String description() {
		return "Represents a generic successful ending state";
	}

	@Override
	public List<State> successors() {
		return List.of();
	}
}
