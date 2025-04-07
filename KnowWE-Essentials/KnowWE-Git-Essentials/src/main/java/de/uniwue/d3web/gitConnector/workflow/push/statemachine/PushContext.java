package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import java.util.ArrayList;
import java.util.List;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.workflow.push.GitTask;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;
import de.uniwue.d3web.gitConnector.workflow.statemachine.StateMessagePair;

public class PushContext implements GitWorkflowContext {
	public final GitConnector gitConnector;
	public final GitTask task;
	public final String username;
	public final String passwordOrToken;

	private List<StateMessagePair> messages;

	private List<State> visitedStates;

	public PushContext(GitConnector gitConnector, GitTask task, String username, String passwordOrToken) {
		this.gitConnector = gitConnector;
		this.task = task;
		this.username = username;
		this.passwordOrToken = passwordOrToken;
		this.visitedStates = new ArrayList<>();
		this.messages = new ArrayList<>();
	}

	public void addState(State state) {
		this.visitedStates.add(state);
	}

	public void addMessage(State state, String message) {
		this.messages.add(new StateMessagePair(state, message));
	}

	@Override
	public GitConnector getGitConnector() {
		return this.gitConnector;
	}
}

