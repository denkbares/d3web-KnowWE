package de.uniwue.d3web.gitConnector;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to perform actions on the git repository.
 */
public interface GitConnector {
	Logger LOGGER = LoggerFactory.getLogger(GitConnector.class);

	String DEFAULT_BRANCH = "main";
	String NO_COMMENT = "<no comment>";

	/**
	 * For a specified path (relative to the repository), returns a list of all long commithashes. The first entry of the returning list is the oldest version and the last entry is the latest version
	 * @param file
	 * @return
	 */
	List<String> commitHashesForFile(String file);
	List<String> commitHashesForFileSince(String file, Date date);

	/**
	 * Checks if git is ready to go in the current runtime environment
	 *
	 * @return true if git is ready to go
	 */
	boolean gitInstalledAndReady();

	/**
	 * Is called when the application is shut down. It shall clean up and destroy the git connection and caches.
	 */
	void destroy();

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
	 *  Returns all commit-hashes that have an assigned date later than the specified date
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
	 * Obtains the size of the file in the provided hash in bytes or -1 if the file was not found in the respective commit
	 * @param commitHash
	 * @param path
	 * @return
	 */
	long getFilesizeForCommit(String commitHash,String path);

	/**
	 * Checks whether there is a file (provided by the path) in the given version
	 * @param path
	 * @param version
	 * @return
	 */
	boolean versionExists(String path, int version);

	/**
	 * Obtain the userdata for a given commit-hash
	 * @param commitHash
	 * @return
	 */
	UserData userDataFor(String commitHash);

	/**
	 * In Seconds since 1970
	 * @param commitHash
	 * @return
	 */
	long commitTimeFor(String commitHash);



	String commitPathsForUser(String message, String author, String email, Set<String> paths);

	/**
	 * This essentially handles the git operation AFTER a file was moved, that is "from" does no longer exist and "to" is the new location of the file
	 * Returns the commithash of the underlying commit
	 */
	String moveFile(Path from, Path to, String user, String email, String message);

	/**
	 * Deletes the path by creating a commit based on the provided userdata! Returns the according commithash
	 * @param pathToDelete
	 * @param userData
	 */
	String deletePath(String pathToDelete, UserData userData, boolean cached);

	String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached);

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

	void addPaths(List<String> path);

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
	 * Obtain the directory in which the .git folder is located
	 * @return
	 */
	String getGitDirectory();

	/**
	 * Returnt he current active branch. Throws Illegalstate if this command fails
	 * @return
	 */
	String currentBranch();

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
	 * Checks whether a given path is ignored!
	 * @param path
	 * @return
	 */
	boolean isIgnored(String path);

	/**
	 * Performs a commit operation on the underlying git using the provided userData - returns the commit hash.
	 *
	 * @param userData
	 * @return
	 */
	String commitForUser(UserData userData);

	/**
	 * Performs a commit operation on the underlying git using the provided userData - returns the commit hash. You can specify a timestamp.
	 * The timestamp has to be specified as a Git timestamp which corresponds to "System.currentTimeMillis()/1000".
	 * @param userData
	 * @return
	 */
	String commitForUser(UserData userData, long timeStamp);
	/**
	 * Returns whether this repository has any remote origins assigned!
	 * @return
	 */
	boolean isRemoteRepository();

	/**
	 * List all branches of this repository
	 */
	 List<String> listBranches();

	/**
	 * Lists all commit hashes for a given branch
	 * @return
	 */
	List<String> listCommitsForBranch(String branchName);

	/**
	 * switches to the specified branch and creates the branch if necessary. Returns true if successful
	 * @param branch
	 * @param createBranch
	 * @return
	 */
	boolean switchToBranch(String branch, boolean createBranch);

	/**
	 * Switches on the current branch to the specified tag (if existing)
	 *
	 * @param tagName tag to be switched
	 * @return
	 */
	boolean switchToTag(String tagName);

	/**
	 * Pushes all commit to origin.
	 *
	 * @return true if push was successful
	 */
	boolean pushAll();

	/**
	 * Pushes the given branch to origin.
	 *
	 * @param branch the branch to be pushed
	 * @return true if push was successful
	 */
	boolean pushBranch(String branch);

	/**
	 * Pulls with rebase mode if specified
	 *
	 * @param rebase rebase mode
	 * @return true if successful
	 */
	boolean pullCurrent(boolean rebase);

	/**
	 * returns the name of the repository.
	 *
	 * @return repo name
	 */
	String repoName();

	/**
	 * Sets the upstream branch for the current branch
	 *
	 * @param branch branch on origin that the current branch is connected to
	 * @return true if successful
	 */
	boolean setUpstreamBranch(String branch);
}
