/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

public interface GitConnectorInstallation {

	/**
	 * Checks if git is ready to go in the current runtime environment
	 *
	 * @return true if git is ready to go
	 */
	boolean gitInstalledAndReady();

}
