/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.cached;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;

public class CachedGCPush implements GitConnectorPush {

	private GitConnectorPush delegate;
	private final Supplier<GitConnectorPush> delegateS;

	CachedGCPush(Supplier<GitConnectorPush> delegate) {
		this.delegateS = delegate;
	}

	@Override
	public boolean pushAll() {
		if(delegate == null) delegate = delegateS.get();
		return this.delegate.pushAll();
	}

	@Override
	public boolean pushBranch(String branch) {
		if(delegate == null) delegate = delegateS.get();
		return this.delegate.pushBranch(branch);
	}

	@Override
	public boolean pushAll(String userName, String passwordOrToken) {
		if(delegate == null) delegate = delegateS.get();
		return delegate.pushAll(userName, passwordOrToken);
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		if(delegate == null) delegate = delegateS.get();
		return delegate.pushBranch(branch, userName, passwordOrToken);
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		if(delegate == null) delegate = delegateS.get();
		return delegate.pushToOrigin(userName,passwordOrToken);
	}

	@Override
	public boolean gitInstalledAndReady() {
		return delegate.gitInstalledAndReady();
	}
}
