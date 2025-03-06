/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

public interface GitConnectorLog {

	static final Logger LOGGER = LoggerFactory.getLogger(GitConnectorLog.class);

	/**
	 * For a specified path (relative to the repository), returns a list of all long commit hashes.
	 * The first entry of the returning list is the oldest version and the last entry is the latest version
	 * @param file file
	 * @return list of commit hashes
	 */
	// TODO: remove
	List<String> commitHashesForFile(@NotNull String file);

	/**
	 * Same as above, but you can also specify the branch where the command gets triggered in
	 * @param file file
	 * @param branchName branch name
	 * @return commit hashes
	 */
	List<String> commitHashesForFileInBranch(@NotNull String file, String branchName);

	/**
	 * For a specified path (relative to the repository), returns a list of all long commit hashes since a certain specified point in time.
	 * The first entry of the returning list is the oldest version and the last entry is the latest version
	 * @param file file
	 * @param date since date
	 * @return list of all commit hashes of the file since date
	 */
	List<String> commitHashesForFileSince(@NotNull String file, @NotNull Date date);

	/**
	 * Returns the specific hash of a given file in a specific version. Keep in mind that the first version is 1 instead of 0!
	 * @param file file
	 * @param version version
	 * @return the commit-hash for a file and a given version, null if it cannot be found.
	 */
	String commitHashForFileAndVersion(String file, int version);

	/**
	 * Returns the number of commits that were made onto a specific file, or 0 it none were made!
	 * @param filePath file path
	 * @return number of commits for the file, 0 if none exist
	 */
	int numberOfCommitsForFile(String filePath);

	/**
	 * Gets the bytes for a given commit of a certain path
	 * @param commitHash commitHash
	 * @return bytes
	 */
	byte[] getBytesForCommit(String commitHash,String path);

	/**
	 * Obtains the size of the file in the provided hash in bytes or -1 if the file was not found in the respective commit
	 * @param commitHash commitHash
	 * @param path path
	 * @return file size in bytes
	 */
	long getFilesizeForCommit(String commitHash, String path);


	/**
	 *  Returns all commit-hashes that have an assigned date later than the specified date
	 * @param timeStamp time stamp
	 * @return all commit hashed that have assigned date later than time stamp
	 */
	List<String> getCommitsSince(Date timeStamp);



	/**
	 * Get bytes for a file (specified by its relative path, starting from git root) in a specific version
	 * @param path path
	 * @param version version
	 * @return a bytearray of the content or null if the version could not be found!
	 */
	byte[] getBytesForPath(String path, int version);


	/**
	 * Checks whether there is a file (provided by the path) in the given version
	 * @param path path
	 * @param version version
	 * @return true iff version exists
	 */
	boolean versionExists(String path, int version);

	/**
	 * Obtain the userdata for a given commit-hash
	 * @param commitHash commitHash
	 * @return user data
	 */
	UserData userDataFor(String commitHash);

	/**
	 * In Seconds since 1970
	 * @param commitHash commitHash
	 * @return commit timestamp in seconds since 1970
	 */
	long commitTimeFor(String commitHash);


	/**
	 * For a provided commithash, this lists all files that are touched in this commit
	 * @param commitHash
	 * @return
	 */
	List<String> listChangedFilesForHash(String commitHash);


	default String getTextForPath(String pathToTxt, int version) {
		if (!pathToTxt.endsWith(".txt")) {
			LOGGER.warn("Request text for a file that isnt a txt");
		}

		return new String(getBytesForPath(pathToTxt, version));
	}

	/**
	 * Return the current head of the current branch
	 * @return
	 */
	String currentHEAD();


	/**
	 * Obtain all commithashes between the provided commitHashFrom and commitHashTo. The list is sorted in the way git stores these commits.
	 * The returned list does not contain the from hash, but it does contain commitHashTo (its the last element so drop that one if you do not need it)
	 * @param commitHashFrom
	 * @param commitHashTo
	 * @return
	 */
	List<String> commitsBetween(String commitHashFrom, String commitHashTo);


	/**
	 * Same as commitsBetween, but the returnes commitHashes are only those that changed the file specified by "path"
	 * @param commitHashFrom
	 * @param commitHashTo
	 * @param path
	 * @return
	 */
	List<String> commitsBetweenForFile(String commitHashFrom, String commitHashTo, String path);


	/**
	 * Lists all commit hashes for a given branch
	 * @return
	 */
	List<String> listCommitsForBranch(String branchName);


	Map<String, String> retrieveNotesForCommit(String commitHash);

	/**
	 * Retrieves
	 *
	 * @param commitHash
	 * @param namespace
	 * @return
	 */
	default String retrieveNoteForCommitInNamespace(String commitHash, String namespace) {
		return retrieveNotesForCommit(commitHash).get(namespace);
	}
}

