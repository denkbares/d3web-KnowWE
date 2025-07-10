package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.workflow.push.GitTask;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.states.VerifyCleanState;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowStateMachine;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class PushTaskStateMachine implements GitWorkflowStateMachine<PushContext> {

	GitConnector gitConnector;
	GitTask task;

	public PushTaskStateMachine(GitConnector gitConnector, GitTask task) {
		this.gitConnector = gitConnector;
		this.task = task;
	}

	@Override
	public State getInitialState() {
		return new VerifyCleanState(null, null);
	}

	@Override
	public PushContext getInitialContext() {
		return new PushContext(this.gitConnector, this.task, null, null);
	}
}

