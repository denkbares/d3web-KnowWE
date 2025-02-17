/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import de.uniwue.d3web.gitConnector.GitConnector;

public interface GitServerConnector {

	/**
	 * Returns a GitConnector for the  main wiki folder connected to the specified repo.
	 *
	 * @param repoName name of the desired repo
	 * @param folder local folder path for the local repo
	 * @return main GitConnector
	 */
	GitConnector getOrInitGitConnectorTo(String folder, String repoName) ;

	/**
	 * Returns a GitConnector for the git repo currently living in the specified folder
	 *
	 * @param folder
	 * @return GitConnector
	 */
	GitConnector getGitConnector(String folder) ;
}
