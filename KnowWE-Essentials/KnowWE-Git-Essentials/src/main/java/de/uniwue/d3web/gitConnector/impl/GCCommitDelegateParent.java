/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;

public class GCCommitDelegateParent implements GitConnectorCommit {
	private final GitConnector parentGitConnector;

	public GCCommitDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}
}
