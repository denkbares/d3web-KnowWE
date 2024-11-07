package de.uniwue.d3web.gitConnector.impl.raw.log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.uniwue.d3web.gitConnector.impl.raw.gitexceptions.GitCommandResultSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.struct.Commit;

/**
 * On a successfull attempt of a "git log" command you will have a list of commits
 */
public final class GitLogCommandResultSuccess implements GitLogCommandResult, GitCommandResultSuccess {
	private List<Commit> commits;

	public GitLogCommandResultSuccess(List<Commit> commits) {
		this.commits = commits;
	}

	public static List<Commit> fromOutput(String output, GitLogCommand command) {
		List<Commit> commits = new ArrayList<>();
		for (String line : output.split("\n")) {
			if (line.trim().isEmpty()) {
				continue;
			}

			String[] split = line.split("\\|");

			if (split.length == 4 && command.isRetrieveMetadata()) {
				Commit commit = new Commit(split[0]);
				commit.setAuthor(split[1]);
				commit.setEmail(split[2]);
				commit.setCommitDate(Date.from(Instant.ofEpochSecond(Long.parseLong(split[3]))));

				commits.add(commit);
			}

			else if (split.length == 1 && !command.isRetrieveMetadata()) {
				commits.add(new Commit(split[0]));
			}

			else {
				//this is an error
				throw new IllegalStateException("");
			}
		}

		return Collections.unmodifiableList(commits);
	}

	public List<Commit> getCommits() {
		return new ArrayList<>(commits);
	}
}
