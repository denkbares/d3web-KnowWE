/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.util.Set;

import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;

public interface GitConnectorRollback {

	void rollbackPaths(Set<String> pathsToRollback);

	ResetCommandResult resetToHEAD();

	/**
	 * Resets a modified (or deleted) file to the last committed state.
	 *
	 * @param file file to be reset
	 * @return true iff reset was successful
	 */
	boolean resetFile(String file);
}
