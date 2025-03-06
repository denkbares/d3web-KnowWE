/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.cached;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

class CachedGCStatus implements GitConnectorStatus {

	private GitConnectorStatus delegate;
	private final Supplier<GitConnectorStatus> delegateS;

	CachedGCStatus(Supplier<GitConnectorStatus> delegate) {
		this.delegateS = delegate;
	}

	@Override
	public FileStatus ofFile(@NotNull String file) {
		if(delegate == null) delegate = delegateS.get();
		return delegate.ofFile(file);
	}

	@Override
	public GitStatusCommandResult get() {
		if(delegate == null) delegate = delegateS.get();
		return delegate.get();
	}

	@Override
	public boolean isClean() {
		if(delegate == null) delegate = delegateS.get();
		return this.delegate.isClean();
	}
}
