package de.uniwue.d3web.gitConnector.workflow.statemachine;

import java.util.List;

public interface State {

	State next(GitWorkflowContext context);


	String description();

	List<State> successors();
}
