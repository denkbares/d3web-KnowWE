/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import org.apache.commons.lang.NotImplementedException;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorBranch;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;

public class GCDelegateParentFactory implements GCFactory {

	protected final GitConnector parentGitConnector;

	public GCDelegateParentFactory(GitConnector gitConnector) {
		this.parentGitConnector = gitConnector;
	}

	@Override
	public GitConnectorStatus createStatus() {
		//return new GCStatusDelegateParent(parentGitConnector);
		throw new NotImplementedException();
	}

	@Override
	public GitConnectorPull createPull() {
		return new GCPullDelegateParent(parentGitConnector);
	}

	@Override
	public GitConnectorPush createPush() {
		return new GCPushDelegateParent(parentGitConnector);
	}

	@Override
	public GitConnectorRollback createRollback() {
		return new GCRollbackDelegateParent(parentGitConnector);
	}

	@Override
	public GitConnectorLog createLog() {
		return new GCLogDelegateParent(parentGitConnector);
	}

	@Override
	public GitConnectorCommit createCommit() {
		return new GCCommitDelegateParent(parentGitConnector);
	}

	@Override
	public GitConnectorBranch createBranch() {
		return new GCBranchDelegateParent(parentGitConnector);
	}
}
