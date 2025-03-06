/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.UserData;

public class GCLogDelegateParent implements GitConnectorLog {

	private final GitConnector parentGitConnector;

	public GCLogDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public List<String> commitHashesForFile(@NotNull String file) {
		return parentGitConnector.commitHashesForFile(file);
	}

	@Override
	public List<String> commitHashesForFileInBranch(@NotNull String file, String branchName) {
		return parentGitConnector.commitHashesForFileInBranch(file, branchName);
	}

	@Override
	public List<String> commitHashesForFileSince(@NotNull String file, @NotNull Date date) {
		return parentGitConnector.commitHashesForFileSince(file, date);
	}

	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		return parentGitConnector.commitHashForFileAndVersion(file, version);
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		return parentGitConnector.numberOfCommitsForFile(filePath);
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		return parentGitConnector.getBytesForCommit(commitHash, path);
	}

	@Override
	public long getFilesizeForCommit(String commitHash, String path) {
		return parentGitConnector.getFilesizeForCommit(commitHash, path);
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		return parentGitConnector.getCommitsSince(timeStamp);
	}

	@Override
	public byte[] getBytesForPath(String path, int version) {
		return parentGitConnector.getBytesForPath(path, version);
	}

	@Override
	public boolean versionExists(String path, int version) {
		return parentGitConnector.versionExists(path, version);
	}

	@Override
	public UserData userDataFor(String commitHash) {
		return parentGitConnector.userDataFor(commitHash);
	}

	@Override
	public long commitTimeFor(String commitHash) {
		return parentGitConnector.commitTimeFor(commitHash);
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		return parentGitConnector.listChangedFilesForHash(commitHash);
	}

	@Override
	public String currentHEAD() {
		return parentGitConnector.currentHEAD();
	}

	@Override
	public List<String> commitsBetween(String commitHashFrom, String commitHashTo) {
		return parentGitConnector.commitsBetween(commitHashFrom, commitHashTo);
	}

	@Override
	public List<String> commitsBetweenForFile(String commitHashFrom, String commitHashTo, String path) {
		return parentGitConnector.commitsBetweenForFile(commitHashFrom, commitHashTo, path);
	}

	@Override
	public List<String> listCommitsForBranch(String branchName) {
		return parentGitConnector.listCommitsForBranch(branchName);
	}

	@Override
	public Map<String, String> retrieveNotesForCommit(String commitHash) {
		return parentGitConnector.retrieveNotesForCommit(commitHash);
	}
}
