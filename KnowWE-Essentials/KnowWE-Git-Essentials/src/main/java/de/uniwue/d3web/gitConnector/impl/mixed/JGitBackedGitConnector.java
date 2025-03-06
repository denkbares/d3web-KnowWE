/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.mixed;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorBranch;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.UserData;
import de.uniwue.d3web.gitConnector.impl.GCDelegateParentFactory;
import de.uniwue.d3web.gitConnector.impl.GCFactory;
import de.uniwue.d3web.gitConnector.impl.GitConnectorParent;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.jgit.JGitConnector;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

/**
 * This implementation is a mix between a bare git implementation and JGIt implementation. We use bare git only if the
 * according JGit implementation is much slower (trust me on this one, there are orders of magnitude sometimes).
 * In essence all it does is to delegate between these two implementations in order to obtain a performant GitConnector
 */
public class JGitBackedGitConnector extends GitConnectorParent {
	private final BareGitConnector bareGitConnector;

	private final JGitConnector jgitConnector;

	public JGitBackedGitConnector(BareGitConnector bareGitConnector, JGitConnector jGitConnector) {
		this.bareGitConnector = bareGitConnector;
		this.jgitConnector = jGitConnector;
	}

	@Override
	protected @NotNull GCFactory getGcFactory() {
		return new BackedGCFactory(this);
	}

	public BareGitConnector getBareGitConnector() {
		return bareGitConnector;
	}

	public JGitConnector getJGitConnector() {
		return jgitConnector;
	}



	private class BackedGCFactory extends GCDelegateParentFactory {
		public BackedGCFactory(GitConnector gitConnector) {
			super(gitConnector);
		}

		@Override
		public GitConnectorStatus createStatus() {
			return new BackedGCStatus(() -> getBareGitConnector().status());
			///return new BackedGCStatus(() -> getJGitConnector().status());
		}

		@Override
		public GitConnectorPull createPull() {
			return new BackedGCPull(() -> getJGitConnector().pull());
		}

		@Override
		public GitConnectorPush createPush() {
			return new BackedGCPush(() -> getJGitConnector(), () -> getBareGitConnector());
		}
	}

