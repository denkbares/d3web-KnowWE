/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorBranch;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.GitConnectorRepo;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;

public abstract class GitConnectorParent implements GitConnector {

	private final GitConnectorStatus statusGC;
	private final GitConnectorPull pullGC;
	private final GitConnectorPush pushGC;
	private final GitConnectorLog logGC;
	private final GitConnectorBranch branchGC;
	private final GitConnectorRollback rollbackGC;
	private final GitConnectorCommit commitGC;
	private final GitConnectorRepo repoGC;

	public GitConnectorParent() {
		GCFactory gcFactory = getGcFactory();
		this.statusGC = gcFactory.createStatus();
		this.pullGC = gcFactory.createPull();
		this.pushGC = gcFactory.createPush();
		this.logGC = gcFactory.createLog();
		this.branchGC = gcFactory.createBranch();
		this.rollbackGC = gcFactory.createRollback();
		this.commitGC = gcFactory.createCommit();
		this.repoGC = gcFactory.createRepo();
	}

	protected @NotNull GCFactory getGcFactory() {
		return new GCDelegateParentFactory(this);
	}

	@Override
	public GitConnectorCommit commit() {
		return commitGC;
	}

	@Override
	public GitConnectorStatus status() {
		return statusGC;
	}

	@Override
	public GitConnectorLog log() {
		return logGC;
	}

	@Override
	public GitConnectorBranch branches() {
		return branchGC;
	}

	@Override
	public GitConnectorPull pull() {
		return pullGC;
	}

	@Override
	public GitConnectorPush push() {
		return pushGC;
	}

	@Override
	public GitConnectorRollback rollback() {
		return rollbackGC;
	}

	@Override
	public GitConnectorRepo repo() {
		return repoGC;
	}
}
