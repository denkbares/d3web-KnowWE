/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.cached;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;

public class CachedGCPull implements GitConnectorPull {

	GitConnectorPull delegate;
	Supplier<GitConnectorPull> delegateS;

	public CachedGCPull(Supplier<GitConnectorPull> delegate) {
		this.delegateS = delegate;
	}

	@Override
	public boolean call(boolean rebase, String origin) {
		if(delegate == null) delegate = delegateS.get();
		return delegate.call(rebase, origin);
	}
}
