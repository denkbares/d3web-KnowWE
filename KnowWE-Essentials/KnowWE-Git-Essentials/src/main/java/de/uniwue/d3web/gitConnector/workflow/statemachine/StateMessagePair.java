package de.uniwue.d3web.gitConnector.workflow.statemachine;

public class StateMessagePair {

	public final State state;
	public final String message;

	public StateMessagePair(State state, String message) {
		this.state = state;
		this.message = message;
	}
}
