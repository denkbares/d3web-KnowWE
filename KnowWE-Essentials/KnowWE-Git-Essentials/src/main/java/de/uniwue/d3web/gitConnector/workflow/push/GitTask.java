package de.uniwue.d3web.gitConnector.workflow.push;

import java.util.List;
import java.util.Map;

public class GitTask {

	public final String taskName;
	public final String featureBranchName;
	public final List<String> commits;

	//required for error handling
	public final Map<String, List<String>> taskToCommitMap;

	public GitTask(String taskName, String featureBranchName, List<String> commits, Map<String, List<String>> taskToCommitMap) {
		this.taskName = taskName;
		this.featureBranchName = featureBranchName;
		this.commits = commits;
		this.taskToCommitMap = taskToCommitMap;
	}

}
