/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.File;
import java.util.List;

import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.uniwue.d3web.gitConnector.GitConnector;

public interface GitServerConnector {

	/**
	 * Returns a GitConnector connected to the specified repo.
	 * If folder.equals(repoName), then the behavior is equal to
	 * {@link GitServerConnector#getGitConnector(String folder)}
	 *
	 * @param repoName name of the desired repo
	 * @param folder   local folder path for the local repo
	 * @return main GitConnector
	 */
	default GitConnector getOrInitGitConnectorTo(@NotNull String folder, @NotNull String repoName) {
		return getOrInitGitConnectorTo(folder, repoName, null);
	}

	public String getGitRemoteURL();

	/**
	 * Returns a GitConnector connected to the specified repo.
	 * If folder.equals(repoName), then the behavior is equal to
	 * {@link GitServerConnector#getGitConnector(String folder)}
	 *
	 * @param repoName name of the desired repo
	 * @param folder   local folder path for the local repo
	 * @param branch   the desired branch
	 * @return main GitConnector
	 */
	GitConnector getOrInitGitConnectorTo(@NotNull String folder, @NotNull String repoName, @Nullable String branch);

	/**
	 * Returns a GitConnector for the git repo currently living in the specified folder
	 *
	 * @param folder
	 * @return GitConnector
	 */
	GitConnector getGitConnector(@NotNull String folder);

	/**
	 * Clones a repository with credentials taken from the instance
	 *
	 * @param url
	 * @param savePath
	 * @throws Exception
	 */
	void cloneRepository(String url, File savePath) throws RuntimeException;

	/**
	 * Clones a repository with credentials taken from the instance without any branches
	 *
	 * @param url
	 * @param savePath
	 * @throws Exception
	 */
	void cloneRepositoryShallow(String url, File savePath) throws RuntimeException;

	/**
	 * Returns a list with all repositories of the server.
	 *
	 * @return
	 * @throws HttpException
	 */
	List<RepositoryInfo> listRepositories() throws HttpException;
}
