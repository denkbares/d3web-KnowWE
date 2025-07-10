package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import java.util.ArrayList;
import java.util.List;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class ExamineCherryPickContext implements GitWorkflowContext {

	public final GitConnector gitConnector;
	private final String commitHash;

	//to be filled during transduction of state machine
	private boolean isEmptyCommit;

	public ExamineCherryPickContext(GitConnector gitConnector, String commitHash) {
		this.gitConnector = gitConnector;
		this.commitHash = commitHash;
	}

	@Override
	public void addState(State state) {
		//TODO
	}

	@Override
	public void addMessage(State state, String message) {
//TODO
	}

	@Override
	public GitConnector getGitConnector() {
		return this.gitConnector;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setEmptyCommit(boolean isEmpty) {
		this.isEmptyCommit = isEmpty;
	}

	private List<String> changedFiles = null;

	public List<String> changedFiles() {
		if (changedFiles == null) {
			List<String> strings = this.gitConnector.listChangedFilesForHash(commitHash);
			this.changedFiles = strings;
		}
		return new ArrayList<>(changedFiles);
	}
}
