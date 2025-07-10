package de.uniwue.d3web.gitConnector.impl.raw.merge;

public final class GitMergeResultUnknownResult implements GitMergeCommandResult {
	private final String result;

	public GitMergeResultUnknownResult(String result) {
		this.result = result;
	}
}
