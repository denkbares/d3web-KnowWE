/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.mixed;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPull;

public class BackedGCPull implements GitConnectorPull {

	private final Supplier<GitConnectorPull> supplier;
	private GitConnectorPull delegate;

	BackedGCPull(Supplier<GitConnectorPull> supplier) {
		this.supplier = supplier;
	}

	@Override
	public boolean call(boolean rebase) {
		if(delegate == null) delegate = supplier.get();
		return delegate.call(rebase);
	}
}
