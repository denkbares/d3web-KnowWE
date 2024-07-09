package de.uniwue.d3web.gitConnector;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface GitConnector {
	Logger LOGGER = LoggerFactory.getLogger(GitConnector.class);

	/**
	 * For a specified path (relative to the repository), returns a list of all long commithashes. The first entry of the returning list is the oldest version and the last entry is the latest version
	 * @param file
	 * @return
	 */
	List<String> commitHashesForFile(String file);
	List<String> commitHashesForFileSince(String file, Date date);

	/**
	 * Returns the specific hash of a given file in a specific version. Keep in mind that the first version is 1 instead of 0!
	 * @param file
	 * @param version
	 * @return the commit-hash for a file and a given version, null if it cannot be found.
	 */
	String commitHashForFileAndVersion(String file, int version);

	/**
	 * Returns the number of commits that were made onto a specific file, or 0 it none were made!
	 * @param filePath
	 * @return
	 */
	int numberOfCommitsForFile(String filePath);

	/**
	 *  Returns all commithashes that have an assigned date later than the specified date
	 * @param timeStamp
	 * @return
	 */
	List<String> getCommitsSince(Date timeStamp);

	boolean isClean();

	/**
	 * Get bytes for a file (specified by its relative path, starting from git root) in a specific version
	 * @param path
	 * @param version
	 * @return a bytearray of the content or null if the version could not be found!
	 */
	byte[] getBytesForPath(String path, int version);

	/**
	 * Gets the bytes for a given commit of a certain path
	 * @param commitHash
	 * @return
	 */
	byte[] getBytesForCommit(String commitHash,String path);

	/**
	 * Checks whether there is a file (provided by the path) in the given version
	 * @param path
	 * @param version
	 * @return
	 */
	boolean versionExists(String path, int version);

	/**
	 * Obtain the userdata for a given commithash
	 * @param commitHash
	 * @return
	 */
	UserData userDataFor(String commitHash);

	long commitTimeFor(String commitHash);



	void commitPathsForUser(String message, String author, String email, Set<String> paths);

	/**
	 * This essentially handles the git operation AFTER a file was moved, that is "from" does no longer exist and "to" is the new location of the file

	 */
	void moveFile(Path from, Path to, String user, String email, String message);

	void deletePath(Path pathToDelete, UserData userData);

	/**
	 * Use this method to add a given path to a commit and commit it. Does not push! The path will be relativized onto the repository.
	 * @param pathToPut
	 * @param userData
	 * @return commitHash of the performed commit, or null if the commit fails
	 */
	String changePath(Path pathToPut, UserData userData);

	/**
	 * Performs a git add <path>
	 * @param path
	 * @return
	 */
	void addPath(String path);

	void rollbackPaths(Set<String> pathsToRollback);

	/**
	 * Performs git garbage collection, which is thought to speed up subsequent git calls. Use with caution as this is slow!
	 * @param aggressive
	 * @param prune
	 */
	void performGC(boolean aggressive, boolean prune);

	/**
	 * Similar to GC() this is meant to speed up subsequent git calls. Use only if you know what it does
	 * @return
	 */
	boolean executeCommitGraph();

	void cherryPick(String branch, List<String> commitHashesToCherryPick);

	List<String> listChangedFilesForHash(String commitHash);


	default String getTextForPath(String pathToTxt, int version) {
		if (!pathToTxt.endsWith(".txt")) {
			LOGGER.warn("Request text for a file that isnt a txt");
		}

		return new String(getBytesForPath(pathToTxt, version));
	}
}
