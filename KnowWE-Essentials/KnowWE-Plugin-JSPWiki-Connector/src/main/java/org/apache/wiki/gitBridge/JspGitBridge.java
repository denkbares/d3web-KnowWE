package org.apache.wiki.gitBridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.providers.GitVersioningUtils;
import org.apache.wiki.structs.VersionCarryingRevCommit;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;

import static org.apache.wiki.api.providers.AttachmentProvider.PROP_STORAGEDIR;
import static org.apache.wiki.gitBridge.JSPUtils.getPath;
import static org.apache.wiki.providers.AbstractFileProvider.FILE_EXT;
import static org.apache.wiki.providers.GitVersioningFileProvider.*;
import static org.apache.wiki.providers.GitVersioningUtils.gitGc;
import static org.apache.wiki.providers.GitVersioningUtils.reverseToList;

public class JspGitBridge {

	private static final Logger LOGGER = LoggerFactory.getLogger(JspGitBridge.class);
	private final Engine engine;
	private String storageDir;

	private String filesystemPath;
	private File pageDir;
	private File gitDir;

	private IgnoreNode ignoreNode;
	private Repository repository;

	public boolean isRemoteRepo() {
		return remoteRepo;
	}

	private boolean remoteRepo;

	public boolean windowsGitHackNeeded = false;
	public boolean dontUseHack = false;

	private AtomicLong commitCount;

	public JspGitBridge(Engine engine) {
		this.engine = engine;
	}

