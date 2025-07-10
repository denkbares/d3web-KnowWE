package de.uniwue.d3web.gitConnector.workflow.statemachine;

public interface GitWorkflowStateMachine<Context extends GitWorkflowContext> {

	State getInitialState();

	Context getInitialContext();

	default Context transduce() {
		State currentState = getInitialState();

		Context context = getInitialContext();
		while (currentState != null) {
			context.addState(currentState);
			currentState = currentState.next(context);
		}

		return context;
	}
}
