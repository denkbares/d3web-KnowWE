package de.uniwue.d3web.gitConnector.workflow.push.structs;


/**
 * Represents the result of a single stage in the process of pushing a task, some stages are (not in order):
 * 1. Creating a feature branch
 * 2. Finding the head of the current local master branch
 * 3. Cherry-Picking the commits of the task onto the feature branch
 * 4. Pushing to upstream
 * 5 etc... (depending on implementation)
 */
public class PushTaskContextResultStage {

	private final String resultString;
	private final boolean success;

	public PushTaskContextResultStage(String resultString, boolean success) {
		this.resultString = resultString;
		this.success = success;
	}

	public String getResultString() {
		return resultString;
	}

	public boolean isSuccess() {
		return success;
	}
}
