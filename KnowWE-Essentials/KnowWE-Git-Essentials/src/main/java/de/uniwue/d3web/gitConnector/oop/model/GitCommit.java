package de.uniwue.d3web.gitConnector.oop.model;

import java.util.ArrayList;
import java.util.List;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;

public class GitCommit {

	public final String hash;
	public final GitBranch branch;

	private List<GitFile> affectedPaths;

	//7 fields metadata on a commit
	private String author;
	private String authorEmail;

	private String commiter;
	private String commiterEmail;

	private long commitTime;
	private long authorTime;

	private String message;

	public GitCommit(String hash, GitBranch gitBranch) {
		this.hash = hash;
		this.branch = gitBranch;
		this.affectedPaths = null;
	}

	public UserData getUserData() {
		return this.branch.getRepository().getGitConnector().userDataFor(this.hash);
	}

	public List<GitFile> getAffectedPaths() {
		if (affectedPaths == null) {
			affectedPaths = connector().listChangedFilesForHash(this.hash)
					.stream()
					.map(file -> new GitFile(this, file))
					.toList();
		}
		return affectedPaths;
	}

	private GitConnector connector() {
		return this.branch.getRepository().getGitConnector();
	}

	//TODO these arent correct
	public String getCommiter() {
		return commiter;
	}

	public long getCommitTime() {
		return commitTime;
	}

	public long getAuthorTime() {
		return authorTime;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getCommiterEmail() {
		return commiterEmail;
	}

	public String getMessage() {
		return message;
	}

	public String getAuthor() {
		return author;
	}
}
