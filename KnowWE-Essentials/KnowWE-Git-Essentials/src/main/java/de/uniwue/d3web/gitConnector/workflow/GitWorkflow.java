package de.uniwue.d3web.gitConnector.workflow;

public interface GitWorkflow<RESULT extends GitWorkflowResult> {

	RESULT execute();
}
