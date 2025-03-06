/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

public interface GitConnectorStatus {

	/**
	 * In a git repo, one file is either committed, staged (added) or untracked.
	 * Or it does not exist in the first place.
	 */
	enum FileStatus {
		Committed_Clean, // file is committed and not changed since
		Committed_Modified, // file is committed but changed after last commit
		Committed_Deleted, // file is committed but file has just been deleted
		Staged,	// file was added but not yet committed // TODO: check if this is properly implemented: Staged vs. Committed_Modified?
		Untracked,	// file has not been added to git-vcs
		NotExisting // requested file does not exist on the file system
	}

	/**
	 * Returns the status of a given file
	 *
	 * @param file file
	 * @return status of the file in the current repo
	 */
	FileStatus ofFile(@NotNull String file);


	/**
	 * Obtain the status of the current active branch
	 *
	 * @return result
	 */
	GitStatusCommandResult get();

	/**
	 * A repo folder is clean when neither untracked nor uncommitted files exist.
	 *
	 * @return true iff clean
	 */
	boolean isClean();

}
