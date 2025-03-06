/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

public interface GitConnectorRepo {

	/**
	 * Performs git garbage collection, which is thought to speed up subsequent git calls. Use with caution as this is slow!
	 * @param aggressive aggressive gc mode
	 * @param prune prune flag
	 */
	void performGC(boolean aggressive, boolean prune);


	/**
	 * Similar to GC() this is meant to speed up subsequent git calls. Use only if you know what it does
	 * @return true iff successful
	 */
	boolean executeCommitGraph();

	/**
	 * Obtain the directory in which the .git folder is located
	 * @return
	 */
	String getGitDirectory();


	/**
	 * Returns whether this repository has any remote origins assigned!
	 * @return
	 */
	boolean isRemoteRepository();


	/**
	 * returns the name of the repository.
	 *
	 * @return repo name
	 */
	String repoName();

}
