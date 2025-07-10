package de.uniwue.d3web.gitConnector.workflow.statemachine;

import de.uniwue.d3web.gitConnector.GitConnector;

public interface GitWorkflowContext {

	void addState(State state);

	void addMessage(State state, String message);

	GitConnector getGitConnector();


}
