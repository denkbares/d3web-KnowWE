package de.uniwue.d3web.gitConnector;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

/**
 * Allows to perform actions on the git repository.
 */
public interface GitConnector {

	Logger LOGGER = LoggerFactory.getLogger(GitConnector.class);

	String DEFAULT_BRANCH = "main";
	String NO_COMMENT = "<no comment>";

	GitConnectorCommit commit();

	GitConnectorStatus status();

	GitConnectorLog log();

	GitConnectorBranch branches();

	GitConnectorPull pull();

	GitConnectorPush push();

	GitConnectorRollback rollback();

	/***
	 * Removes the file from stage (reverse 'git add')
	 *
	 * @param file file to be unstaged
	 * @return true iff it was unstaged
	 */
	boolean unstage(@NotNull String file);

	/**
	 * Returns the status of a given file
	 *
	 * @param file file
	 * @return status of the file in the current repo
	 */
	// TODO: remove
	@Deprecated
	//GitConnectorStatus.FileStatus getStatus(@NotNull String file);

	/**
	 * For a specified path (relative to the repository), returns a list of all long commit hashes.
	 * The first entry of the returning list is the oldest version and the last entry is the latest version
	 * @param file file
	 * @return list of commit hashes
	 */
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
	 * A repo folder is clean when neither untracked nor uncommitted files exist.
	 *
	 * @return true iff clean
	 */
	boolean isClean();

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


	/**
	 * Deletes the paths by creating a commit based on the provided userdata! Returns the according commit hash
	 *
	 * @param pathsToDelete paths of files to be deleted
	 * @param userData user
	 * @param cached if true, the file is only removed from index, but still exists, otherwise it's gone
	 * @return commit hash
	 */
	String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached);

	/**
	 * Use this method to add a given path to a commit and commit it. Does not push! The path will be relativized onto the repository.
	 * @param pathToPut path to put
	 * @param userData user
	 * @return commitHash of the performed commit, or null if the commit fails
	 */
	String changePath(Path pathToPut, UserData userData);

	/**
	 * Performs a git add <path>
	 * @param path file to be added
	 */
	void addPath(String path);

	void addPaths(List<String> path);

	void rollbackPaths(Set<String> pathsToRollback);

	String cherryPick( List<String> commitHashesToCherryPick);

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
	String currentBranch() throws IllegalStateException;

	/**
	 * Return the current head of the current branch
	 * @return
	 */
	String currentHEAD();

	/**
	 * Return the current head of the specified branch
	 *
	 * @return
	 */
	String currentHEADOfBranch(String branchName);


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
	 List<String> listBranches(boolean includeRemoteBranches);

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


	boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch);

	/**
	 * Switches on the current branch to the specified tag (if existing)
	 *
	 * @param tagName tag to be switched
	 * @return
	 */
	boolean switchToTag(String tagName);

	/**
	 * Execute a git rm --cached on the provided path
	 *
	 * @param path
	 * @return
	 */
	boolean untrackPath(String path);

	boolean addNoteToCommit(String noteText, String commitHash, String namespace);

	boolean copyNotes(String commitHashFrom, String commitHashTo);

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

	/**
	 * Pushes all commit to origin.
	 *
	 * @return true if push was successful
	 */
	boolean pushAll(String userName, String passwordOrToken);

	/**
	 * Pushes the given branch to origin.
	 *
	 * @param branch the branch to be pushed
	 * @return true if push was successful
	 */
	boolean pushBranch(String branch, String userName, String passwordOrToken);



	default boolean pushAll() {
		return pushAll("", "");
	}


	default boolean pushBranch(String branch) {
		return pushBranch(branch, "", "");
	}

	/**
	 * Pulls with rebase mode if specified
	 *
	 * @param rebase rebase mode
	 * @return true if successful
	 */
	boolean pullCurrent(boolean rebase);

	/**
	 * Obtain the status of the current active branch
	 *
	 * @return
	 */
	// TODO: remove -> moved to GitConnectorStatus
	@Deprecated
	//GitStatusCommandResult getStatus();
	/**
	 * returns the name of the repository.
	 *
	 * @return repo name
	 */
	String repoName();

	void abortCherryPick();

	/**
	 * Sets the upstream branch for the current branch
	 *
	 * @param branch branch on origin that the current branch is connected to
	 * @return true if successful
	 */
	boolean setUpstreamBranch(String branch);

	GitMergeCommandResult mergeBranchToCurrentBranch(String branchName);

	PushCommandResult pushToOrigin(String userName, String passwordOrToken);

	ResetCommandResult resetToHEAD();

	/**
	 * Resets a modified (or deleted) file to the last committed state.
	 *
	 * @param file file to be reset
	 * @return true iff reset was successful
	 */
	boolean resetFile(String file);
}
