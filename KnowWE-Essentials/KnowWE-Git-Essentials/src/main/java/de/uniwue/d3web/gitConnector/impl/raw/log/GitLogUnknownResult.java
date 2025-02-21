package de.uniwue.d3web.gitConnector.impl.raw.log;

import de.uniwue.d3web.gitConnector.impl.raw.gitexceptions.GitUnknownCommandResult;

public final class GitLogUnknownResult implements GitLogCommandResult, GitUnknownCommandResult {

	private String message;

	public GitLogUnknownResult(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return this.message;
	}
}
