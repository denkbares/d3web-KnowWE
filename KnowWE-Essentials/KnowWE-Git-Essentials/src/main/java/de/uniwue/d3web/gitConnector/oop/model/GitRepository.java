package de.uniwue.d3web.gitConnector.oop.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import de.uniwue.d3web.gitConnector.GitConnector;

public class GitRepository {

	private final GitConnector gitConnector;

	public GitRepository(GitConnector gitConnector) {
		this.gitConnector = gitConnector;
	}

	public GitConnector getGitConnector() {
		return gitConnector;
	}

	public List<GitBranch> getLocalBranches() {
		return this.gitConnector.listBranches(false)
				.stream().map(it -> new GitBranch(it, this, false)).toList();
	}

	public Optional<GitBranch> getCurrentLocalBranch() {
		String currentBranch = this.gitConnector.currentBranch();
		if (currentBranch == null) {
			return Optional.empty();
		}
		return Optional.of(new GitBranch(currentBranch, this, false));
	}

	public Optional<GitBranch> switchToBranch(String branchName, boolean createIfNotExist) {
		boolean success = this.gitConnector.switchToBranch(branchName, createIfNotExist);
		if (success) {
			return Optional.of(new GitBranch(branchName, this, false));
		}
		return Optional.empty();
	}

	public Optional<GitTag> switchToTag(String tagName, boolean createIfNotExist) {
		boolean success = this.gitConnector.switchToTag(tagName);
		if (success) {
			return Optional.of(new GitTag(tagName, this));
		}
		return Optional.empty();
	}

	public void executeGC(boolean aggressive, boolean prune) {
		this.gitConnector.performGC(aggressive, prune);
	}

	public Path getGitDirectory() {
		return Path.of(this.gitConnector.getGitDirectory());
	}

	public GitStagingArea getStagingArea(){
		return new GitStagingArea(this);
	}
}
