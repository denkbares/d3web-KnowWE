package de.uniwue.d3web.gitConnector.impl;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;

/**
 * Basicly a wrapper around a GitConnector that also manages a cache. Its best understood with an example:
 * <p>
 * If a user wants all commits for file A, then you have no choice but to iterate the entire history of this repository.
 * But once this was done, we can store the list of commits alongside the timestamp of the last commit to the file.
 * As it is impossible to sneak commits into the history (or at least would be pretty much the same as hijacking), this
 * cache never has to be invalidated,
 */
public class CachingGitConnector implements GitConnector {

	private final GitHashCash cache;
	private final GitConnector delegate;

	public CachingGitConnector(GitConnector delegate) {
		this.delegate = delegate;
		this.cache = new GitHashCash();
	}

	@Override
	public List<String> commitHashesForFile(String file) {
		if (this.cache.contains(file)) {
			//build
		}
		//TODO
		return List.of();
	}

	@Override
	public List<String> commitHashesForFileSince(String file, Date date) {

		//TODO
		return List.of();
	}

	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		//TODO
		return "";
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		//TODO
		return 0;
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		//TODO
		return List.of();
	}

	@Override
	public boolean isClean() {
		return this.delegate.isClean();
	}

	@Override
	public byte[] getBytesForPath(String path, int version) {
		//TODO
		return new byte[0];
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		return this.delegate.getBytesForCommit(commitHash, path);
	}

	@Override
	public boolean versionExists(String path, int version) {
		return false;
	}

	@Override
	public UserData userDataFor(String commitHash) {
		return this.delegate.userDataFor(commitHash);
	}

	@Override
	public long commitTimeFor(String commitHash) {
		return this.delegate.commitTimeFor(commitHash);
	}

	@Override
	public void commitPathsForUser(String message, String author, String email, Set<String> paths) {
		this.delegate.commitPathsForUser(message, author, email, paths);
	}

	@Override
	public void moveFile(Path from, Path to, String user, String email, String message) {
		this.delegate.moveFile(from, to, user, email, message);
	}

	@Override
	public void deletePath(Path pathToDelete, UserData userData) {
		this.delegate.deletePath(pathToDelete, userData);
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		return this.delegate.changePath(pathToPut, userData);
	}

	@Override
	public void addPath(String path) {
		this.delegate.addPath(path);
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		this.delegate.rollbackPaths(pathsToRollback);
	}

	@Override
	public void performGC(boolean aggressive, boolean prune) {
		this.delegate.performGC(aggressive, prune);
	}

	@Override
	public boolean executeCommitGraph() {
		return this.delegate.executeCommitGraph();
	}

	@Override
	public void cherryPick(String branch, List<String> commitHashesToCherryPick) {
		this.delegate.cherryPick(branch, commitHashesToCherryPick);
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		return this.delegate.listChangedFilesForHash(commitHash);
	}
}
