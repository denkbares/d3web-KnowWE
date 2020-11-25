package org.apache.wiki.providers;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.WikiProvider;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.event.WikiEventManager;
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
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
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

import com.denkbares.utils.Log;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 21.08.20
 */
public class GitAutoUpdater {

	private static final Logger log = Logger.getLogger(GitAutoUpdater.class);

	private final GitVersioningFileProvider fileProvider;
	private final WikiEngine engine;
	private final Repository repository;
	private final GitVersionCache cache;

	public GitAutoUpdater(WikiEngine engine, GitVersioningFileProvider fileProvider) {
		this.fileProvider = fileProvider;
		repository = fileProvider.repository;
		this.engine = engine;
		cache = fileProvider.getCache();
	}

	public void update() {
		Git git = new Git(fileProvider.repository);
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			fileProvider.pushLock();

			RevWalk revWalk = new RevWalk(fileProvider.repository);
			ObjectId oldHead = fileProvider.repository.resolve("remotes/origin/master");
			RevCommit oldHeadCommit = revWalk.parseCommit(oldHead);

			try {
				Status status = git.status().call();
				if (!status.isClean()) {
					Log.warning("Git is not clean, doing reset first");
					try {
						git.reset().setMode(ResetCommand.ResetType.HARD).call();
					}
					catch (GitAPIException e) {
						Log.severe("Reset wasn't successful", e);
						return;
					}
				}
			} catch (GitAPIException e){
				Log.severe("Status query wasn't successful, quiting update", e);
				return;
			}

			FetchCommand fetch1 = git.fetch();
			FetchResult fetch = fetch1.call();
			Collection<TrackingRefUpdate> trackingRefUpdates = fetch.getTrackingRefUpdates();

			if(!trackingRefUpdates.isEmpty()) {
				PullCommand pull = git.pull()
						.setRemote("origin")
						.setRemoteBranchName("master")
						.setStrategy(MergeStrategy.RESOLVE)
						.setRebase(true);
				PullResult pullResult = pull.call();
				RebaseResult rebaseResult = pullResult.getRebaseResult();
				boolean successful = rebaseResult.getStatus().isSuccessful();
				if (!successful) {
					log.error("unsuccessful pull " + String.join(",", rebaseResult.getConflicts()));
				}
				ObjectId newHead = fileProvider.repository.resolve(Constants.HEAD);
				if (!oldHeadCommit.equals(newHead)) {
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
					resetCiBuildCache();
					while (iterator.hasNext()){
						RevCommit commit = iterator.next();
						if(commit.equals(oldHeadCommit)) {
							parse = true;
							continue;
						}
						if(parse) {
							mapRevCommit(objectReader, oldTreeParser, newTreeParser, diffFormatter, commit, refreshedPages);
						}
					}
					if (!refreshedPages.isEmpty())
						WikiEventManager.fireEvent(fileProvider, new GitRefreshCacheEvent(fileProvider, GitRefreshCacheEvent.UPDATE, refreshedPages));
				}
				stopWatch.stop();
				Log.info("Update of wiki lasts "+ stopWatch);
			}
		}
		catch (GitAPIException | IOException e) {
			log.error("Error while updating!", e);
			try {
				git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
			}
			catch (GitAPIException ex) {
				log.error("Error while aborting rebase!", e);
			}
		}
		finally {
			fileProvider.pushUnlock();
		}
	}

	private void resetCiBuildCache() {
		List<Attachment> allLatestAttachments = this.cache.getAllLatestAttachments();
		for (Attachment att : allLatestAttachments){
			if(att.getFileName().contains("ci-build")){
				this.cache.reset(att);
			}
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
						cache.mapCommit(commit, path);
						String toUpdate = refreshWikiCache(path);
						choosePageForEvent(refreshedPages, toUpdate);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
					path = diff.getNewPath();
					if (path != null) {
						cache.mapCommit(commit, path);
						String toUpdate = refreshWikiCache(path);
						choosePageForEvent(refreshedPages, toUpdate);
					}
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
					cache.mapDelete(commit, diff.getOldPath());
					String toUpdate = refreshWikiCache(diff.getOldPath());
					choosePageForEvent(refreshedPages, toUpdate);
				}
				else if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
					cache.mapMove(commit, diff.getOldPath(), diff.getNewPath());
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
				cache.mapCommit(commit, tw.getPathString());
				String toUpdate = refreshWikiCache(tw.getPathString());
				choosePageForEvent(refreshedPages, toUpdate);
			}
		}
	}

	private void choosePageForEvent(Collection<String> refreshedPages, String toUpdate) {
		if(toUpdate!=null) {
			if(toUpdate.contains("/")){
				String[] attachment = toUpdate.split("/");
				if(!attachment[1].contains("ci-build")){
					refreshedPages.add(attachment[0]);
				}
			} else {
				refreshedPages.add(toUpdate);
			}
		}
	}

	private String refreshWikiCache(String path) {
		try {
			WikiPage toRefresh;
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
			engine.deleteVersion(toRefresh);

			WikiPage page = engine.getPage(toRefresh.getName());
			if(page!=null)
				log.info(page.getName());

			return toRefresh.getName();
		}
		catch (ProviderException e) {
			log.error("error refreshing cache", e);
		}
		return null;
	}
}
