package de.uniwue.d3web.gitConnector.impl.raw.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.raw.RawGitCommand;
import de.uniwue.d3web.gitConnector.impl.raw.struct.Commit;

public class GitLogCommand implements RawGitCommand<GitLogCommandResult> {

	private String repositoryPath;
	private Date commitsSinceDate;
	private String branch;
	private String commitHashFrom;
	private String commitHashTo;
	private String path;

	//flags of what should be retrieved of a single commit
	private boolean retrieveMetadata;
	private boolean retrieveCommitMessage;

	public GitLogCommand(String repositoryPath) {
		this.repositoryPath = repositoryPath;
		//by default we will only retrieve the hash
	}

	public void setRetrieveMetadata(boolean retrieveMetadata) {
		this.retrieveMetadata = retrieveMetadata;
	}

	public boolean isRetrieveMetadata() {
		return retrieveMetadata;
	}

	public void setRetrieveCommitMessage(boolean retrieveCommitMessage) {
		this.retrieveCommitMessage = retrieveCommitMessage;
	}

	public void setCommitsSinceDate(Date commitsSinceDate) {
		this.commitsSinceDate = commitsSinceDate;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setCommitHashFrom(String commitHashFrom) {
		this.commitHashFrom = commitHashFrom;
	}

	public void setCommitHashTo(String commitHashTo) {
		this.commitHashTo = commitHashTo;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String[] getCommand() {
		List<String> command = new ArrayList<>();

		command.add("git");
		command.add("log");

		if (branch != null) {
			command.add(branch);
		}

		//currently not possible to not retrieve the hash
		String formatCommand = "--format=%H";

		if (retrieveMetadata) {
			formatCommand += "|%an|%ae|%at";
		}
		command.add(formatCommand);

		if (commitsSinceDate != null) {
			command.add("--since=@" + (commitsSinceDate.getTime() / 1000L));
		}

		if (commitHashFrom != null && commitHashTo != null && commitsSinceDate == null) {
			command.add(commitHashFrom + ".." + commitHashTo);
		}

		if (path != null) {
			command.add("--");
			command.add(path);
		}

		return command.toArray(new String[0]);
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
	public GitLogCommandResult execute() {
		String output = new String(RawGitExecutor.executeGitCommandWithTempFile(getCommand(), this.repositoryPath));

		//this is most likely an exception (
		if (output.isEmpty()) {
			//retry the execution, but this time without any temp files as the temp files calls do not include error log!
			String out = RawGitExecutor.executeRawGitCommand(this);

			if (out.startsWith("fatal: not a git repository")) {
				return new GitLogNotAGitRepositoryResult();
			}

			if (out.startsWith("fatal: your current branch") && out.trim().endsWith("does not have any commits yet")) {
				return new GitLogGitRepositoryWithoutCommitsResult();
			}

			if (out.startsWith("fatal: ambiguous argument")) {
				int index = out.indexOf(": unknown revision");
				String ambiguousArg = "";
				if (index != -1) {
					ambiguousArg = out.substring(0, index)
							.replace("fatal: ambiguous argument", "")
							.replace("'", "")
							.trim();
				}
				return new GitLogAmbiguousArgumentResult(ambiguousArg);
			}

			return new GitLogUnknownResult(output);
		}
		List<Commit> commits = GitLogCommandResultSuccess.fromOutput(output, this);

		return new GitLogCommandResultSuccess(commits);
	}

	public static void main(String[] args) {
		String gitPath = "/Users/mkrug/Konap/testWiki";
		GitLogCommand gitLogCommand = new GitLogCommand(gitPath);
		gitLogCommand.setBranch("a2");
		gitLogCommand.setRetrieveMetadata(true);

		GitLogCommandResult execute = gitLogCommand.execute();
	}
}
