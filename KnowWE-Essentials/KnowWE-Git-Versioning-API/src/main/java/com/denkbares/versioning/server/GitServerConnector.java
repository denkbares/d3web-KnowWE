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

	String getBranchURL(GitConnector gitConnector);

	List<MergeRequest> listMergeRequests(int repositoryId, String sourceBranch) throws HttpException;

	List<MergeRequest> listMergeRequests(int repositoryId) throws HttpException;

	/**
	 * Creates a merge request in the specified repository to merge sourceBranch in targetBranch. Asking again for a
	 * merge request that is open already answers the open one instead of failing, so a repeated attempt to integrate
	 * a branch continues where the last one stopped.
	 *
	 * @param repositoryId the repository the merge request is raised in
	 * @param sourceBranch the branch to be integrated
	 * @param targetBranch the branch it is integrated into
	 * @return the merge request, newly created or open already
	 * @throws GitServerException naming why the merge request could not be had, among it
	 *                            {@link GitServerException.Reason#MERGE_REQUEST_EXISTS} for a branch that is open
	 *                            against another target already
	 */
	MergeRequest createMergeRequest(int repositoryId, String sourceBranch, String targetBranch) throws RuntimeException, HttpException;

	/**
	 * Merges a merge request using the id of an already started merge request
	 *
	 * @param repositoryId   the repository the merge request lives in
	 * @param mergeRequestId the id of the merge request to merge
	 * @param commitMessage  the message of the resulting commit
	 * @return the merge request as it stands after the merge
	 * @throws GitServerException naming why the merge did not happen, among it
	 *                            {@link GitServerException.Reason#NOT_MERGEABLE} and
	 *                            {@link GitServerException.Reason#CONFLICT}
	 */
	MergeRequest mergeMergeRequest(int repositoryId, int mergeRequestId, String commitMessage) throws RuntimeException, HttpException;

	/**
	 * A merge request as far as it is of interest here.
	 *
	 * @param detailedMergeStatus what the server says about the mergeability beyond the coarse status, such as a
	 *                            pipeline or an approval that is still pending, or null where it says nothing
	 */
	record MergeRequest(int id, String name, String sourceBranch, String targetBranch, MergeRequestState state,
	                    MergeRequestStatus mergeStatus, @Nullable String detailedMergeStatus, String url) {

		/**
		 * Whether this merge request is still to be decided, so a new one for the same branch cannot be had.
		 */
		public boolean isOpen() {
			return state == MergeRequestState.OPENED || state == MergeRequestState.REOPENED
					|| state == MergeRequestState.LOCKED;
		}

		/**
		 * Whether the mergeability of this merge request is still being computed.
		 */
		public boolean isCheckPending() {
			return mergeStatus == MergeRequestStatus.UNCHECKED || mergeStatus == MergeRequestStatus.CHECKING;
		}

		/**
		 * Whether this merge request holds changes that contradict the target branch.
		 */
		public boolean hasConflict() {
			return mergeStatus == MergeRequestStatus.CANNOT_BE_MERGED
					|| mergeStatus == MergeRequestStatus.CANNOT_BE_MERGED_RECHECK;
		}
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
