package org.apache.wiki.providers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.FullParseEvent;

import static de.knowwe.core.utils.KnowWEUtils.getDefaultArticleManager;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 21.08.20
 */
public class GitAutoUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(GitAutoUpdater.class);

	private final GitVersioningFileProvider fileProvider;
	private final Engine engine;
	private final Repository repository;
	private static boolean running;

	public GitAutoUpdater(Engine engine, GitVersioningFileProvider fileProvider) {
		this.fileProvider = fileProvider;
		repository = fileProvider.repository;
		this.engine = engine;
	}

	public static boolean running() {
		return running;
	}

	public void update() {
		running = true;
		if (Files.exists(Paths.get(fileProvider.getFilesystemPath(), "lock.wc"))) {
			LOGGER.info("Not updating, because of filesystem lock");
			return;
		}
		Git git = new Git(fileProvider.repository);
		ArticleManager articleManager = getDefaultArticleManager();
		try {
			Compilers.awaitTermination(Compilers.getCompilerManager(Environment.DEFAULT_WEB));

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			articleManager.open();
			fileProvider.pushLock();

			RevWalk revWalk = new RevWalk(fileProvider.repository);
			ObjectId oldHead = fileProvider.repository.resolve("remotes/origin/master");
			RevCommit oldHeadCommit = revWalk.parseCommit(oldHead);
			boolean pullAfterReset = false;
			try {
				Status status = git.status().call();
				if (!status.isClean()) {
					LOGGER.warn("Git is not clean, doing reset first");
					LOGGER.warn("Added              : " + String.join(",", status.getAdded()));
					LOGGER.warn("Uncommitted changes: " + String.join(",", status.getUncommittedChanges()));
					LOGGER.warn("Missing            : " + String.join(",", status.getMissing()));
					LOGGER.warn("Modified           : " + String.join(",", status.getModified()));
					LOGGER.warn("Changed            : " + String.join(",", status.getChanged()));
					LOGGER.warn("Conflicting        : " + String.join(",", status.getConflicting()));
					try {
						switch (repository.getRepositoryState()) {
							case REBASING_INTERACTIVE, REBASING, REBASING_REBASING, REBASING_MERGE -> {
								git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
								pullAfterReset = true;
							}
						}
					}
					catch (GitAPIException e) {
						LOGGER.error("Reset wasn't successful", e);
						return;
					}
					try {
						git.reset().setMode(ResetCommand.ResetType.HARD).call();
					}
					catch (GitAPIException e) {
						LOGGER.error("Reset wasn't successful", e);
						return;
					}
				}
			}
			catch (GitAPIException e) {
				LOGGER.error("Status query wasn't successful, quiting update", e);
				return;
			}

			FetchCommand fetch1 = git.fetch();
			FetchResult fetch = fetch1.call();
			Collection<TrackingRefUpdate> trackingRefUpdates = fetch.getTrackingRefUpdates();

			if (pullAfterReset || !trackingRefUpdates.isEmpty()) {
				PullCommand pull = git.pull()
						.setRemote("origin")
						.setRemoteBranchName("master")
						.setStrategy(MergeStrategy.RESOLVE)
						.setRebase(true);
				PullResult pullResult = null;
				try {
					pullResult = pull.call();
				}
				catch (JGitInternalException ie) {
					LOGGER.error("internal jgit error", ie);
					try {
						switch (repository.getRepositoryState()) {
							case REBASING_INTERACTIVE, REBASING, REBASING_REBASING, REBASING_MERGE -> git.rebase()
									.setOperation(RebaseCommand.Operation.ABORT)
									.call();
						}
						pullResult = pull.setContentMergeStrategy(ContentMergeStrategy.OURS).call();
					}
					catch (JGitInternalException ie2) {
						LOGGER.error("internal jgit error", ie);
					}
				}
				if (pullResult != null) {
					RebaseResult rebaseResult = pullResult.getRebaseResult();
					boolean successful = rebaseResult.getStatus().isSuccessful();
					if (!successful) {
						LOGGER.error("unsuccessful pull " + rebaseResult.getStatus());
						if (rebaseResult.getConflicts() != null) {
							LOGGER.error("unsuccessful pull " + String.join(",", rebaseResult.getConflicts()));
						}
						else {
							LOGGER.error("unsuccessful pull " + rebaseResult.getFailingPaths());
						}
					}
				}
				GitVersioningUtils.gitGc(true, fileProvider.needsWindowsHack(), repository, false);
				fileProvider.getCache().initializeCache();
				fileProvider.pushUnlock();
				ObjectId newHead = fileProvider.repository.resolve(Constants.HEAD);
				String title = null;
				if (!oldHeadCommit.equals(newHead)) {
					LOGGER.info("Read changes after rebase");
					try {
						fileProvider.canWriteFileLock();
						RevCommit newHeadCommit = revWalk.parseCommit(newHead);

						Iterable<RevCommit> call = git.log().addRange(oldHeadCommit, newHeadCommit).call();

						final ObjectReader objectReader = this.repository.newObjectReader();
						final CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
						final CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
						final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
						diffFormatter.setRepository(this.repository);
						revWalk.reset();
						revWalk.sort(RevSort.REVERSE);
						revWalk.markStart(newHeadCommit);
						revWalk.parseCommit(oldHead);
						Iterator<RevCommit> iterator = revWalk.iterator();
						boolean parse = false;
						Set<String> refreshedPages = new TreeSet<>();
						//					resetCiBuildCache();
						while (iterator.hasNext()) {
							RevCommit commit = iterator.next();
							if (commit.equals(oldHeadCommit)) {
								parse = true;
								continue;
							}
							if (parse) {
								mapRevCommit(objectReader, oldTreeParser, newTreeParser, diffFormatter, commit, refreshedPages);
							}
						}
						LOGGER.info("Beginn compile");

						TreeSet<String> debugSet = new TreeSet<>(refreshedPages);
						for (String refreshedPage : debugSet) {
							LOGGER.info("Refreshed Page: " + refreshedPage);
						}

//							articleManager.open();
						if (!refreshedPages.isEmpty()) {
							WikiEventManager.fireEvent(fileProvider, new GitRefreshCacheEvent(fileProvider, GitRefreshCacheEvent.UPDATE, refreshedPages));
						}

						title = refreshedPages.stream()
								.filter(p -> p.contains("GVA_Gesamt") && p.contains("vm_gva_objekte"))
								.findFirst()
								.orElse(null);
					}
					finally {
						fileProvider.writeFileUnlock();
					}
				}
				if (title != null) {
					LOGGER.info("do full parse");
					Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
					EventManager.getInstance().fireEvent(new FullParseEvent(article));
				}
				stopWatch.stop();
				LOGGER.info("Update of wiki lasts " + stopWatch);
			}
		}
		catch (GitAPIException | IOException e) {
			LOGGER.error("Error while updating!", e);
			try {
				git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
			}
			catch (GitAPIException ex) {
				LOGGER.error("Error while aborting rebase!", e);
			}
		}
		finally {
			try {
				fileProvider.pushUnlock();
			}
			catch (IllegalMonitorStateException ignored) {
			}
			LOGGER.info("Commit compile");
			articleManager.commit();
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException ignored) {
			}
			Compilers.awaitTermination(Compilers.getCompilerManager(Environment.DEFAULT_WEB));
			LOGGER.info("Compile ends");
			running = false;
		}
	}

	private void mapRevCommit(ObjectReader objectReader, CanonicalTreeParser oldTreeParser, CanonicalTreeParser newTreeParser, DiffFormatter diffFormatter, RevCommit commit, Collection<String> refreshedPages) throws IOException {
		final RevCommit[] parents = commit.getParents();
		RevTree tree = commit.getTree();
		if (parents.length > 0) {
			oldTreeParser.reset(objectReader, commit.getParent(0)
					.getTree());
			newTreeParser.reset(objectReader, tree);
			List<DiffEntry> diffs = diffFormatter.scan(oldTreeParser, newTreeParser);
			RenameDetector rd = new RenameDetector(repository);
			rd.addAll(diffs);
			diffs = rd.compute();
			for (final DiffEntry diff : diffs) {
				String path;
				if (diff.getChangeType() == DiffEntry.ChangeType.MODIFY) {
					path = diff.getOldPath();
					if (path != null) {
//						cache.mapCommit(commit, path);
						String toUpdate = refreshWikiCache(path);
						choosePageForEvent(refreshedPages, toUpdate);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
					path = diff.getNewPath();
					if (path != null) {
//						cache.mapCommit(commit, path);
						String toUpdate = refreshWikiCache(path);
						choosePageForEvent(refreshedPages, toUpdate);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
//					cache.mapDelete(commit, diff.getOldPath());
					String toUpdate = refreshWikiCache(diff.getOldPath());
					choosePageForEvent(refreshedPages, toUpdate);
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
//					cache.mapMove(commit, diff.getOldPath(), diff.getNewPath());
					String toDelete = refreshWikiCache(diff.getOldPath());
					choosePageForEvent(refreshedPages, toDelete);
					String toAdd = refreshWikiCache(diff.getNewPath());
					choosePageForEvent(refreshedPages, toAdd);
				}
			}
		}
		else {
			final TreeWalk tw = new TreeWalk(this.repository);
			tw.reset(tree);
			tw.setRecursive(true);
			while (tw.next()) {
//				cache.mapCommit(commit, tw.getPathString());
				String toUpdate = refreshWikiCache(tw.getPathString());
				choosePageForEvent(refreshedPages, toUpdate);
			}
		}
	}

	private void choosePageForEvent(Collection<String> refreshedPages, String toUpdate) {
		if (toUpdate != null) {
			if (toUpdate.contains("/")) {
				String[] attachment = toUpdate.split("/");
				if (!attachment[1].contains("ci-build")) {
					refreshedPages.add(attachment[0]);
				}
			}
			else {
				refreshedPages.add(toUpdate);
			}
		}
	}

	private String refreshWikiCache(String path) {
		try {
			Page toRefresh;
			if (path.contains("/")) {
				String[] split = path.split("/");
				String parentName = TextUtil.urlDecodeUTF8(split[0])
						.replace(GitVersioningAttachmentProvider.DIR_EXTENSION, "");
				String attachmentName = TextUtil.urlDecodeUTF8(split[1]);
				toRefresh = new Attachment(engine, parentName, attachmentName);
			}
			else {
				toRefresh = new WikiPage(engine, TextUtil.urlDecodeUTF8(path.replace(GitVersioningFileProvider.FILE_EXT, "")));
			}
			toRefresh.setVersion(WikiProvider.LATEST_VERSION);
			PageManager manager = engine.getManager(PageManager.class);
			manager.getProvider().deleteVersion(toRefresh, PageProvider.LATEST_VERSION);

			Page page = manager.getPage(toRefresh.getName());
			if (page != null) {
				LOGGER.info("refresh call" + page.getName());
			}

			return toRefresh.getName();
		}
		catch (ProviderException e) {
			LOGGER.error("error refreshing cache", e);
		}
		return null;
	}
}
