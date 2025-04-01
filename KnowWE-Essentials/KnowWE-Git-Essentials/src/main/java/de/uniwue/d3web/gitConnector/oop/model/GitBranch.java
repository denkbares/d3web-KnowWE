package de.uniwue.d3web.gitConnector.oop.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class GitBranch {

	public final String name;

	private final GitRepository repository;
	private final boolean isRemoteBranch;

	public GitBranch(String name, GitRepository gitRepository, boolean isRemoteBranch) {
		this.name = name;
		this.repository = gitRepository;
		this.isRemoteBranch = isRemoteBranch;
	}

	/**
	 * @return A list of all commits on this branch
	 */
	public List<GitCommit> commits() {
		return this.repository.getGitConnector()
				.listCommitsForBranch(this.name)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public List<GitCommit> commitsForPath(String path) {
		return this.repository.getGitConnector()
				.commitHashesForFile(path)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public List<GitCommit> commitsForPathSince(String path, Date date) {
		return this.repository.getGitConnector()
				.commitHashesForFileSince(path, date)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public List<GitCommit> commitsBetween(String commitHashFrom, String commitHashTo) {
		return this.repository.getGitConnector()
				.commitsBetween(commitHashFrom, commitHashTo)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public List<GitCommit> commitsBetweenForPath(String commitHashFrom, String commitHashTo, String path) {
		return this.repository.getGitConnector()
				.commitsBetweenForFile(commitHashFrom, commitHashTo, path)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public int numberOfCommitsForPath(String path) {
		return commitsForPath(path).size();
	}

	public List<GitCommit> commitsSince(String path) {
		return this.repository.getGitConnector()
				.commitHashesForFile(path)
				.stream()
				.map(hash -> new GitCommit(hash, this))
				.toList();
	}

	public boolean untrackPath(String path) {
		return this.repository.getGitConnector().untrackPath(path);
	}

	public boolean isIgnored(String path) {
		return this.repository.getGitConnector().isIgnored(path);
	}

	public GitRepository getRepository() {
		return repository;
	}

	public boolean pull(boolean rebase) {
		return this.repository.getGitConnector().pullCurrent(rebase);
	}

	public GitCommit getCurrentHead() {
		String head = this.repository.getGitConnector().currentHEAD();
		return new GitCommit(head, this);
	}

	public boolean versionExists(String path, int version) {
		return this.repository.getGitConnector().versionExists(path, version);
	}

	public String cherryPick(List<GitCommit> commits) {
		List<String> commitHashes = commits.stream().map(it -> it.hash).toList();
		return this.repository.getGitConnector().cherryPick(commitHashes);
	}

	public Optional<GitCommit> commitForPathAndVersion(String path, int version) {
		String commitHash = getRepository().getGitConnector().commitHashForFileAndVersion(path, version);
		if (commitHash != null) {
			return Optional.of(new GitCommit(commitHash, this));
		}
		return Optional.empty();
	}


	//TODO

//	String commitForUser(UserData userData);
//
//	/**
//	 * Performs a commit operation on the underlying git using the provided userData - returns the commit hash. You can
//	 * specify a timestamp.
//	 * The timestamp has to be specified as a Git timestamp which corresponds to "System.currentTimeMillis()/1000".
//	 *
//	 * @param userData
//	 * @return
//	 */
//	String commitForUser(UserData userData, long timeStamp);
}
