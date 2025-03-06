/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import java.util.Set;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;

public class GCRollbackDelegateParent implements GitConnectorRollback {
	private final GitConnector parentGitConnector;

	public GCRollbackDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		parentGitConnector.rollbackPaths(pathsToRollback);
	}

	@Override
	public ResetCommandResult resetToHEAD() {
		return parentGitConnector.resetToHEAD();
	}

	@Override
	public boolean resetFile(String file) {
		return parentGitConnector.resetFile(file);
	}
}
