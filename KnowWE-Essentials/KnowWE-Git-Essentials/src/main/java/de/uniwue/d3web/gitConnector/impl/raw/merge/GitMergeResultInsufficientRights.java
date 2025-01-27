package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultInsufficientRights implements GitMergeCommandResult{
	public final String message;

	public GitMergeResultInsufficientRights(String message) {
		this.message = message;
	}
}
