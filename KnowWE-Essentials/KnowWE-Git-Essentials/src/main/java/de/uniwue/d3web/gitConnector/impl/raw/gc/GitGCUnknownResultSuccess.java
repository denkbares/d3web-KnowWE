package de.uniwue.d3web.gitConnector.impl.raw.gc;

import de.uniwue.d3web.gitConnector.impl.raw.gitexceptions.GitUnknownCommandResult;

public final class GitGCUnknownResultSuccess implements GitGCCommandResult, GitUnknownCommandResult {

	private String message;

	public GitGCUnknownResultSuccess(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
