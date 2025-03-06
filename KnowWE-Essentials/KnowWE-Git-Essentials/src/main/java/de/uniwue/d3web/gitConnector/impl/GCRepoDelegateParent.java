/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorRepo;

public class GCRepoDelegateParent implements GitConnectorRepo {
	private final GitConnector parentGitConnector;

	GCRepoDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public void performGC(boolean aggressive, boolean prune) {
		parentGitConnector.performGC(aggressive, prune);
	}

	@Override
	public boolean executeCommitGraph() {
		return parentGitConnector.executeCommitGraph();
	}

	@Override
	public String getGitDirectory() {
		return parentGitConnector.getGitDirectory();
	}

	@Override
	public boolean isRemoteRepository() {
		return parentGitConnector.isRemoteRepository();
	}

	@Override
	public String repoName() {
		return parentGitConnector.repoName();
	}
}
