package de.uniwue.d3web.gitConnector.workflow.push.structs;

import java.util.ArrayList;
import java.util.List;

//TODO this is actually deprecated remove if you can!
public class PushTaskContextResult {

	public final List<PushTaskContextResultStage> resultsOfStages;

	public PushTaskContextResult() {
		this.resultsOfStages = new ArrayList<>();
	}

	public void addStageResult(PushTaskContextResultStage stageResult) {
		this.resultsOfStages.add(stageResult);
	}

	public void addStageResult(String message, boolean success) {
		this.resultsOfStages.add(new PushTaskContextResultStage(message, success));
	}

	public List<PushTaskContextResultStage> getResultsOfStages() {
		return new ArrayList<>(resultsOfStages);
	}

	public boolean hasError() {
		if (resultsOfStages.isEmpty()) {
			return false;
		}
		return !resultsOfStages.get(resultsOfStages.size() - 1).isSuccess();
	}

	public void add(PushTaskContextResult other) {
		this.resultsOfStages.addAll(other.resultsOfStages);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Executed workflow with intermediatery results: \n");
		for (PushTaskContextResultStage stage : resultsOfStages) {
			builder.append(stage.getResultString() + " || " + stage.isSuccess() + "\n");
		}
		return builder.toString();
	}
}
