package de.uniwue.d3web.gitConnector.impl.raw.push;

import java.util.Map;

import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;
//TODO
public class PushCommand implements RawGitCommand<PushCommandResult> {
	private final String repositoryPath;
	private final String userName;
	private final String passwordOrToken;

	public PushCommand(String repositoryPath, String userName, String passwordOrToken) {
		this.repositoryPath = repositoryPath;
		this.userName = userName;
		this.passwordOrToken = passwordOrToken;
	}

	@Override
	public String[] getCommand() {
		return new String[0];
	}

	@Override
	public String getRepositoryPath() {
		return "";
	}

	@Override
	public Map<String, String> getEnvironmentParams() {
		return Map.of("LANG","en_US.UTF-8");
	}

	@Override
	public PushCommandResult execute() {
		return null;
	}
}
