/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.util.List;

import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;

public interface GitConnectorBranch {

	/**
	 * Pushes the given branch to origin.
	 *
	 * @param branch the branch to be pushed
	 * @return true if push was successful
	 */
	boolean pushBranch(String branch, String userName, String passwordOrToken);

	default boolean pushBranch(String branch) {
		return pushBranch(branch, "", "");
	}

	/**
	 * switches to the specified branch and creates the branch if necessary. Returns true if successful
	 * @param branch
	 * @param createBranch
	 * @return
	 */
	boolean switchToBranch(String branch, boolean createBranch);


	boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch);

	/**
	 * List all branches of this repository
	 */
	List<String> listBranches(boolean includeRemoteBranches);


	/**
	 * Return the current active branch. Throws IllegalState if this command fails
	 * @return
	 */
	String currentBranch() throws IllegalStateException;

	/**
	 * Return the current head of the specified branch
	 *
	 * @return
	 */
	String currentHEADOfBranch(String branchName);

	/**
	 * Sets the upstream branch for the current branch
	 *
	 * @param branch branch on origin that the current branch is connected to
	 * @return true if successful
	 */
	boolean setUpstreamBranch(String branch);

	GitMergeCommandResult mergeBranchToCurrentBranch(String branchName);



}
