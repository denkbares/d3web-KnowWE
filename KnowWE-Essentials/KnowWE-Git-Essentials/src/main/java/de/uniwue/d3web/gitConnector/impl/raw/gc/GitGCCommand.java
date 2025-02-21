package de.uniwue.d3web.gitConnector.impl.raw.gc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uniwue.d3web.gitConnector.impl.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;

public class GitGCCommand implements RawGitCommand<GitGCCommandResult> {

	private final String repositoryPath;

	private GCStrategy gcStrategy;

	public GitGCCommand(String repositoryPath, GCStrategy strategy) {
		this.repositoryPath = repositoryPath;
		this.gcStrategy = strategy;
	}

	public GitGCCommand(String repositoryPath) {
		this(repositoryPath, GCStrategy.None);
	}

	@Override
	public String[] getCommand() {
		List<String> commandArgs = new ArrayList<>();
		commandArgs.add("git");
		commandArgs.add("gc");

		switch (gcStrategy) {
			case None: {/* DO NOTHING */
				break;
			}
			case Auto:
				commandArgs.add("--auto");
				break;
			case Force:
				commandArgs.add("--force");
				break;
			case Aggressive:
				commandArgs.add("--aggressive");
		}

		return commandArgs.toArray(new String[0]);
	}

	@Override
	public String getRepositoryPath() {
		return repositoryPath;
	}

	@Override
	public Map<String, String> getEnvironmentParams() {
		return Map.of("LANG", "en_US.UTF-8");
	}

	@Override
	public GitGCCommandResult execute() {
		String output = RawGitExecutor.executeRawGitCommand(this);

		if(output.isEmpty()){
			return new GitGCCommandResultSuccess();
		}
		if(output.startsWith("Nothing new to pack.")){
			return new GitGCCommandResultSuccess();
		}

		return new GitGCUnknownResultSuccess(output);
	}

	public static void main(String[] args) {
		String repositoryPath ="/Users/mkrug/Konap/testWiki";

		GitGCCommand gitGCCommand = new GitGCCommand(repositoryPath, GCStrategy.Aggressive);
		GitGCCommandResult execute = gitGCCommand.execute();
	}
}
