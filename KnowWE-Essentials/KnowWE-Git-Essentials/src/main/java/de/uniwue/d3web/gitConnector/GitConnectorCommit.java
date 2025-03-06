/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface GitConnectorCommit {

	/***
	 * Removes the file from stage (reverse 'git add')
	 *
	 * @param file file to be unstaged
	 * @return true iff it was unstaged
	 */
	boolean unstage(@NotNull String file);

	/**
	 * Commits the collections of files for the given user, mail and message.
	 * If no message is passed, an empty default message has to be generated.
	 *
	 * @param message message
	 * @param author author
	 * @param email email
	 * @param paths paths of files to be committed
	 * @return commit hash
	 */
	// TODO: BareGitConnector returns the complete command line response !
	String commitPathsForUser(String message, String author, String email, Set<String> paths);

	/**
	 * Performs a commit operation on the underlying git using the provided userData - returns the commit hash. You can specify a timestamp.
	 * The timestamp has to be specified as a Git timestamp which corresponds to "System.currentTimeMillis()/1000".
	 * @param userData
	 * @return
	 */
	String commitForUser(UserData userData, long timeStamp);


	/**
	 * Performs a commit operation on the underlying git using the provided userData - returns the commit hash.
	 *
	 * @param userData
	 * @return
	 */
	String commitForUser(UserData userData);


	/**
	 * Performs a git add <path>
	 * @param path file to be added
	 */
	void addPath(String path);

	void addPaths(List<String> path);

	boolean addNoteToCommit(String noteText, String commitHash, String namespace);

	boolean copyNotes(String commitHashFrom, String commitHashTo);


	/**
	 * Deletes the paths by creating a commit based on the provided userdata! Returns the according commit hash
	 *
	 * @param pathsToDelete paths of files to be deleted
	 * @param userData user
	 * @param cached if true, the file is only removed from index, but still exists, otherwise it's gone
	 * @return commit hash
	 */
	// TODO: remove
	String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached);

	/**
	 * Use this method to add a given path to a commit and commit it. Does not push! The path will be relativized onto the repository.
	 * @param pathToPut path to put
	 * @param userData user
	 * @return commitHash of the performed commit, or null if the commit fails
	 */
	// TODO: remove
	String changePath(Path pathToPut, UserData userData);

	/**
	 * Execute a git rm --cached on the provided path
	 *
	 * @param path
	 * @return
	 */
	// TODO
	boolean untrackPath(String path);


	/**
	 *
	 * This essentially handles the git operation AFTER a file was moved, that is "from" does no longer exist and "to" is the new location of the file
	 * Returns the commit hash of the underlying commit
	 * @param from from path
	 * @param to to path
	 * @param user user
	 * @param email mail
	 * @param message message
	 * @return commit hash of the move commit
	 */
	String moveFile(Path from, Path to, String user, String email, String message);

	/**
	 * Deletes the path by creating a commit based on the provided userdata! Returns the according commit hash
	 * @param pathToDelete path of file to be deleted
	 * @param cached if true, the file is only removed from index, but still exists, otherwise it's gone
	 * @param userData user data
	 * @return commit hash
	 */
	String deletePath(String pathToDelete, UserData userData, boolean cached);


}
