/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;

public class GCPushDelegateParent implements GitConnectorPush {

	private final GitConnector parentGitConnector;

	public GCPushDelegateParent(GitConnector parentGitConnector) {
		this.parentGitConnector = parentGitConnector;
	}

	@Override
	public boolean pushAll(String userName, String passwordOrToken) {
		return parentGitConnector.pushAll(userName, passwordOrToken);
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		return parentGitConnector.pushBranch(branch, userName, passwordOrToken);
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		return parentGitConnector.pushToOrigin(userName, passwordOrToken);
	}
}
