package de.uniwue.d3web.gitConnector.impl.raw.status;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;

public class GitStatusCommand  implements RawGitCommand<GitStatusCommandResult> {

	private final String repositoryPath;

	public GitStatusCommand(@NotNull String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	@Override
	public String[] getCommand() {
		return new String[]{"git","status"};
	}

	@Override
	public String getRepositoryPath() {
		return repositoryPath;
	}

	@Override
	public Map<String, String> getEnvironmentParams() {
		return Map.of("LANG","en_US.UTF-8");
	}

	@Override
	public GitStatusCommandResult execute() {
		String output = RawGitExecutor.executeRawGitCommand(this);

		GitStatusResultSuccess gitStatusResult = GitStatusResultSuccess.fromOutput(output);
		return gitStatusResult;
	}
}
