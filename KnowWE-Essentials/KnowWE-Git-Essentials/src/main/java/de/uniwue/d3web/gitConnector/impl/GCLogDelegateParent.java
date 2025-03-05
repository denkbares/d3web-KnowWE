/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorLog;

public class GCLogDelegateParent implements GitConnectorLog {

	private final GitConnector parentGitConnector;

	public GCLogDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}
}
