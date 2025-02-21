package de.uniwue.d3web.gitConnector.impl.raw.push;

public final class PushCommandUnresolvedAddress implements PushCommandResult{
	private final String message;

	public PushCommandUnresolvedAddress(String message) {
		this.message = message;
	}
}
