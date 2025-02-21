package de.uniwue.d3web.gitConnector.workflow.result;

/**
 * Represents the result of a single stage in the process of pushing a task, some stages are (not in order):
 * 1. Creating a feature branch
 * 2. Finding the head of the current local master branch
 * 3. Cherry-Picking the commits of the task onto the feature branch
 * 4. Pushing to upstream
 * 5 etc... (depending on implementation)
 */
public class DefaultGitWorkflowResultStage implements GitWorkflowResultStage {

	private final String resultString;
	private final boolean success;

	public DefaultGitWorkflowResultStage(String resultString, boolean success) {
		this.resultString = resultString;
		this.success = success;
	}

	public String getResultString() {
		return resultString;
	}

	public boolean wasSuccessful() {
		return success;
	}
}
