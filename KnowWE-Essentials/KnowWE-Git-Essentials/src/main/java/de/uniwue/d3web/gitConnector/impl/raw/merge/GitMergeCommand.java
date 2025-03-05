package de.uniwue.d3web.gitConnector.impl.raw.merge;

import java.util.Map;

import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;

public class GitMergeCommand implements RawGitCommand<GitMergeCommandResult> {

	public final String repositoryPath;
	public final String branchName;

	public GitMergeCommand(String repositoryPath, String branchName) {
		this.repositoryPath = repositoryPath;
		this.branchName = branchName;
	}

	@Override
	public String[] getCommand() {
		return new String[]{"git","merge",branchName};
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
	public GitMergeCommandResult execute() {
		String result = RawGitExecutor.executeRawGitCommand(this);

		if(result.startsWith("Already up to date")){
			return new GitMergeResultSuccess();
		}

		if(result.startsWith("Merge made by")){
			return new GitMergeResultSuccess();
		}

		if(result.startsWith("Updating")){
			return new GitMergeResultSuccess();
		}

		if(result.startsWith("error: Merging is not possible because you have unmerged")){
			return new GitMergeResultUnmergedFiles(result);
		}


		if(result.startsWith("error: Your local changes to the following files would be overwritten")){
			return new GitMergeResultUnstagedChanges(result);
		}

		if(result.contains("CONFLICT")){
			return new GitMergeResultConflict(result);
		}


		return new GitMergeResultUnknownResult(result);
	}
}
