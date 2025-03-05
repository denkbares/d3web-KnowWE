package de.uniwue.d3web.gitConnector.impl.raw.reset;

import java.util.Map;

import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;

public class ResetCommand implements RawGitCommand<ResetCommandResult> {

	public final String repositoryPath;

	public ResetCommand(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	@Override
	public String[] getCommand() {
		return new String[] { "git", "reset", "--hard", "HEAD" };
	}

	@Override
	public String getRepositoryPath() {
		return this.repositoryPath;
	}

	@Override
	public Map<String, String> getEnvironmentParams() {
		return Map.of("LANG", "en_US.UTF-8");
	}

	@Override
	public ResetCommandResult execute() {
		String output = RawGitExecutor.executeRawGitCommand(this);

		if (output.isEmpty() || output.contains("HEAD is now at")) {
			return new ResetCommandSuccess();
		}
		else if (output.contains("fatal: unable to read tree object")) {
			return new CorruptedRepositoryResetCommandResult(output);
		}

		else if (output.contains("Permission denied")) {
			return new PermissionDeniedCommandResult(output);
		}

		return new UnknownResetCommandResult(output);
	}
}
