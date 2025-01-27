package de.uniwue.d3web.gitConnector.impl.raw.reset;

public final class UnknownResetCommandResult implements ResetCommandResult{

	public final String failureString;

	public UnknownResetCommandResult(String failureString) {
		this.failureString = failureString;
	}
}
