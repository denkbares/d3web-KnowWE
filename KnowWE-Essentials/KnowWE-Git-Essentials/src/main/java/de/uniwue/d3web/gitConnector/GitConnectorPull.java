/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

public interface GitConnectorPull {

	/**
	 * Pulls with rebase mode if specified
	 *
	 * @param rebase rebase mode
	 * @return true if successful
	 */
	default boolean call(boolean rebase) {
		return call(rebase, "origin");
	}

	boolean call(boolean rebase, String origin);

}
