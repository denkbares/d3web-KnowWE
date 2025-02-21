package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultNotAGit implements GitMergeCommandResult{
	public final String message;

	public GitMergeResultNotAGit(String message) {
		this.message = message;
	}
}
