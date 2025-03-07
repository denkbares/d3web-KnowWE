/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.jgit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.StopWatch;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;

import com.denkbares.utils.Stopwatch;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.UserData;
import de.uniwue.d3web.gitConnector.impl.GCDelegateParentFactory;
import de.uniwue.d3web.gitConnector.impl.GCFactory;
import de.uniwue.d3web.gitConnector.impl.GitConnectorParent;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;

public class JGitConnector extends GitConnectorParent {

	public static final String REFS_HEADS = "refs/heads/";
	private final Repository repository;
	private final Git git;
	private IgnoreNode ignoreNode;

	public JGitConnector(Repository repository) {
		this.repository = repository;
		this.ignoreNode = initIgnoreNode(repository.getDirectory().getParentFile());
		this.git = new Git(repository);
	}

	@Override
	protected @NotNull GCFactory getGcFactory() {
		return new JGCFactory(this);
	}

	private class JGCFactory extends GCDelegateParentFactory {
		public JGCFactory(GitConnector gitConnector) {
			super(gitConnector);
		}

		@Override
		public GitConnectorStatus createStatus() {
			return new JGitGCStatus(JGitConnector.this::getGit, JGitConnector.this::getRepository);
		}

		@Override
		public GitConnectorPull createPull() {
			return new JGitGCPull(JGitConnector.this::getGit, JGitConnector.this::getRepository);
		}

		@Override
		public GitConnectorPush createPush() {
			return new JGitGCPush(JGitConnector.this::getGit, JGitConnector.this::getRepository);
		}
	}

	private Repository getRepository() {
		return repository;
	}

	private Git getGit() {
		return git;
	}

	@Override
	public boolean unstage(@NotNull String file) {
		try {
			git.reset().addPath(file).call();
			return true;
		}
		catch (GitAPIException e) {
			LOGGER.error("Error unstaging file: {}", file, e);
			return false;
		}
	}

	@Override
	public boolean resetFile(String file) {
		try {
			git.checkout().addPath(file).call();
			return true;
		}
		catch (GitAPIException e) {
			LOGGER.error("Fehler beim Zurücksetzen der Datei: {}", file, e);
			return false;
		}
	}



