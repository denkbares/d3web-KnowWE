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

	/**
	 * Returns the id of a repository the server chose for
	 *
	 * @param repoName
	 * @return
	 * @throws HttpException
	 */
	int getRepositoryId(String repoName, String httpUrl) throws HttpException;

	List<MergeRequest> listMergeRequests(int repositoryId, String sourceBranch) throws HttpException;

	List<MergeRequest> listMergeRequests(int repositoryId) throws HttpException;

	/**
	 * Creates a merge request in the specified repository to merge sourceBranch in targetBranch
	 *
	 * @param repositoryId
	 * @param sourceBranch
	 * @param targetBranch
	 * @return
	 * @throws RuntimeException
	 * @throws HttpException
	 */
	MergeRequest createMergeRequest(int repositoryId, String sourceBranch, String targetBranch) throws RuntimeException, HttpException;

	/**
	 * Merges a merge request using the id of an already started merge request
	 *
	 * @param repositoryId
	 * @param mergeRequestId
	 * @return
	 * @throws RuntimeException
	 * @throws HttpException
	 */
	MergeRequest mergeMergeRequest(int repositoryId, int mergeRequestId) throws RuntimeException, HttpException;

	record MergeRequest(int id, String name, String sourceBranch, String targetBranch, MergeRequestState state,
						MergeRequestStatus mergeStatus, String url) {
	}

	/**
	 * Whether a merge request was merged or not
	 */
	enum MergeRequestState {
		OPENED,
		REOPENED,
		CLOSED,
		LOCKED,
		MERGED
	}

	/**
	 * Whether a merge request can be merged (at some point) or not
	 */
	enum MergeRequestStatus {
		UNCHECKED,
		CHECKING,
		CAN_BE_MERGED,
		CANNOT_BE_MERGED,
		CANNOT_BE_MERGED_RECHECK
	}
}
