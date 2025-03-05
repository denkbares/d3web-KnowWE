/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorBranch;

public class GCBranchDelegateParent implements GitConnectorBranch {
	private final GitConnector parentGitConnector;

	public GCBranchDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}
}
