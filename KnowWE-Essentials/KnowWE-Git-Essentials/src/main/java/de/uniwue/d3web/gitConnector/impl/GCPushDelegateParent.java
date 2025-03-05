/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorPush;

public class GCPushDelegateParent implements GitConnectorPush {
	private final GitConnector parentGitConnector;

	public GCPushDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}
}
