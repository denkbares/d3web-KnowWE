package de.uniwue.d3web.gitConnector.workflow.push.structs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import de.uniwue.d3web.gitConnector.workflow.GitWorkflowResult;
import de.uniwue.d3web.gitConnector.workflow.result.DefaultGitWorkflowResultStage;

public class PushTaskWorkflowResult implements GitWorkflowResult {

	private List<DefaultGitWorkflowResultStage> initialCleanResults;
	private List<DefaultGitWorkflowResultStage> featureBranchResults;
	private List<DefaultGitWorkflowResultStage> cherryPickReadinessResults;
	private List<DefaultGitWorkflowResultStage> cherryPickResults;
	private List<DefaultGitWorkflowResultStage> mergeResults;
	private List<DefaultGitWorkflowResultStage> pushResults;
	private List<DefaultGitWorkflowResultStage> restoreWorkingBranchResults;
	private List<DefaultGitWorkflowResultStage> resetResults;

	private boolean markedAsSuccessful = false;

	//create an empty workflow result
	public PushTaskWorkflowResult() {
		this.markedAsSuccessful = false;
	}

	@Override
	public boolean wasSuccessful() {
		//respect the shortcut
		if (markedAsSuccessful) {
			return true;
		}
		return cherryPickSuccessful() && mergeSuccessful() && pushSuccessful() && restoreWorkingBranchSuccessful();
	}

	public boolean initialCleanSuccessful() {
		return initialCleanResults != null && initialCleanResults.get(initialCleanResults.size() - 1).wasSuccessful();
	}

	public boolean createFeaturetaskSuccessful() {
		return featureBranchResults != null && featureBranchResults.get(featureBranchResults.size() - 1).wasSuccessful();
	}

	public boolean cherryPickSuccessful() {
		return cherryPickResults != null && cherryPickResults.get(cherryPickResults.size() - 1).wasSuccessful();
	}

	public boolean cherryPickExamineSuccessful() {
		return cherryPickReadinessResults != null && cherryPickReadinessResults.get(cherryPickReadinessResults.size() - 1).wasSuccessful();
	}

	public boolean mergeSuccessful() {
		return mergeResults != null && mergeResults.get(mergeResults.size() - 1).wasSuccessful();
	}

	public boolean pushSuccessful() {
		return pushResults != null && pushResults.get(pushResults.size() - 1).wasSuccessful();
	}

	public boolean requiredReset(){
		return this.resetResults!=null;
	}

	public boolean resetSuccessfull() {
		return resetResults != null && resetResults.get(resetResults.size() - 1).wasSuccessful();
	}


	public boolean restoreWorkingBranchSuccessful() {
		return restoreWorkingBranchResults != null && restoreWorkingBranchResults.get(restoreWorkingBranchResults.size() - 1)
				.wasSuccessful();
	}

	@Override
	public boolean isFinished() {
		if (markedAsSuccessful) {
			return true;
		}

		//TODO this is really complicated
		throw new NotImplementedException("TODO!");
	}

	private List<DefaultGitWorkflowResultStage> addToAccordingList(List<DefaultGitWorkflowResultStage> accordingList, String message, boolean result) {
		if (accordingList == null) {
			accordingList = new ArrayList<>();
		}
		accordingList.add(new DefaultGitWorkflowResultStage(message, result));

		return accordingList;
	}

	public void addInitialCleanResult(String message, boolean success) {
		this.initialCleanResults = addToAccordingList(initialCleanResults, message, success);
	}

	public void addCherryPickReadinessResults(String message, boolean success) {
		this.cherryPickReadinessResults = addToAccordingList(cherryPickReadinessResults, message, success);
	}

	public void addCreateFeatureBranchResults(String message, boolean success) {
		this.featureBranchResults =addToAccordingList(featureBranchResults, message, success);
	}

	public void addCherryPickResults(String message, boolean success) {
		this.cherryPickResults = addToAccordingList(cherryPickResults, message, success);
	}

	public void addMergeResults(String message, boolean success) {
		this.mergeResults =addToAccordingList(mergeResults, message, success);
	}

	public void addPushResults(String message, boolean success) {
		this.pushResults = addToAccordingList(pushResults, message, success);
	}

	public void addResetResult(String message, boolean success) {
		this.resetResults = addToAccordingList(resetResults, message, success);
	}

	public void addRestoreWorkingBranchResult(String message, boolean success) {
		this.restoreWorkingBranchResults = addToAccordingList(restoreWorkingBranchResults, message, success);
	}

	public void markAsSuccessful() {
		markedAsSuccessful = true;
	}

	public String printWorkflowResult() {
		StringBuilder sb = new StringBuilder();
		appendStageResults(sb, initialCleanResults, "===Phase: Initial clean results===");
		appendStageResults(sb, featureBranchResults, "===Phase: Create Feature Branch===");
		appendStageResults(sb, cherryPickReadinessResults, "===Phase: Examine Readiness For Cherry-Pick===");
		appendStageResults(sb, cherryPickResults, "===Phase: Results for Cherry-Pick===");
		appendStageResults(sb, mergeResults, "===Phase: Results for Merge===");
		appendStageResults(sb, pushResults, "===Phase: Results for Push===");
		appendStageResults(sb, restoreWorkingBranchResults, "===Phase: Results for Restore Working Branch===");

		return sb.toString();
	}

	private void appendStageResults(StringBuilder sb, List<DefaultGitWorkflowResultStage> stageResults, String stageString) {
		if (stageResults != null) {
			sb.append(stageString).append("\n");
			for (DefaultGitWorkflowResultStage stage : stageResults) {
				sb.append(stage.getResultString()).append("\n");
			}
		}
	}
}