	@Override
	public String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached) {
		try {
			Collection<String> deleteFiles = new HashSet<>(pathsToDelete);

			RmCommand rmCommand = git.rm();

			Status status = git.status().call();

			// filter for untracked files
			Set<String> untracked = status.getUntracked();
			deleteFiles.removeAll(untracked);

			for (String path : deleteFiles) {
				rmCommand.addFilepattern(path);
			}

			if (deleteFiles.isEmpty()) {
				return "<no untracked files to delete>";
			}

			if (cached) {
				rmCommand.setCached(true);
			}

			rmCommand.call();

			CommitCommand commit = git.commit()
					.setMessage("Deleted paths: " + String.join(", ", pathsToDelete))
					.setAuthor(userData.user, userData.email);

			return commit.call().getId().getName();
		}
		catch (GitAPIException e) {
			throw new RuntimeException("Failed to delete paths: " + pathsToDelete, e);
		}
	}

	@NotNull
	private IgnoreNode initIgnoreNode(File pageDir) {
		IgnoreNode ignoreNode = new IgnoreNode();
		File gitignoreFile = new File(pageDir + "/.gitignore");
		if (gitignoreFile.exists()) {
			try {
				ignoreNode.parse(new FileInputStream(gitignoreFile));
			}
			catch (IOException e) {
				LOGGER.error("Can not read the gitignore file!");
				throw new RuntimeException(e);
			}
		}
		else {
			LOGGER.warn("NO .gitignore FILE WAS FOUND!! This will lead to a blown-up git history polluted by thousands of ci-build-*.xml file versions, in case of that CI-Dashboards are used! Recommendation: Add .gitignore file ignoring CI-Dashboard builds");
		}
		return ignoreNode;
	}

	@Override
	public boolean executeCommitGraph() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public String cherryPick(List<String> commitHashesToCherryPick) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public String getGitDirectory() {
		return this.repository.getDirectory().getParentFile().getAbsolutePath();
	}

	@Override
	public String currentBranch() {
		try {
			return this.repository.getBranch();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String currentHEAD() {
		ObjectId head = null;
		try {
			head = repository.resolve("HEAD");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (head == null) {
			LOGGER.error("Unable to obtain the current HEAD");
			throw new IllegalStateException("HEAD is null!");
		}
		return head.getName();
	}

	@Override
	public String currentHEADOfBranch(String branchName) {
		try {
			ObjectId resolve = repository.resolve(REFS_HEADS + branchName);

			if (resolve != null) {
				return resolve.getName();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public List<String> commitsBetween(String commitHashFrom, String commitHashTo) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> commitsBetweenForFile(String headCache, String s, String path) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean isIgnored(String path) {
		Boolean b = ignoreNode.checkIgnored(path, false);
		return b != null && b;
	}

	@Override
	public String commitForUser(UserData userData) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public String commitForUser(UserData userData, long timeStamp) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean isRemoteRepository() {
		final Set<String> remoteNames = this.repository.getRemoteNames();
		return !remoteNames.isEmpty();
	}

	@Override
	public List<String> listBranches(boolean includeRemoteBranches) {
		ListBranchCommand listBranchCommand = git.branchList();
		try {
			List<Ref> refs = listBranchCommand.call();
			refs.stream().forEach(ref -> {
				if (!ref.getName().startsWith(REFS_HEADS)) {
					throw new IllegalStateException("Unexpected ref name: " + ref.getName());
				}
			});
			return refs.stream().map(ref -> ref.getName().substring(REFS_HEADS.length())).toList();
		}
		catch (GitAPIException e) {
			LOGGER.error("jgit API exception", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> listCommitsForBranch(String branchName) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean switchToBranch(String branch, boolean createBranch) {
		boolean existsAlready = this.listBranches(createBranch).contains(branch);
		try {
			if (existsAlready) {
				CheckoutCommand checkoutCommand = git.checkout()
						.setName(REFS_HEADS + branch);
				Ref ref = checkoutCommand.call();
				return ref.getName().equals(branch);
			}
			else {
				if (createBranch) {
					// Erstelle einen neuen Branch

					git.branchCreate().setName(branch).call();
					// Wechsle zu dem neuen Branch
					git.checkout().setName(branch).call();
					return true;
				}
				else {
					// does not exist and we may not create it
					return false;
				}
			}
		}
		catch (GitAPIException e) {
			LOGGER.error("jgit API exception (Maybe the repo is still empty -> some file required!", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean switchToTag(String tagName) {
		return switchToBranch(tagName, false);
	}

	@Override
	public String repoName() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean setUpstreamBranch(String branch) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean untrackPath(String path) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean addNoteToCommit(String noteText, String commitHash, String namespace) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean copyNotes(String commitHashFrom, String commitHashTo) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public Map<String, String> retrieveNotesForCommit(String commitHash) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public void abortCherryPick() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public GitMergeCommandResult mergeBranchToCurrentBranch(String branchName) {
		throw new NotImplementedException("TODO");
	}



	@Override
	public ResetCommandResult resetToHEAD() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> commitHashesForFile(@NotNull String file) {
		long time = System.currentTimeMillis();
		Iterable<RevCommit> commitIterable = null;
		try {
			commitIterable = git.log().add(git.getRepository().resolve(Constants.HEAD)).addPath(file).call();
		}
		catch (GitAPIException | IOException e) {
			throw new RuntimeException(e);
		}

		if (commitIterable == null) {
			LOGGER.error("Could not execute git log!");
			return List.of();
		}

		// Return the commits
		List<RevCommit> revCommits = reverseToList(commitIterable);

		List<String> list = revCommits.stream().map(AnyObjectId::getName).toList();

		LOGGER.info("Obtaining commits for " + file + " in " + (System.currentTimeMillis() - time) + " ms");
		return list;
	}

	@Override
	public List<String> commitHashesForFileInBranch(@NotNull String file, String branchName) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> commitHashesForFileSince(@NotNull String file, @NotNull Date date) {
		Iterable<RevCommit> commits;
		RevFilter filter = new RevFilter() {
			@Override
			public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
				return (1000L * cmit.getCommitTime()) >= date.getTime();
			}

			@Override
			public RevFilter clone() {
				return null;
			}
		};
		Git git = new Git(repository);
		try {
			commits = git
					.log()
					.add(git.getRepository().resolve(Constants.HEAD))
					.addPath(file)
					.setRevFilter(filter)
					.call();
			return reverseToList(commits).stream().map(AnyObjectId::getName).toList();
		}
		catch (IOException | GitAPIException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	@Override
	public boolean gitInstalledAndReady() {
		// should always be able to run
		return true;
	}

	@Override
	public void destroy() {
		org.eclipse.jgit.nls.NLS.clear();
	}

	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		//version is assumed to start at 1
		int versionIndex = version - 1;
		List<String> strings = commitHashesForFile(file);

		if (versionIndex >= strings.size()) {
			LOGGER.warn("Requested a version higher than available for file: " + file);
			return null;
		}
		//TODO i hate that this means latest...
		String commitHash = null;
		if (version == -1) {
			commitHash = strings.get(strings.size() - 1);
		}
		else {
			//TODO check order
			commitHash = strings.get(version - 1);
		}
		if (commitHash == null) {
			LOGGER.error("Can not obtain the according git hash");
		}

		return commitHash;
	}

	private List<RevCommit> reverseToList(Iterable<RevCommit> revCommits) {
		Stopwatch watch = new Stopwatch();
		LinkedList<RevCommit> ret = new LinkedList<>();
		for (RevCommit revCommit : revCommits) {
			ret.addFirst(revCommit);
		}
		LOGGER.info("Read and Reverse RevCommit Iterator took: " + watch.getDisplay());
		return ret;
	}

	//	@Override
	private RevCommit getCommitForHash(String commitHash) {
		try {
			ObjectId objectId = git.getRepository().resolve(commitHash);
			if (objectId != null) {
				try (RevWalk revWalk = new RevWalk(git.getRepository())) {
					return revWalk.parseCommit(objectId);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		return this.commitHashesForFile(filePath).size();
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		Iterable<RevCommit> commits;
		RevFilter filter = new RevFilter() {
			@Override
			public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
				return (1000L * cmit.getCommitTime()) >= timeStamp.getTime();
			}

			@Override
			public RevFilter clone() {
				return null;
			}
		};
		Git git = new Git(repository);
		try {
			commits = git
					.log()
					.add(git.getRepository().resolve(Constants.HEAD))
					.setRevFilter(filter)
					.call();
			return reverseToList(commits).stream().map(AnyObjectId::getName).toList();
		}
		catch (IOException | GitAPIException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}


	@Override
	public byte[] getBytesForPath(String path, int version) {
		String commitHash = commitHashForFileAndVersion(path, version);
		if (commitHash == null) {
			return null;
		}

		RevCommit commitForHash = getCommitForHash(commitHash);

		byte[] bytes = null;
		try {
			bytes = getBytesFromCommit(path, commitForHash);
		}
		catch (IOException e) {
			LOGGER.error("Error obtaining bytes from git commit", e);
			throw new RuntimeException(e);
		}

		return bytes;
	}

	private RevCommit getCommit(String commitHash) throws IOException {
		try (RevWalk revWalk = new RevWalk(repository)) {
			ObjectId commitId = ObjectId.fromString(commitHash);
			RevCommit commit = revWalk.parseCommit(commitId);
			return commit;
		}
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		//TODO exception handling in here is rly bad!
		RevCommit revCommit = null;
		try {
			revCommit = getCommit(commitHash);
		}
		catch (IOException e) {
			LOGGER.error("Could not create RevCommit for: " + commitHash);
			throw new RuntimeException(e);
		}

		if (revCommit == null) {
			LOGGER.error("Could not create RevCommit for: " + commitHash);
			return new byte[0];
		}

		final TreeWalk treeWalkDir = new TreeWalk(this.repository);
		try {
			treeWalkDir.reset(revCommit.getTree());
			treeWalkDir.setFilter(PathFilter.create(path));
			treeWalkDir.setRecursive(false);
			//here we have our file directly
			if (treeWalkDir.next()) {
				final ObjectId fileId = treeWalkDir.getObjectId(0);
				final ObjectLoader loader = this.repository.open(fileId);
				return loader.getBytes();
			}
			else {
				throw new ProviderException("Can't load Git object for " + path);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getFilesizeForCommit(String commitHash, String path) {
		return getBytesForCommit(commitHash, path).length;
	}

	@Override
	public boolean versionExists(String path, int version) {
		return commitHashForFileAndVersion(path, version) != null;
	}

	@Override
	public UserData userDataFor(String commitHash) {
		throw new NotImplementedException("TODO!");
	}

	@Override
	public long commitTimeFor(String commitHash) {
		throw new NotImplementedException("TODO");
	}

	@NotNull
	private byte[] getBytesFromCommit(String path, RevCommit revCommit) throws IOException, ProviderException {
		final TreeWalk treeWalkDir = new TreeWalk(this.repository);
		treeWalkDir.reset(revCommit.getTree());
		treeWalkDir.setFilter(PathFilter.create(path));
		treeWalkDir.setRecursive(false);
		//here we have our file directly
		if (treeWalkDir.next()) {
			final ObjectId fileId = treeWalkDir.getObjectId(0);
			final ObjectLoader loader = this.repository.open(fileId);
			return loader.getBytes();
		}
		else {
			throw new ProviderException("Can't load Git object for " + path);
		}
	}

	@Override
	public void performGC(final boolean aggressive, boolean prune) {
		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		final Git git = new Git(repository);
		try {
			LOGGER.info("Beginn Git gc");
			GarbageCollectCommand gc = git.gc()
					.setAggressive(aggressive);
			if (prune) {
				gc.setExpire(null);
			}
			final Properties gcRes = gc.call();
			for (final Map.Entry<Object, Object> entry : gcRes.entrySet()) {
				LOGGER.info("Git gc result: " + entry.getKey() + " " + entry.getValue());
			}
		}
		catch (final GitAPIException e) {
			LOGGER.warn("Git gc not successful: " + e.getMessage());
		}
		stopwatch.stop();
		LOGGER.info("gc took " + stopwatch);
	}

	@Override
	public String commitPathsForUser(String message, String author, String email, Set<String> paths) {
		final CommitCommand commitCommand = git.commit();

		//set necessary data into commit
		setUserData(author, email, commitCommand);
		commitCommand.setMessage(message);
		//set paths
		for (final String path : paths) {
			commitCommand.setOnly(path);
		}

		String commitHash = null;
		try {
			commitCommand.setAllowEmpty(true);
			commitHash = retryGitOperation(() -> commitCommand.call().getId().getName()
					, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return commitHash;
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {

		final ResetCommand reset = git.reset();
		final CleanCommand clean = git.clean();
		final CheckoutCommand checkout = git.checkout().setForced(true);
		clean.setPaths(pathsToRollback);
		for (final String path : pathsToRollback) {
			reset.addPath(path);
			checkout.addPath(path);
		}
		clean.setCleanDirectories(true);
		try {
			retryGitOperation(() -> {
				reset.call();
				return null;
			}, LockFailedException.class, "Retry reset repo, because of lock failed exception");

			retryGitOperation(() -> {
				clean.call();
				return null;
			}, LockFailedException.class, "Retry clean repo, because of lock failed exception");

			retryGitOperation(() -> {
				final Status status = git.status().call();
				return null;
			}, LockFailedException.class, "Retry status repo, because of lock failed exception");

			retryGitOperation(() -> {
				checkout.call();
				return null;
			}, LockFailedException.class, "Retry checkout repo, because of lock failed exception");
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public String moveFile(Path from, Path to, String user, String email, String message) {
		//TODO better exception handling
		try {
			retryGitOperation(() -> {
				git.add().addFilepattern(to.toFile().getName()).call();
				return null;
			}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			retryGitOperation(() -> {
				git.rm().setCached(true).addFilepattern(from.toFile().getName()).call();
				return null;
			}, LockFailedException.class, "Retry removing from repo, because of lock failed exception");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		final CommitCommand commitCommand = git.commit()
				.setOnly(to.toFile().getName())
				.setOnly(from.toFile().getName());
		setUserData(user, email, commitCommand);
		if (message.isEmpty()) {
			commitCommand.setMessage("renamed page " + from + " to " + to);
		}
		else {
			commitCommand.setMessage(message);
		}

		String commitHash = null;
		try {
			commitHash = retryGitOperation(() -> {
				RevCommit commit = commitCommand.call();
				return commit.getId().getName();
			}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return commitHash;
	}

	@Override
	public String deletePath(String path, UserData userData, boolean cached) {

		String commitHash = null;
		try {
			final Git git = new Git(this.repository);

			retryGitOperation(() -> {
				RmCommand rmCommand = git.rm().addFilepattern(path);
				if (cached) {
					rmCommand.setCached(true);
				}
				rmCommand.call();
				return null;
			}, LockFailedException.class, "Retry removing from repo, because of lock failed exception");

			final CommitCommand commitCommand = git.commit()
					.setOnly(path);

			String comment = userData.message;
			if (comment.isEmpty()) {
				commitCommand.setMessage("removed page");
			}
			else {
				commitCommand.setMessage(comment);
			}
			setUserData(userData.user, userData.email, commitCommand);

			commitHash = retryGitOperation(() -> {
				final RevCommit revCommit = commitCommand.call();
				return revCommit.getId().getName();
			}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Can't delete page " + path + ": " + e.getMessage());
		}
		return commitHash;
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {

		String path = pathToPut.toFile().getName();

		addPath(path);

		String comment = userData.message;
		if (comment.isEmpty()) {
			comment = "Added page";
		}

		final CommitCommand commit = git
				.commit()
				.setOnly(pathToPut.toFile().getName());
		commit.setMessage(comment);
		setUserData(userData.user, userData.email, commit);
		commit.setAllowEmpty(true);

		String commitHash = null;
		try {
			commitHash = retryGitOperation(() -> {
				final RevCommit revCommit = commit.call();
				return revCommit.getName().toString();
			}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("File " + pathToPut + " could not be committed to git");
		}
		return commitHash;
	}

	@Override
	public void addPath(String path) {

		try {
			retryGitOperation(() -> {
				git.add().addFilepattern(path).call();
				return null;
			}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
		}
		catch (Exception e) {
			LOGGER.error("Could not add file to git" + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addPaths(List<String> path) {
		throw new NotImplementedException("TODO");
	}

	private void setUserData(String author, String email, CommitCommand commitCommand) {
		String checkedAuthor = author;
		String checkedEmail = email;

		if (null == author || "".equals(author)) {
			checkedAuthor = "";
		}

		if (null == email || "".equals(email)) {
			checkedEmail = "";
		}
		commitCommand.setCommitter(checkedAuthor, checkedEmail);
	}

	//TODO these variables are dumb
	private final int RETRY = 2;
	private final int DELAY = 100;

	public <V> V retryGitOperation(Callable<V> callable, Class<? extends Throwable> t, String message) throws
			Exception {
		int counter = 0;

		JGitInternalException internalException = null;
		while (counter < RETRY) {
			try {
				return callable.call();
			}
			catch (JGitInternalException e) {
				internalException = e;
				if (t.isAssignableFrom(e.getClass()) ||
						(e.getCause() != null && t.isAssignableFrom(e.getCause().getClass()))) {
					counter++;
					LOGGER.warn(String.format("retry %s/%s, %s", counter, RETRY, message));

					try {
						Thread.sleep(DELAY);
					}
					catch (InterruptedException e1) {
						// ignore
					}
				}
				else {
					throw e;
				}
			}
		}
		throw internalException;
	}

	public static JGitConnector fromPath(String path) {

		//if the directory does not exist => create it
		if (!new File(path).exists()) {
			try {
				Files.createDirectories(Paths.get(path));
			}
			catch (IOException e) {
				LOGGER.error("Unable to create repository");
				throw new IllegalArgumentException("Unable to create repository ...");
			}
		}

		//check whether the repository is already existing!
		boolean alreadyExisting = false;
		if (RepositoryCache.FileKey.isGitRepository(new File(path, ".git"), FS.DETECTED)) {
			alreadyExisting = true;
		}

		Repository repository = initRepository(path, alreadyExisting);

		return new JGitConnector(repository);
	}

	private static @NotNull Repository initRepository(String path, boolean alreadyExisting) {
		Repository repository = null;
		if (alreadyExisting) {
			try {
				repository = new FileRepositoryBuilder()
						.setGitDir(new File(path, ".git"))
						.build();

				if (!repository.getObjectDatabase().exists()) {
					// Create the repository if it does not exist
					try (Git git = new Git(repository)) {
						git.init().call();
					}
					catch (GitAPIException e) {
						throw new RuntimeException(e);
					}
				}
			}
			catch (IOException e) {
				LOGGER.error("Unable to create repository");
				throw new IllegalArgumentException("Unable to create repository ...");
			}
		}
		else {
			repository = createNewRepository(path);
		}

		if (repository == null) {
			LOGGER.error("Unable to create repository");
			throw new IllegalArgumentException("Unable to create repository ...");
		}
		return repository;
	}

	private static @NotNull Repository createNewRepository(String path) {
		try {
			try (Git git = Git.init().setDirectory(new File(path)).call()) {
				return git.getRepository();
			}
		}
		catch (GitAPIException e) {
			LOGGER.error("Unable to create repository");
			throw new RuntimeException("Unable to create repository", e);
		}
	}
}
