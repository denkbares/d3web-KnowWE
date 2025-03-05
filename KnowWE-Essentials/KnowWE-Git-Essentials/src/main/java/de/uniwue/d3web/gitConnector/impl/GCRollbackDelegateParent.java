/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;

public class GCRollbackDelegateParent implements GitConnectorRollback {
	private final GitConnector parentGitConnector;

	public GCRollbackDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}
}
