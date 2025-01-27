package de.uniwue.d3web.gitConnector.workflow.result;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitWorkflowResult2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitWorkflowResult2.class);
	public final List<GitWorkflowResultStage> resultsOfStages;

	public GitWorkflowResult2() {
		this.resultsOfStages = new ArrayList<>();
	}

	public void addStageResult(GitWorkflowResultStage stageResult) {
		LOGGER.info("Add gitworkflow result: " + stageResult.getResultString() + " with success: " + stageResult.wasSuccessful());
		this.resultsOfStages.add(stageResult);
	}

	public void addStageResult(String message, boolean success) {
		LOGGER.info("Add gitworkflow result: " + message + " with success: " + success);
		this.resultsOfStages.add(new DefaultGitWorkflowResultStage(message, success));
	}

	public List<GitWorkflowResultStage> getResultsOfStages() {
		return new ArrayList<>(resultsOfStages);
	}

	public boolean hasError() {
		if (resultsOfStages.isEmpty()) {
			return false;
		}
		return !resultsOfStages.get(resultsOfStages.size() - 1).wasSuccessful();
	}

	public GitWorkflowResultStage lastStageResult() {
		return resultsOfStages.get(resultsOfStages.size() - 1);
	}

	public void add(GitWorkflowResult2 other) {
		this.resultsOfStages.addAll(other.resultsOfStages);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Executed workflow with intermediatery results: \n");
		for (GitWorkflowResultStage stage : resultsOfStages) {
			builder.append(stage.getResultString() + " || " + stage.wasSuccessful() + "\n");
		}
		return builder.toString();
	}
}
