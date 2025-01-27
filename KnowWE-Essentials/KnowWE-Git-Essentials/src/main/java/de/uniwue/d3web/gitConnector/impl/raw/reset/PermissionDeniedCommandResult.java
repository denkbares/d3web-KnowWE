package de.uniwue.d3web.gitConnector.impl.raw.reset;

public final class PermissionDeniedCommandResult implements ResetCommandResult{

	public final String failureString;

	public PermissionDeniedCommandResult(String failureString) {
		this.failureString = failureString;
	}
}
