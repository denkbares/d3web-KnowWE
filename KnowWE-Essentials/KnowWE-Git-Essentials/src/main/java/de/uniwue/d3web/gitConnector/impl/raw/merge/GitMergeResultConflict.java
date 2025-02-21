package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultConflict implements GitMergeCommandResult{
	public final String message;

	public GitMergeResultConflict(String message) {
		this.message = message;
	}
}
