/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.mixed;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

class BackedGCStatus implements GitConnectorStatus {

	private final Supplier<GitConnectorStatus> supplier;
	private GitConnectorStatus delegate;

	BackedGCStatus(Supplier<GitConnectorStatus> supplier) {
		this.supplier = supplier;
	}

	@Override
	public FileStatus ofFile(@NotNull String file) {
		if(delegate == null) delegate = supplier.get();
		return delegate.ofFile(file);
	}

	@Override
	public GitStatusCommandResult get() {
		if(delegate == null) delegate = supplier.get();
		return delegate.get();
	}
}
