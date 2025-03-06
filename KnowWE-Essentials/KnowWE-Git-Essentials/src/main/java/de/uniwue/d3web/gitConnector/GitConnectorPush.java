/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;

public interface GitConnectorPush {

	/**
	 * Pushes all commit to origin.
	 *
	 * @return true if push was successful
	 */
	boolean pushAll(String userName, String passwordOrToken);

	default boolean pushAll() {
		return pushAll("", "");
	}
	/**
	 * Pushes the given branch to origin.
	 *
	 * @param branch the branch to be pushed
	 * @return true if push was successful
	 */
	boolean pushBranch(String branch, String userName, String passwordOrToken);


	// TODO: remove
	default boolean pushBranch(String branch) {
		return pushBranch(branch, "", "");
	}

	PushCommandResult pushToOrigin(String userName, String passwordOrToken);


}
