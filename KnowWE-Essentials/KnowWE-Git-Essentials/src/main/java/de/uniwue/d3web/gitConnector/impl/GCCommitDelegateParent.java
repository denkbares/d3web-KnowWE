/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;
import de.uniwue.d3web.gitConnector.UserData;

class GCCommitDelegateParent implements GitConnectorCommit {
	private final GitConnector parentGitConnector;

	GCCommitDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public boolean unstage(@NotNull String file) {
		return parentGitConnector.unstage(file);
	}

	@Override
	public String commitPathsForUser(String message, String author, String email, Set<String> paths) {
		return parentGitConnector.commitPathsForUser(message, author, email, paths);
	}

	@Override
	public String commitForUser(UserData userData, long timeStamp) {
		return parentGitConnector.commitForUser(userData, timeStamp);
	}

	@Override
	public String commitForUser(UserData userData) {
		return parentGitConnector.commitForUser(userData);
	}

	@Override
	public void addPath(String path) {
		parentGitConnector.addPath(path);
	}

	@Override
	public void addPaths(List<String> path) {
		parentGitConnector.addPaths(path);
	}

	@Override
	public boolean addNoteToCommit(String noteText, String commitHash, String namespace) {
		return parentGitConnector.addNoteToCommit(noteText, commitHash, namespace);
	}

	@Override
	public boolean copyNotes(String commitHashFrom, String commitHashTo) {
		return parentGitConnector.copyNotes(commitHashFrom, commitHashTo);
	}

	@Override
	public String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached) {
		return parentGitConnector.deletePaths(pathsToDelete, userData, cached);
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		return parentGitConnector.changePath(pathToPut, userData);
	}

	@Override
	public boolean untrackPath(String path) {
		return parentGitConnector.untrackPath(path);
	}

	@Override
	public String moveFile(Path from, Path to, String user, String email, String message) {
		return parentGitConnector.moveFile(from, to, user, email, message);
	}

	@Override
	public String deletePath(String pathToDelete, UserData userData, boolean cached) {
		return parentGitConnector.deletePath(pathToDelete, userData, cached);
	}
}
