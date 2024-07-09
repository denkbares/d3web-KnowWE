package de.uniwue.d3web.gitConnector.impl;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;

/**
 * This implementation is a mix between a bare git implementation and JGIt implementation. We use bare git only if the
 * according JGit implementation is much slower (trust me on this one, there are orders of magnitude sometimes).
 * In essence all it does is to delegate between these two implementations in order to obtain a performant GitConnector
 */
public class JGitBackedGitConnector implements GitConnector {
	private final BareGitConnector bareGitConnector;
	private final JGitConnector jgitConnector;

	public JGitBackedGitConnector(BareGitConnector bareGitConnector, JGitConnector jGitConnector) {
		this.bareGitConnector = bareGitConnector;
		this.jgitConnector = jGitConnector;
	}

	@Override
	public boolean executeCommitGraph() {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.executeCommitGraph();
		}
		return this.jgitConnector.executeCommitGraph();
	}

	@Override
	public void cherryPick(String branch, List<String> commitHashesToCherryPick) {
		this.jgitConnector.cherryPick(branch, commitHashesToCherryPick);
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.listChangedFilesForHash(commitHash);
		}
		return this.jgitConnector.listChangedFilesForHash(commitHash);
	}

	@Override
	public List<String> commitHashesForFile(String file) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashesForFile(file);
		}
		return this.jgitConnector.commitHashesForFile(file);
	}

	@Override
	public List<String> commitHashesForFileSince(String file, Date date) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashesForFileSince(file, date);
		}
		return this.jgitConnector.commitHashesForFileSince(file, date);
	}

	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashForFileAndVersion(file, version);
		}
		return this.jgitConnector.commitHashForFileAndVersion(file, version);
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.numberOfCommitsForFile(filePath);
		}

		return this.jgitConnector.numberOfCommitsForFile(filePath);
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.getCommitsSince(timeStamp);
		}
		return this.jgitConnector.getCommitsSince(timeStamp);
	}

	@Override
	public boolean isClean() {
		//just use JGit as it has always been fast
		return this.jgitConnector.isClean();
	}

	@Override
	public byte[] getBytesForPath(String path, int version) {
		//obtaining the commithash is way faster with the bare version
		String commitHash = null;
		if (bareGitConnector.isGitInstalled) {
			commitHash = this.bareGitConnector.commitHashForFileAndVersion(path, version);
		}
		else {
			commitHash = this.jgitConnector.commitHashForFileAndVersion(path, version);
		}
		if (commitHash == null) {
			LOGGER.error("Could not get commit hash for " + path + " Returning empty bytes");
			return new byte[0];
		}

		//getting the bytes is fine using jgit
		return this.getBytesForCommit(commitHash, path);
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.getBytesForCommit(commitHash, path);
		}
		return this.jgitConnector.getBytesForCommit(commitHash, path);
	}

	@Override
	public boolean versionExists(String path, int version) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.versionExists(path, version);
		}

		return this.jgitConnector.versionExists(path, version);
	}

	@Override
	public UserData userDataFor(String commitHash) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.userDataFor(commitHash);
		}
		return this.jgitConnector.userDataFor(commitHash);
	}

	@Override
	public long commitTimeFor(String commitHash) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitTimeFor(commitHash);
		}
		return this.jgitConnector.commitTimeFor(commitHash);
	}

	@Override
	public void performGC(boolean aggressive, boolean prune) {
		if (bareGitConnector.isGitInstalled) {
			this.bareGitConnector.performGC(aggressive, prune);
		}
		this.jgitConnector.performGC(aggressive, prune);
	}

	@Override
	public void commitPathsForUser(String message, String author, String email, Set<String> paths) {
		this.jgitConnector.commitPathsForUser(message, author, email, paths);
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		this.jgitConnector.rollbackPaths(pathsToRollback);
	}

	@Override
	public void moveFile(Path from, Path to, String user, String email, String message) {
		this.jgitConnector.moveFile(from, to, user, email, message);
	}

	@Override
	public void deletePath(Path pathToDelete, UserData userData) {
		this.jgitConnector.deletePath(pathToDelete, userData);
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		return this.jgitConnector.changePath(pathToPut, userData);
	}

	@Override
	public void addPath(String path) {
		this.jgitConnector.addPath(path);
	}

	// ===== FACTORY====

	public static JGitBackedGitConnector fromPath(String path) {
		BareGitConnector bareGitConnector = BareGitConnector.fromPath(path);
		JGitConnector jGitConnector = JGitConnector.fromPath(path);

		return new JGitBackedGitConnector(bareGitConnector, jGitConnector);
	}
}
