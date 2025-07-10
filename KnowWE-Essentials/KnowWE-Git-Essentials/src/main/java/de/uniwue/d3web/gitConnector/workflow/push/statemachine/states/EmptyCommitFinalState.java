package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class EmptyCommitFinalState implements State {
	@Override
	public State next(GitWorkflowContext context) {
		return null;
	}

	@Override
	public String description() {
		return "Represents a state in which a commit was declared to be empty";
	}

	@Override
	public List<State> successors() {
		return List.of();
	}
}
