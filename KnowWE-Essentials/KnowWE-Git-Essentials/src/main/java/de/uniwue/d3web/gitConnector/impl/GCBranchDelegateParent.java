/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import java.util.List;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorBranch;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;

public class GCBranchDelegateParent implements GitConnectorBranch {

	private final GitConnector parentGitConnector;

	GCBranchDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		return parentGitConnector.pushBranch(branch, userName, passwordOrToken);
	}

	@Override
	public boolean switchToBranch(String branch, boolean createBranch) {
		return parentGitConnector.switchToBranch(branch, createBranch);
	}

	@Override
	public boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch) {
		return parentGitConnector.createBranch(branchName, branchNameToBaseOn, switchToBranch);
	}

	@Override
	public List<String> listBranches(boolean includeRemoteBranches) {
		return parentGitConnector.listBranches(includeRemoteBranches);
	}

	@Override
	public String currentBranch() throws IllegalStateException {
		return parentGitConnector.currentBranch();
	}

	@Override
	public String currentHEADOfBranch(String branchName) {
		return parentGitConnector.currentHEADOfBranch(branchName);
	}

	@Override
	public boolean setUpstreamBranch(String branch) {
		return parentGitConnector.setUpstreamBranch(branch);
	}

	@Override
	public GitMergeCommandResult mergeBranchToCurrentBranch(String branchName) {
		return parentGitConnector.mergeBranchToCurrentBranch(branchName);
	}
}
