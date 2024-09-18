package de.uniwue.d3web.gitConnector.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

	//the key is a branch name
	private final Map<String, GitHashCash> cache;
	private final GitConnector delegate;

	public CachingGitConnector(GitConnector delegate) {
		this.delegate = delegate;
		this.cache = new ConcurrentHashMap<>();
	}

	/**
	 * This is the most important method of this cache - it updates the cache for a given path.
	 * This is done as follows:
	 * We check the HEAD (which is a commit hash) of the current branch and compare it to the HEAD that was set, the
	 * last time we updated our cache
	 * If they agree, we can assume our cache is up to date. If they do not agree, we ask Git for all hashes between the
	 * two HEADS
	 * (as Git tracks them based on when they were applied to a given patch, this does work even if commits are merged
	 * that date back years ago)
	 * We obtain all files from these hashes and mark them as "dirty" (basicly this means we drop them from the cache
	 * -at this)
	 */
	private void updateCache() {
		//access cache for current branch
		GitHashCash currentCache = getCurrentCache();

		//compare HEAD of the cache with current HEAD of Git
		String headCache = currentCache.getHEAD();
		String headGit = this.currentHEAD();

		if (headCache == null) {
			//this means we do not have any entries yet, and therefore there is no need to invalidate!
			currentCache.setHEAD(headGit);
			return;
		}

		//nothing to do
		if (headCache.equals(headGit)) {
			return;
		}

		// loop over the files in all commits between these 2 heads and collect all files to invalidate
		Set<String> changedFiles = new HashSet<>();
		for (String commit : this.commitsBetween(headCache, headGit)) {
			changedFiles.addAll(this.listChangedFilesForHash(commit));
		}

		//invalidate the files
		for (String file : changedFiles) {
			if (currentCache.contains(file)) {
				updateCachForPathByCommitsBetween(file, currentCache);
			}
		}

		//and update the head
		currentCache.setHEAD(headGit);
	}

	private void updateCacheForPath(String path) {
		//we update our cache so that we can assure our HEADs are matching the git
		this.updateCache();

		//then obtain: we combine the currently stored history with potential new commits
		GitHashCash currentCache = getCurrentCache();

		if (!currentCache.contains(path)) {
			//slow call...
			List<String> hashes = this.delegate.commitHashesForFile(path);
			if (!hashes.isEmpty()) {
				currentCache.put(path, hashes);
			}
		}
		else {
			//path is in the cache so we only update in between!
			updateCachForPathByCommitsBetween(path, currentCache);
		}
	}

	private void updateCachForPathByCommitsBetween(String path, GitHashCash currentCache) {
		List<String> cachedHashes = currentCache.get(path);
		String headCache = cachedHashes.get(cachedHashes.size() - 1);

		List<String> updatedHistory = new ArrayList<>(cachedHashes);
		updatedHistory.addAll(this.delegate.commitsBetweenForFile(headCache, this.currentHEAD(), path));
		if (!updatedHistory.isEmpty()) {
			currentCache.put(path, updatedHistory);
		}
	}

	//obtains the cache for the current branch
	private GitHashCash getCurrentCache() {
		String currentBranch = this.currentBranch();
		if (!this.cache.containsKey(currentBranch)) {
			this.cache.put(currentBranch, new GitHashCash());
		}
		return this.cache.get(currentBranch);
	}

	@Override
	public List<String> commitHashesForFile(String file) {
		this.updateCacheForPath(file);
		return this.getCurrentCache().get(file);
	}

	@Override
	public List<String> commitHashesForFileSince(String file, Date date) {
		//TODO it is weird, but i currently can not do this efficiently with this implementation of the cache!
		return this.delegate.commitHashesForFileSince(file, date);
	}

	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		this.updateCacheForPath(file);

		List<String> strings = getCurrentCache().get(file);
		if(strings == null) {
			return null;
		}

		int versionIndex = version - 1;

		if (versionIndex >= strings.size()) {
			LOGGER.warn("Requested a version higher than available for file: " + file);
			return null;
		}
		String commitHash = null;
		if (version == -1) {
			commitHash = strings.get(strings.size() - 1);
		}
		else {
			commitHash = strings.get(version - 1);
		}
		if (commitHash == null) {
			LOGGER.error("Can not obtain the according git hash");
		}

		return commitHash;
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		this.updateCacheForPath(filePath);

		if (!this.getCurrentCache().contains(filePath)) {
			return 0;
		}

		return this.getCurrentCache().get(filePath).size();
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		//TODO yet again current implementation cannot do this efficiently!
		return this.delegate.getCommitsSince(timeStamp);
	}

	@Override
	public byte[] getBytesForPath(String path, int version) {
		this.updateCacheForPath(path);
		String commitHash = this.commitHashForFileAndVersion(path, version);
		if (commitHash == null) {
			return null;
		}
		return getBytesForCommit(commitHash, path);
	}

	@Override
	public boolean versionExists(String path, int version) {
		updateCacheForPath(path);
		List<String> commitHashes = this.getCurrentCache().get(path);
		return version > 0 && version <= commitHashes.size();
	}

	@Override
	public boolean isClean() {
		return this.delegate.isClean();
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		return this.delegate.getBytesForCommit(commitHash, path);
	}

	@Override
	public long getFilesizeForCommit(String commitHash, String path) {
		GitHashCash currentCache = getCurrentCache();
		if (currentCache.hasFilesizeFor(commitHash, path)) {
			return currentCache.getFilesizeFor(commitHash, path);
		}
		long filesize = this.delegate.getFilesizeForCommit(commitHash, path);

		currentCache.putFilesizeFor(commitHash, path, filesize);
		return filesize;
	}

	@Override
	public UserData userDataFor(String commitHash) {
		GitHashCash currentCache = getCurrentCache();
		if (currentCache.hasUserDataFor(commitHash)) {
			return currentCache.getUserDataFor(commitHash);
		}

		UserData userData = this.delegate.userDataFor(commitHash);
		currentCache.putUserDataFor(commitHash, userData);
		return userData;
	}

	@Override
	public long commitTimeFor(String commitHash) {
		GitHashCash currentCache = getCurrentCache();
		if (currentCache.hasTimestampFor(commitHash)) {
			return currentCache.getTimestampFor(commitHash);
		}
		long timestamp = this.delegate.commitTimeFor(commitHash);
		currentCache.putTimestampFor(commitHash, timestamp);
		return timestamp;
	}

	@Override
	public String commitPathsForUser(String message, String author, String email, Set<String> paths) {
		return this.delegate.commitPathsForUser(message, author, email, paths);
	}

	@Override
	public String moveFile(Path from, Path to, String user, String email, String message) {
		return this.delegate.moveFile(from, to, user, email, message);
	}

	@Override
	public String deletePath(String pathToDelete, UserData userData, boolean cached) {
		return this.delegate.deletePath(pathToDelete, userData,cached);
	}

	@Override
	public String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached) {
		return this.delegate.deletePaths(pathsToDelete, userData, cached);
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
	public void addPaths(List<String> path) {
		this.delegate.addPaths(path);
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

	@Override
	public String getGitDirectory() {
		return this.delegate.getGitDirectory();
	}

	@Override
	public String currentBranch() {
		return this.delegate.currentBranch();
	}

	@Override
	public String currentHEAD() {
		return this.delegate.currentHEAD();
	}

	@Override
	public List<String> commitsBetween(String commitHashFrom, String commitHashTo) {
		return this.delegate.commitsBetween(commitHashFrom, commitHashTo);
	}

	@Override
	public List<String> commitsBetweenForFile(String headCache, String s, String path) {
		return this.delegate.commitsBetweenForFile(headCache, s, path);
	}

	@Override
	public boolean isIgnored(String path) {
		return this.delegate.isIgnored(path);
	}

	@Override
	public String commitForUser(UserData userData) {
		return this.delegate.commitForUser(userData);
	}

	@Override
	public String commitForUser(UserData userData, long timeStamp) {
		return this.delegate.commitForUser(userData, timeStamp);
	}

	@Override
	public boolean isRemoteRepository() {
		return this.delegate.isRemoteRepository();
	}

	@Override
	public List<String> listBranches() {
		return this.delegate.listBranches();
	}

	@Override
	public List<String> listCommitsForBranch(String branchName) {
		return this.delegate.listCommitsForBranch(branchName);
	}

	@Override
	public boolean switchToBranch(String branch, boolean createBranch) {
		return this.delegate.switchToBranch(branch, createBranch);
	}
}