	@Override
	public boolean executeCommitGraph() {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.executeCommitGraph();
		}
		return this.jgitConnector.executeCommitGraph();
	}

	@Override
	public String cherryPick(List<String> commitHashesToCherryPick) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.cherryPick(commitHashesToCherryPick);
		}
		else {
			return this.jgitConnector.cherryPick(commitHashesToCherryPick);
		}
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.listChangedFilesForHash(commitHash);
		}
		return this.jgitConnector.listChangedFilesForHash(commitHash);
	}

	@Override
	public String getGitDirectory() {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.getGitDirectory();
		}
		return this.jgitConnector.getGitDirectory();
	}

	@Override
	public String currentBranch() {
		return this.jgitConnector.currentBranch();
	}

	@Override
	public String currentHEAD() {
		return this.bareGitConnector.currentHEAD();
	}

	@Override
	public String currentHEADOfBranch(String branchName) {
		return this.bareGitConnector.currentHEADOfBranch(branchName);
	}

	@Override
	public List<String> commitsBetween(String commitHashFrom, String commitHashTo) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitsBetween(commitHashFrom, commitHashTo);
		}
		return this.jgitConnector.commitsBetween(commitHashFrom, commitHashTo);
	}

	@Override
	public List<String> commitsBetweenForFile(String commitHashFrom, String commitHashTo, String path) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitsBetweenForFile(commitHashFrom, commitHashTo, path);
		}
		return this.jgitConnector.commitsBetweenForFile(commitHashFrom, commitHashTo, path);
	}

	@Override
	public boolean isIgnored(String path) {
		return this.jgitConnector.isIgnored(path);
	}

	@Override
	public String commitForUser(UserData userData) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitForUser(userData);
		}
		return this.jgitConnector.commitForUser(userData);
	}

	@Override
	public String commitForUser(UserData userData, long timeStamp) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitForUser(userData, timeStamp);
		}
		return this.jgitConnector.commitForUser(userData, timeStamp);
	}

	@Override
	public boolean isRemoteRepository() {
		return this.jgitConnector.isRemoteRepository();
	}

	@Override
	public List<String> listBranches(boolean includeRemoteBranches) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.listBranches(includeRemoteBranches);
		}
		return this.jgitConnector.listBranches(includeRemoteBranches);
	}

	@Override
	public List<String> listCommitsForBranch(String branchName) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.listCommitsForBranch(branchName);
		}
		return this.jgitConnector.listCommitsForBranch(branchName);
	}

	@Override
	public boolean switchToBranch(String branch, boolean createBranch) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.switchToBranch(branch, createBranch);
		}
		return this.jgitConnector.switchToBranch(branch, createBranch);
	}

	@Override
	public boolean switchToTag(String tagName) {
		return this.bareGitConnector.switchToTag(tagName);
	}



	@Override
	public String repoName() {
		return this.bareGitConnector.repoName();
	}

	@Override
	public boolean setUpstreamBranch(String branch) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.setUpstreamBranch(branch);
		}
		return this.jgitConnector.setUpstreamBranch(branch);
	}

	@Override
	public boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.createBranch(branchName, branchNameToBaseOn, switchToBranch);
		}
		return this.jgitConnector.createBranch(branchName, branchNameToBaseOn, switchToBranch);
	}

	@Override
	public boolean untrackPath(String path) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.untrackPath(path);
		}
		return this.jgitConnector.untrackPath(path);
	}

	@Override
	public boolean addNoteToCommit(String noteText, String commitHash, String namespace) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.addNoteToCommit(noteText, commitHash, namespace);
		}
		;
		return this.jgitConnector.addNoteToCommit(noteText, commitHash, namespace);
	}

	@Override
	public boolean copyNotes(String commitHashFrom, String commitHashTo) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.copyNotes(commitHashFrom, commitHashTo);
		}
		return this.jgitConnector.copyNotes(commitHashFrom, commitHashTo);
	}

	@Override
	public Map<String, String> retrieveNotesForCommit(String commitHash) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.retrieveNotesForCommit(commitHash);
		}
		return this.jgitConnector.retrieveNotesForCommit(commitHash);
	}




	public GitStatusCommandResult getStatus() {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.status().get();
		}
		return this.jgitConnector.status().get();
	}

	@Override
	public void abortCherryPick() {
		if (this.bareGitConnector.isGitInstalled) {
			this.bareGitConnector.abortCherryPick();
		}
		else {
			this.jgitConnector.abortCherryPick();
		}
	}

	@Override
	public GitMergeCommandResult mergeBranchToCurrentBranch(String branchName) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.mergeBranchToCurrentBranch(branchName);
		}
		return this.jgitConnector.mergeBranchToCurrentBranch(branchName);
	}



	@Override
	public ResetCommandResult resetToHEAD() {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.resetToHEAD();
		}
		return this.jgitConnector.resetToHEAD();
	}

	@Override
	public boolean resetFile(String file) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.resetFile(file);
		}
		return this.jgitConnector.resetFile(file);
	}



	@Override
	public boolean unstage(@NotNull String file) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.unstage(file);
		}
		return this.jgitConnector.unstage(file);
	}


	@Override
	public List<String> commitHashesForFile(@NotNull String file) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashesForFile(file);
		}
		return this.jgitConnector.commitHashesForFile(file);
	}

	@Override
	public List<String> commitHashesForFileInBranch(@NotNull String file, String branchName) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashesForFileInBranch(file, branchName);
		}
		return this.jgitConnector.commitHashesForFileInBranch(file, branchName);
	}

	@Override
	public List<String> commitHashesForFileSince(@NotNull String file, @NotNull Date date) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.commitHashesForFileSince(file, date);
		}
		return this.jgitConnector.commitHashesForFileSince(file, date);
	}

	@Override
	public boolean gitInstalledAndReady() {
		return bareGitConnector.gitInstalledAndReady() && jgitConnector.gitInstalledAndReady();
	}

	@Override
	public void destroy() {
		this.bareGitConnector.destroy();
		this.jgitConnector.destroy();
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
		//TODO verify that this is indeed correct!
		return this.jgitConnector.getBytesForCommit(commitHash, path);
	}

	@Override
	public long getFilesizeForCommit(String commitHash, String path) {
		if (bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.getFilesizeForCommit(commitHash, path);
		}
		return this.jgitConnector.getFilesizeForCommit(commitHash, path);
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
			return;
		}
		this.jgitConnector.performGC(aggressive, prune);
	}

	@Override
	public String commitPathsForUser(String message, String author, String email, Set<String> paths) {
		return this.jgitConnector.commitPathsForUser(message, author, email, paths);
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		this.jgitConnector.rollbackPaths(pathsToRollback);
	}

	@Override
	public String moveFile(Path from, Path to, String user, String email, String message) {
		return this.jgitConnector.moveFile(from, to, user, email, message);
	}

	@Override
	public String deletePath(String pathToDelete, UserData userData, boolean cached) {
		return this.jgitConnector.deletePath(pathToDelete, userData, cached);
	}

	@Override
	public String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.deletePaths(pathsToDelete, userData, cached);
		}
		return this.jgitConnector.deletePaths(pathsToDelete, userData, cached);
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		if (this.bareGitConnector.isGitInstalled) {
			return this.bareGitConnector.changePath(pathToPut, userData);
		}
		return this.jgitConnector.changePath(pathToPut, userData);
	}

	@Override
	public void addPath(String path) {
		if (this.bareGitConnector.isGitInstalled) {
			this.bareGitConnector.addPath(path);
			return;
		}
		this.jgitConnector.addPath(path);
	}

	@Override
	public void addPaths(List<String> path) {
		if (this.bareGitConnector.isGitInstalled) {
			this.bareGitConnector.addPaths(path);
			return;
		}
		this.jgitConnector.addPaths(path);
	}

	// ===== FACTORY====

	public static JGitBackedGitConnector fromPath(String path) {
		BareGitConnector bareGitConnector = BareGitConnector.fromPath(path);
		JGitConnector jGitConnector = JGitConnector.fromPath(path);

		return new JGitBackedGitConnector(bareGitConnector, jGitConnector);
	}
}