	public void init(Properties properties) {

		this.storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		this.filesystemPath = TextUtil.getCanonicalFilePathProperty(properties, JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		this.pageDir = new File(this.filesystemPath);
		this.gitDir = new File(pageDir.getAbsolutePath() + File.separator + GIT_DIR);

		this.ignoreNode = initIgnoreNode(this.pageDir);
		this.commitCount = new AtomicLong();
	}


	public String getFilesystemPath() {
		return filesystemPath;
	}

	public Repository initFromFilesystem(File gitDir, boolean alreadyExisting) throws IOException {
		if (alreadyExisting) {
			return new FileRepositoryBuilder()
					.setGitDir(gitDir)
					.build();
		}
		else {
			Repository repository = FileRepositoryBuilder.create(gitDir);
			repository.create();

			return repository;
		}
	}

	public Repository initGitFromRemote(File gitDir, File pageDir, String remoteURL) throws IOException {
		try {
			final Git git = Git.cloneRepository()
					.setURI(remoteURL)
					.setDirectory(pageDir)
					.setGitDir(gitDir)
					.setBare(false)
					.call();
			return git.getRepository();
		}
		catch (final GitAPIException e) {
			throw new IOException(e);
		}
	}

	public void executeGitCommitGraphCommand() {
		StopWatch sw = new StopWatch();
		sw.start();
		String label = "git commit graph command ";
		try {
			LOGGER.info("Starting execution of " + label);
			String command = "git commit-graph write --reachable --changed-paths";
			Process process = Runtime.getRuntime().exec(
					command, null, new File(this.filesystemPath));

			InputStream responseStream = process.getInputStream();
			int exitVal = process.waitFor();
			sw.stop();
			LOGGER.info("Execution of '" + command + "' took: " + sw);
			List<String> response = IOUtils.readLines(responseStream);
			String responseString = Strings.concat("\n", response);
			if (exitVal == 0) {
				LOGGER.info(label + "executed successfully. " + responseString);
			}
			else {
				LOGGER.warn(label + " terminated with error code: " + exitVal + " and message: " + responseString);
			}
		}
		catch (IOException | InterruptedException e) {
			LOGGER.error(label + " could not be run: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Repository initGitRepository(File gitDir, File pageDir, String remoteURL) throws IOException {
		if (!RepositoryCache.FileKey.isGitRepository(gitDir, FS.DETECTED)) {
			if (!"".equals(remoteURL)) {
				return initGitFromRemote(gitDir, pageDir, remoteURL);
			}
			else {
				return initFromFilesystem(gitDir, false);
			}
		}
		else {
			return initFromFilesystem(gitDir, true);
		}
	}

	public void addUserInfo(Engine engine, String author, CommitCommand commit) {
		if (null != author && !"".equals(author)) {
			try {
				UserProfile userProfile = engine.getManager(UserManager.class)
						.getUserDatabase()
						.findByFullName(author);
				commit.setCommitter(userProfile.getFullname(), userProfile.getEmail());
			}
			catch (NoSuchPrincipalException e) {
				// is sometime necessary, e.g. CI-process is not a Wiki account
				commit.setCommitter(author, "");
			}
		}
		if (author == null) {
			//we try to obtain the author
		}
	}

	public Iterable<RevCommit> getRevCommits(final String pageName, final Git git) throws GitAPIException, IOException {
		return git.log().add(git.getRepository().resolve(Constants.HEAD)).addPath(pageName).call();
	}

	public VersionCarryingRevCommit getRevCommitWithVersion(Attachment att, Git git, int version) throws GitAPIException, IOException {
		String path = getPath(att);
		VersionCarryingRevCommit revCommit = getRevCommitWithVersion(path, git, version);

		if (revCommit == null) {
			revCommit = getRevCommitWithVersion(JSPUtils.unmangleName(path), git, version);
		}

		return revCommit;
	}

	public List<RevCommit> getRevCommitList(Attachment att, Git git) throws GitAPIException, IOException {
		String path = getPath(att);
		List<RevCommit> revCommitList = getRevCommitList(path, git, -1);

		if (revCommitList.isEmpty()) {
			revCommitList = getRevCommitList(JSPUtils.unmangleName(path), git, -1);
		}

		return revCommitList;
	}

	public VersionCarryingRevCommit getRevCommitWithVersion(String path, Git git, int version) throws GitAPIException, IOException {

		long time = System.currentTimeMillis();
		List<String> commitsHashes = new ArrayList<>();
		//try raw access firstLOGGER.info("Access commits for page: " + path);

		//we attempt raw first as this appears to be the faster version
		try {
			commitsHashes = getCommitHashesForPageRaw(path, false, -1);
		}
		catch (InterruptedException e) {
			LOGGER.error("Tried to access commits for page " + path + " raw, but didnt work, we default to JGit");
		}
		System.out.println("Time raw git: " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		if (!commitsHashes.isEmpty()) {
			Collections.reverse(commitsHashes);
			String commitHash = null;
			int commitVersion = -1;
			if (version == LATEST_VERSION) {
				commitHash = commitsHashes.get(commitsHashes.size() - 1);
				commitVersion = commitsHashes.size();
			}
			else {
				commitHash = commitsHashes.get(version - 1);
				commitVersion = version;
			}
			if (commitHash != null) {
				System.out.println("Reverse Revcommit raw git: " + (System.currentTimeMillis() - time));
				return new VersionCarryingRevCommit(GitVersioningUtils.getRevCommit(git, commitHash), commitVersion);
			}
		}

		List<RevCommit> slowRevCommits = getRevCommitsJGit(path, git, -1);
		if (version == LATEST_VERSION) {
			return new VersionCarryingRevCommit(slowRevCommits.get(slowRevCommits.size() - 1), slowRevCommits.size());
		}
		return new VersionCarryingRevCommit(slowRevCommits.get(version - 1), version);
	}

	public List<RevCommit> getRevCommitList(String path, Git git, int maxCount) throws GitAPIException, IOException {

		long time = System.currentTimeMillis();
		List<String> commitsHashes = new ArrayList<>();
		//try raw access first
		LOGGER.info("Access commits for page: " + path);

		//we attempt raw first as this appears to be the faster version
		try {
			commitsHashes = getCommitHashesForPageRaw(path, false, -1);
		}
		catch (InterruptedException e) {
			LOGGER.error("Tried to access commits for page " + path + " raw, but didnt work, we default to JGit");
		}
		System.out.println("Time raw git: " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		if (!commitsHashes.isEmpty()) {
			List<RevCommit> list = commitsHashes.stream()
					.map(hash -> GitVersioningUtils.getRevCommit(git, hash))
					.collect(Collectors.toList());
			Collections.reverse(list);
			System.out.println("Reverse Revcommit raw git: " + (System.currentTimeMillis() - time));
			return list;
		}

		return getRevCommitsJGit(path, git, maxCount);
	}

	@NotNull
	private static List<RevCommit> getRevCommitsJGit(String path, Git git, int maxCount) throws IOException, GitAPIException {
		LogCommand logCommand = git
				.log()
				.add(git.getRepository().resolve(Constants.HEAD))
				.addPath(path);

		if (maxCount != -1) {
			logCommand.setMaxCount(maxCount);
		}
		Iterable<RevCommit> revCommits = logCommand
				.call();
		return reverseToList(revCommits);
	}

	public int numberOfCommitsForFile(Git git, String filePath, String filesystemPath) {

		String command = "git rev-list --count HEAD -- " + filePath;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(
					command, null, new File(filesystemPath));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		InputStream responseStream = process.getInputStream();

		try {
			int exitVal = process.waitFor();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		String response = null;
		try {
			response = new String(responseStream.readNBytes(10));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return Integer.parseInt(response.trim());
	}

	public List<RevCommit> getCommitsForFile(Git git, String filePath) throws IOException {
		RevWalk revWalk = new RevWalk(git.getRepository());

		// Set the file path filter
		revWalk.setTreeFilter(PathFilter.create(filePath));

		// Sort the commits in reverse chronological order
		revWalk.sort(RevSort.REVERSE);

		// Start walking from HEAD
		ObjectId head = git.getRepository().resolve("HEAD");
		revWalk.markStart(revWalk.parseCommit(head));

		List<RevCommit> commits = new ArrayList<>();

//		for(RevCommit commit : revWalk){
//			commits.add(commit);
//		}

		RevCommit currentCommit = revWalk.next();
		while (currentCommit != null) {
			commits.add(currentCommit);
			currentCommit = revWalk.next();
		}

		// Return the commits
		return commits;
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

	public void addWithRetry(Git git, File attDir) {
		try {
			retryGitOperation(() -> {
				git.add().addFilepattern(attDir.getName()).call();
				return null;
			}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

//	public boolean shouldAddFile(){
////		Boolean ignored = ignoreNode.checkIgnored(this.gitBridge.getPath(att), newFile.isDirectory());
////
////		boolean add = !newFile.exists() && (ignored == null || !ignored);
//	}

	@NotNull
	public List<Page> getAllChangedSinceSafe(Date date, Repository repository, Engine engine) {
		final Iterable<RevCommit> commits = GitVersioningUtils.getRevCommitsSince(date, repository);
		final List<Page> pages = new ArrayList<>();

		try {
			ObjectId oldCommit = null;
			ObjectId newCommit;
			LOGGER.info("Access commits for repository ");
			for (final RevCommit commit : GitVersioningUtils.reverseToList(commits)) {
				final String fullMessage = commit.getFullMessage();
				final String author = commit.getCommitterIdent().getName();
				final Date modified = new Date(1000L * commit.getCommitTime());

				if (oldCommit != null) {
					newCommit = commit.getTree();
					final List<DiffEntry> diffs = GitVersioningUtils.getDiffEntries(oldCommit, newCommit, repository);
					for (final DiffEntry diff : diffs) {
						String path = diff.getOldPath();
						if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
							path = diff.getNewPath();
						}
						if (path.endsWith(FILE_EXT) && !path.contains("/")) {
							final WikiPage page = getWikiPage(fullMessage, author, modified, path, engine);
							pages.add(page);
						}
					}
				}
				else {
					try (final TreeWalk treeWalk = new TreeWalk(repository)) {
						treeWalk.reset(commit.getTree());
						treeWalk.setRecursive(true);
						treeWalk.setFilter(TreeFilter.ANY_DIFF);
						while (treeWalk.next()) {
							final String path = treeWalk.getPathString();
							if (path.endsWith(FILE_EXT) && !path.contains("/")) {
								final WikiPage page = getWikiPage(fullMessage, author, modified, path, engine);
								pages.add(page);
							}
						}
					}
				}
				oldCommit = commit.getTree();
			}
		}
		catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return pages;
	}

	@NotNull
	private WikiPage getWikiPage(final String fullMessage, final String author, final Date modified, final String path, Engine engine) {
		final int cutpoint = path.lastIndexOf(FILE_EXT);
		if (cutpoint == -1) {
			LOGGER.error("wrong page name " + path);
		}
		final WikiPage page = new WikiPage(engine, JSPUtils.unmangleName(path.substring(0, cutpoint)));
		page.setAttribute(WikiPage.CHANGENOTE, fullMessage);
		page.setAuthor(author);
		page.setLastModified(modified);
		return page;
	}

	public RevCommit getLatestCommitForFile(Git git, String filePath) throws IOException {
		RevWalk revWalk = new RevWalk(git.getRepository());

		// Set the file path filter
		revWalk.setTreeFilter(PathFilter.create(filePath));

		// Sort the commits in reverse chronological order
		revWalk.sort(RevSort.REVERSE);

		// Start walking from HEAD
		ObjectId head = git.getRepository().resolve("HEAD");
		revWalk.markStart(revWalk.parseCommit(head));

		RevCommit next = revWalk.next();

		revWalk.close();
		return next;
	}

	public void initAndVerifyGitRepository(Properties properties) throws IOException {
		final String remoteURL = TextUtil.getStringProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "");
		this.repository = initGitRepository(gitDir, pageDir, remoteURL);
		//verify that we have the required branch for this project
		verifyGitRepositoryInitialStatus(properties);

		final Set<String> remoteNames = this.repository.getRemoteNames();
		if (!remoteNames.isEmpty()) {
			this.remoteRepo = true;
		}
	}

	private void verifyGitRepositoryInitialStatus(Properties properties) {
		//check whether maintencance branch is already existing
		Git git = new Git(this.repository);
		try {
			String defaultBranchName = properties.getProperty(JSPWIKI_GIT_DEFAULT_BRANCH,null);
			//do nothing if it is not set
			if(defaultBranchName==null){
				return;
			}
			List<Ref> branchRefs = git.branchList().call();
			List<Ref> refs = branchRefs.stream()
					.filter(ref -> ref.getName().equals("refs/heads/" + defaultBranchName))
					.toList();
			if (refs.isEmpty()) {
				git.add().addFilepattern(".").call();
				git.commit().setMessage("init repository").call();
				git.branchRename().setNewName(defaultBranchName).call();
			}
		}
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getCommitHashesForPageRaw(String pageName, boolean mangle, int maxCount) throws InterruptedException, IOException {
		long time = System.currentTimeMillis();
		String filename = attemptGetFilename(pageName, mangle);

		File outputFile = File.createTempFile("git-log-output", ".txt");
		String[] command = null;
		if (maxCount == -1) {
			command = new String[] { "git", "log", "--format=%H", filename };
		}
		else {
			command = new String[] { "git", "log", "--format=%H", "--max-count=" + maxCount, "--", filename, };
		}

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File(this.getFilesystemPath()));
		processBuilder.redirectOutput(outputFile);

		// Set the working directory if needed
		// processBuilder.directory(new File("your_git_repository_directory"));

		// Start the process
		Process process = processBuilder.start();
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			LOGGER.info("Successfully execute: " + processBuilder.command() + " in " + (System.currentTimeMillis() - time) + "ms");
//			System.out.println("Command executed successfully");
		}
		else {
			LOGGER.error("Failed to execute command with exit code: " + (exitCode) + " for command: " + processBuilder);
		}

		List<String> commitHashes = new ArrayList<>();

		List<String> response = Files.readAllLines(outputFile.toPath());

		for (String line : response) {
			String commitHash = line.replaceAll("\"", "").trim();
			commitHashes.add(commitHash);
		}

		outputFile.delete();

		return commitHashes;
	}

	//TODO i hate this method this is just an ugly hack
	public String attemptGetFilename(String pageName, boolean mangle) {
		//check if the file does exist already
		if (Paths.get(getFilesystemPath(), pageName).toFile().exists()) {
			return pageName;
		}

		if (Paths.get(getFilesystemPath(), pageName + ".txt").toFile().exists()) {
			return pageName + ".txt";
		}

		//try mangling
		String filename = null;
		if (mangle) {
			filename = JSPUtils.mangleName(pageName) + ".txt";
		}

		if (filename != null && Paths.get(getFilesystemPath(), filename).toFile().exists()) {
			return filename;
		}

		//try unmangling
		if (pageName.contains("%")) {
			filename = JSPUtils.unmangleName(pageName);
		}

		if (filename != null && Paths.get(getFilesystemPath(), filename).toFile().exists()) {
			return filename;
		}

		//attempt unmangled folder and mangle file
		if (pageName.split("/").length > 1) {
			filename = JSPUtils.unmangleName(pageName.split("/")[0]) + "/" + pageName.split("/")[1];
		}

		if (filename != null && Paths.get(getFilesystemPath(), filename).toFile().exists()) {
			return filename;
		}

		//same but mangle first
		if (pageName.split("/").length > 1) {
			filename = JSPUtils.mangleName(pageName.split("/")[0]) + "/" + pageName.split("/")[1];
		}

		if (filename != null && Paths.get(getFilesystemPath(), filename).toFile().exists()) {
			return filename;
		}

		if (pageName.split(File.separator).length > 1) {
			filename = JSPUtils.mangleName(pageName.split(File.separator)[0]) + "-att" + File.separator + pageName.split("/")[1];
		}

		if (filename != null && Paths.get(getFilesystemPath(), filename).toFile().exists()) {
			return filename;
		}

//TODO totally clueless
		return null;
	}

	public Repository getRepository() {
		return this.repository;
	}

	public boolean isClean() {
		try {
			Status call = new Git(this.repository).status().call();
			return call.isClean();
		}
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public void assignAuthor(Attachment att) {
		int a = 2;
	}

	public void assignAuthor(Page page) {

		try {
			List<String> commitHashesForPageRaw = this.getCommitHashesForPageRaw(page.getName(), false, -1);
			if (!commitHashesForPageRaw.isEmpty()) {
				int version = page.getVersion();
				int commitOffset;
				if (version == LATEST_VERSION) {
					commitOffset = 0;
				}
				else {
					commitOffset = commitHashesForPageRaw.size() - version;
				}

				if (commitOffset < 0 || commitOffset >= commitHashesForPageRaw.size()) {
					LOGGER.error("Tried to access a version that does not exist:" + version + " for page: " + page.getName() + " of which there are: " + commitHashesForPageRaw.size() + " versions");
					return;
				}
				RevCommit revCommit = GitVersioningUtils.getRevCommit(new Git(this.repository), commitHashesForPageRaw.get(commitOffset));
				if (revCommit != null) {
					page.setAuthor(revCommit.getAuthorIdent().getName());
				}
			}
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * In addition to detached commit clean up, git gc will also perform compression on stored Git Objects, freeing up
	 * precious disk space. When Git identifies a group of similar objects it will compress them into a 'pack'. Packs
	 * are like zip files of Git bjects and live in the ./git/objects/pack directory within a repository.
	 *
	 * @param properties of the wiki
	 * @see <a href="https://www.atlassian.com/git/tutorials/git-gc">...</a>
	 */
	public void applyGarbageCollection(Properties properties) {
		boolean skipGC = TextUtil.getBooleanProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_SKIP_GC, false);

		if (!skipGC) {
			gitGc(true, windowsGitHackNeeded && !dontUseHack, repository, true);
			this.repository.autoGC(new TextProgressMonitor());
		}
	}

	private static final int RETRY = 2;
	private static final int DELAY = 100;

	public static <V> V retryGitOperation(Callable<V> callable, Class<? extends Throwable> t, String message) throws Exception {
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

//	public WikiEngineGitBridge(Engine engine, Repository repository) {
//		this.engine = engine;
//		this.repository = repository;
//		verifyForWindowsHack();
//		this.commitCount = new AtomicLong();
//	}

	public void periodicalGitGC() {
		final long l = this.commitCount.incrementAndGet();
		if (l % 2000 == 0) {
			GitVersioningUtils.gitGc(true, windowsGitHackNeeded && !dontUseHack, repository, true);
		}
		else if (l % 500 == 0) {
			GitVersioningUtils.gitGc(false, windowsGitHackNeeded && !dontUseHack, repository, false);
		}
		else if (l % 100 == 0) {
			this.repository.autoGC(new TextProgressMonitor());
		}
	}

	private void verifyForWindowsHack() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.startsWith("windows") || os.equals("nt")) {
			windowsGitHackNeeded = true;
		}
		if (windowsGitHackNeeded) {
			try {
				Runtime.getRuntime().exec("git");
			}
			catch (IOException e) {
				if (e.getMessage().toLowerCase().contains("command not found")) {
					dontUseHack = true;
					LOGGER.warn("Can't find git in PATH");
				}
				else {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
