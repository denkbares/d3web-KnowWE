package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultUnmergedFiles implements GitMergeCommandResult{
	public final String message;

	public GitMergeResultUnmergedFiles(String message) {
		this.message = message;
	}
}
