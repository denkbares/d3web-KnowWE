package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultUnstagedChanges implements GitMergeCommandResult{
	public final String message;

	public GitMergeResultUnstagedChanges(String message) {
		this.message = message;
	}
}
