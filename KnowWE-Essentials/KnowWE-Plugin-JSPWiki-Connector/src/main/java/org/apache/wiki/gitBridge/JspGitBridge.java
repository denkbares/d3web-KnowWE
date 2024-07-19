package org.apache.wiki.gitBridge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.time.StopWatch;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.providers.GitProviderProperties;
import org.apache.wiki.providers.GitVersioningUtils;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.wiki.api.providers.AttachmentProvider.PROP_STORAGEDIR;
import static org.apache.wiki.providers.AbstractFileProvider.FILE_EXT;
import static org.apache.wiki.providers.GitVersioningFileProvider.LATEST_VERSION;

public class JspGitBridge {

	private static final Logger LOGGER = LoggerFactory.getLogger(JspGitBridge.class);
	private final Engine engine;
	private String storageDir;

	private String filesystemPath;
	private File pageDir;
	private File gitDir;

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

		this.filesystemPath = TextUtil.getCanonicalFilePathProperty(properties, GitProviderProperties.JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		this.pageDir = new File(this.filesystemPath);
		this.gitDir = new File(pageDir.getAbsolutePath() + File.separator + ".git");

		this.commitCount = new AtomicLong();
	}


	private String getFilesystemPath() {
		return filesystemPath;
	}

	private Repository initFromFilesystem(File gitDir, boolean alreadyExisting) throws IOException {
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

	private Repository initGitFromRemote(File gitDir, File pageDir, String remoteURL) throws IOException {
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


	private Repository initGitRepository(File gitDir, File pageDir, String remoteURL) throws IOException {
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

	public void initAndVerifyGitRepository(Properties properties) throws IOException {
		final String remoteURL = TextUtil.getStringProperty(properties, GitProviderProperties.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "");
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
			String defaultBranchName = properties.getProperty(GitProviderProperties.JSPWIKI_GIT_DEFAULT_BRANCH,null);
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

	private List<String> getCommitHashesForPageRaw(String pageName, boolean mangle, int maxCount) throws InterruptedException, IOException {
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
	private String attemptGetFilename(String pageName, boolean mangle) {
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



	public void periodicalGitGC() {
		final long l = this.commitCount.incrementAndGet();
		if (l % 2000 == 0) {
			gitGc(true, windowsGitHackNeeded && !dontUseHack, repository, true);
		}
		else if (l % 500 == 0) {
			gitGc(false, windowsGitHackNeeded && !dontUseHack, repository, false);
		}
		else if (l % 100 == 0) {
			this.repository.autoGC(new TextProgressMonitor());
		}
	}

	private void gitGc(boolean prune, boolean windowsGitHack, Repository repository, boolean aggressive){
		LOGGER.info("Start git gc");
		if (windowsGitHack) {
			doBinaryGC(repository.getDirectory(), prune);
		}
		else {
			doGC(repository, aggressive, prune);
		}
	}

	private void doBinaryGC(File pageDir, boolean prune) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			LOGGER.info("binary gc start");
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO().command("git", "gc", prune?"--prune=now":"").directory(pageDir);
			Process git_gc = pb.start();
			git_gc.waitFor(2, TimeUnit.MINUTES);
			sw.stop();
			LOGGER.info("binary gc took " + sw.toString());
		}
		catch (InterruptedException e) {
			LOGGER.warn("External git process didn't end in 2 minutes, therefore cancel it");
		}
		catch (IOException e) {
			LOGGER.error("Error executing external git: " + e.getMessage(), e);
		}
	}

	private void doGC(final Repository repository, final boolean aggressive, boolean prune) {
		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		final Git git = new Git(repository);
		try {
			LOGGER.info("Beginn Git gc");
			GarbageCollectCommand gc = git.gc()
					.setAggressive(aggressive);
			if(prune)
				gc.setExpire(null);
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
