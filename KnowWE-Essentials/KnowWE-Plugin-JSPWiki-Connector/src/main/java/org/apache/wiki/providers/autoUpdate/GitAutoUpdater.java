package org.apache.wiki.providers.autoUpdate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.event.GitRefreshCacheEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.GitVersioningAttachmentProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;
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
import org.jetbrains.annotations.Nullable;
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
		repository = fileProvider.getRepository();
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

		Git git = new Git(fileProvider.getRepository());
		ArticleManager articleManager = getDefaultArticleManager();

		updateMasterBranch(articleManager, git);
	}

	private void updateMasterBranch(ArticleManager articleManager, Git git) {
		try {
			performRebase(articleManager, git);
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
			postprocess(articleManager);
			running = false;
		}
	}

	private void performRebase(ArticleManager articleManager, Git git) throws IOException, GitAPIException {
		//wait until KnowWe compiler is done
		Compilers.awaitTermination(Compilers.getCompilerManager(Environment.DEFAULT_WEB));

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		articleManager.open();
		fileProvider.pushLock();

		boolean pullAfterReset = false;

		try {
			Status status = git.status().call();
			if (!status.isClean()) {

				logStatus(status);

				try {
					//we abort the rebase
					pullAfterReset = abortRebase(git);
				}
				catch (GitAPIException e) {
					LOGGER.error("Reset wasn't successful", e);
					return;
				}
				//fallback is hard reset or abort if even this fails!
				boolean resetSuccess = resetHard(git);
				if (resetSuccess) {
					return;
				}
			}
		}
		catch (GitAPIException e) {
			LOGGER.error("Status query wasn't successful, quiting update", e);
			return;
		}

		RevWalk revWalk = new RevWalk(fileProvider.getRepository());
		ObjectId oldHead = fileProvider.getRepository().resolve("remotes/origin/master");
		RevCommit oldHeadCommit = revWalk.parseCommit(oldHead);

		FetchCommand fetch1 = git.fetch();
		FetchResult fetch = fetch1.call();
		Collection<TrackingRefUpdate> trackingRefUpdates = fetch.getTrackingRefUpdates();

		if (pullAfterReset || !trackingRefUpdates.isEmpty()) {

			//pull with rebase
			PullResult pullResult = getPullResult(git);

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
			fileProvider.pushUnlock();
			ObjectId newHead = fileProvider.getRepository().resolve(Constants.HEAD);

			String title = null;
			if (!oldHeadCommit.equals(newHead)) {
				LOGGER.info("Read changes after rebase");
				title = readChangesAfterRebase(revWalk, newHead, git, oldHeadCommit, oldHead);
			}
			if (title != null) {
				LOGGER.info("do full parse");
				Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
				//lets hope this sticks!
				EventManager.getInstance().fireEvent(new FullParseEvent(article,null));
			}
			stopWatch.stop();
			LOGGER.info("Update of wiki lasts " + stopWatch);
		}
	}

	private boolean abortRebase(Git git) throws GitAPIException {
		boolean pullAfterReset = false;
		switch (repository.getRepositoryState()) {
			case REBASING_INTERACTIVE, REBASING, REBASING_REBASING, REBASING_MERGE -> {
				git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
				pullAfterReset = true;
			}
		}
		return pullAfterReset;
	}

	@Nullable
	private PullResult getPullResult(Git git) throws GitAPIException {
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
		return pullResult;
	}

	private void postprocess(ArticleManager articleManager) {
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
	}

	private boolean resetHard(Git git) {
		try {
			git.reset().setMode(ResetCommand.ResetType.HARD).call();
		}
		catch (GitAPIException e) {
			LOGGER.error("Reset wasn't successful", e);
			return false;
		}
		return true;
	}

	private static void logStatus(Status status) {
		LOGGER.warn("Git is not clean, doing reset first");
		LOGGER.warn("Added              : " + String.join(",", status.getAdded()));
		LOGGER.warn("Uncommitted changes: " + String.join(",", status.getUncommittedChanges()));
		LOGGER.warn("Missing            : " + String.join(",", status.getMissing()));
		LOGGER.warn("Modified           : " + String.join(",", status.getModified()));
		LOGGER.warn("Changed            : " + String.join(",", status.getChanged()));
		LOGGER.warn("Conflicting        : " + String.join(",", status.getConflicting()));
	}

	@Nullable
	private String readChangesAfterRebase(RevWalk revWalk, ObjectId newHead, Git git, RevCommit oldHeadCommit, ObjectId oldHead) throws IOException, GitAPIException {
		String title;
		try {
			fileProvider.canWriteFileLock();
			RevCommit newHeadCommit = revWalk.parseCommit(newHead);

			git.log().addRange(oldHeadCommit, newHeadCommit).call();

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
			Collection<String> toRefresh = new HashSet<>();
			Collection<RevCommit> revWalkCommits = new ArrayList<>();
			while (iterator.hasNext()) {
				RevCommit commit = iterator.next();
				revWalkCommits.add(commit);
			}

			for (RevCommit commit : revWalkCommits) {
				if (commit.equals(oldHeadCommit)) {
					parse = true;
					continue;
				}
				if (parse) {
					mapRevCommit(objectReader, oldTreeParser, newTreeParser, diffFormatter, commit, toRefresh);
				}
			}
			Set<String> refreshedPages = new TreeSet<>();
			for (String path : toRefresh) {
				chooseAndUpdate(refreshedPages, path);
			}
			LOGGER.info("Beginn compile foo");

			TreeSet<String> debugSet = new TreeSet<>(refreshedPages);
			for (String refreshedPage : debugSet) {
				LOGGER.info("Refreshed Page: " + refreshedPage);
			}

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
		return title;
	}



	private void mapRevCommit(ObjectReader objectReader, CanonicalTreeParser oldTreeParser, CanonicalTreeParser newTreeParser, DiffFormatter diffFormatter, RevCommit commit, Collection<String> toRefresh) throws IOException {
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
						toRefresh.add(path);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
					path = diff.getNewPath();
					if (path != null) {
						toRefresh.add(path);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
					toRefresh.add(diff.getOldPath());
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
					toRefresh.add(diff.getOldPath());
					toRefresh.add(diff.getNewPath());
				}
			}
		}
		else {
			final TreeWalk tw = new TreeWalk(this.repository);
			tw.reset(tree);
			tw.setRecursive(true);
			while (tw.next()) {
				toRefresh.add(tw.getPathString());
			}
		}
	}

	private void chooseAndUpdate(Collection<String> refreshedPages, String path) {
		String toUpdate = refreshWikiCache(path);
		choosePageForEvent(refreshedPages, toUpdate);
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
				parentName = TextUtil.urlDecodeUTF8(parentName.replace(GitVersioningFileProvider.FILE_EXT, ""));
				String attachmentName = TextUtil.urlDecodeUTF8(split[1]);
				toRefresh = new Attachment(engine, parentName, attachmentName);
				toRefresh.setVersion(PageProvider.LATEST_VERSION);
				AttachmentManager manager = engine.getManager(AttachmentManager.class);
				manager.getCurrentProvider().deleteVersion((Attachment) toRefresh);
				//Page page = manager.getAttachmentInfo(toRefresh.getName());
				//if (page != null) {
				//	LOGGER.info("refresh call" + page.getName());
				//}
			}
			else {
				toRefresh = new WikiPage(engine, TextUtil.urlDecodeUTF8(path.replace(GitVersioningFileProvider.FILE_EXT, "")));
				PageManager manager = engine.getManager(PageManager.class);
				manager.getProvider().deleteVersion(toRefresh, PageProvider.LATEST_VERSION);
				//Page page = manager.getPage(toRefresh.getName());
				//if (page != null) {
				//	LOGGER.info("refresh call" + page.getName());
				//}
			}
			toRefresh.setVersion(WikiProvider.LATEST_VERSION);

			return toRefresh.getName();
		}
		catch (ProviderException e) {
			LOGGER.error("error refreshing cache", e);
		}
		return null;
	}
}
