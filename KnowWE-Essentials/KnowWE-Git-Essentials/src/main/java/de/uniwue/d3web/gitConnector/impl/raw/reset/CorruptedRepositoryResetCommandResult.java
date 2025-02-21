package de.uniwue.d3web.gitConnector.impl.raw.reset;

public final class CorruptedRepositoryResetCommandResult implements ResetCommandResult{

	public final String failureString;

	public CorruptedRepositoryResetCommandResult(String failureString) {
		this.failureString = failureString;
	}
}
