/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

public class DefaultGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGitServerConnector.class);

	private final String repoManagementServerURL;

	private final String gitRemoteURL;

	public DefaultGitServerConnector(String repoManagementServerURL, String gitRemoteURL) {
		this.repoManagementServerURL = repoManagementServerURL;
		this.gitRemoteURL = gitRemoteURL;
	}

	@Override
	public GitConnector getGitConnector(@NotNull String folder) {
		return JGitBackedGitConnector.fromPath(folder);
	}

	@Override
	public GitConnector getOrInitGitConnectorTo(@NotNull String pullTargetFolder, @NotNull String repoName, @Nullable String branch) {
		File gitDir = new File(pullTargetFolder);

		if (!gitDir.exists()) {
			// we want to pull a new repo into a new folder
			cloneRepo(repoName, branch, gitDir);
		}
		else {
			boolean success = switchFolderToOtherRepoAndBranch(pullTargetFolder, repoName, branch);
			if (!success) return null;
		}
		return getGitConnector(pullTargetFolder);
	}

	public String getGitRemoteURL() {
		return gitRemoteURL;
	}

	private boolean switchFolderToOtherRepoAndBranch(@NotNull String pullTargetFolder, @NotNull String repoName, @Nullable String branch) {
		GitConnector oldGitConnector = getGitConnector(pullTargetFolder);
		if (!oldGitConnector.repo().repoName().equals(repoName)) {
			// we want another repo than currently initialized

			if (!oldGitConnector.status().isClean()) {
				LOGGER.info("Can not switch Repo as old local repo is not clean");
				return false;
			}

			// delete old repo
			File gitDir = new File(pullTargetFolder);
			try {
				FileUtils.deleteDirectory(gitDir);
			}
			catch (IOException e) {
				LOGGER.error("Could not delete directory for repo change: " + gitDir + " : " + e.getMessage());
				throw new RuntimeException(e);
			}

			cloneRepo(repoName, branch, gitDir);
		}
		else {
			// this is a weird case that does a normal pull on the existing repo
		}
		return true;
	}

	private void cloneRepo(@NotNull String repoName, @Nullable String branch, File gitDir) {
		// initialize/clone new git connected to other repo
		String cloneBranch = (branch != null && !branch.isBlank()) ? branch : GitConnector.DEFAULT_BRANCH;
		try (Git result = Git.cloneRepository()
				.setURI(new File("").getAbsolutePath() + gitRemoteURL + repoName)
				.setBranch(cloneBranch)
				.setDirectory(gitDir)
				.call()) {
			// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
		}
		catch (GitAPIException e) {
			LOGGER.error("Git clone failed for repo url:" + gitRemoteURL);
			throw new RuntimeException(e);
		}
	}
}
