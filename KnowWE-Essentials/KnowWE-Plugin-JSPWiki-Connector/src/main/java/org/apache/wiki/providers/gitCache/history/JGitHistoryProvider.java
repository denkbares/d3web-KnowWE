package org.apache.wiki.providers.gitCache.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.providers.GitVersioningUtils;
import org.apache.wiki.structs.PageIdentifier;
import org.apache.wiki.structs.WikiPageProxy;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGitHistoryProvider implements GitHistoryProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(JGitHistoryProvider.class);
	private final Repository repo;
	private final Engine engine;
	private final JspGitBridge gitBridge;

	public JGitHistoryProvider(Repository repo, Engine engine, JspGitBridge gitJSPBridge) {
		this.repo = repo;
		this.engine = engine;
		this.gitBridge = gitJSPBridge;
	}

	@Override
	public List<Page> getPageHistory(PageIdentifier pageIdentifier) throws ProviderException {
		String pageName = pageIdentifier.pageName();
		List<Page> pages = attemptGetPageHistory(pageName);

		if (pages.isEmpty()) {
			String mangledName = JSPUtils.mangleName(pageName);
			if (!mangledName.equals(pageName)) {
				pages = attemptGetPageHistory(mangledName);
			}
		}

		return pages;
	}

	@Override
	public String basePath() {
		return this.gitBridge.getFilesystemPath();
	}

	private List<Page> attemptGetPageHistory(String pageName) throws ProviderException {
		//TODO this feels so super weird!
		String pageFileName = pageName;
		if (!pageFileName.endsWith(".txt")) {
			pageFileName += ".txt";
		}
		final List<Page> pageVersions = new ArrayList<>();
		final Git git = new Git(repo);
		Iterable<RevCommit> revCommitsIterator = null;
		try {
			revCommitsIterator = gitBridge.getRevCommits(pageFileName, git);
		}
		catch (GitAPIException | IOException e) {
			LOGGER.error("Exception reading RevCommits" + e.getMessage());
			throw new ProviderException("Can't get version history for page " + pageName + ": " + e.getMessage());
		}
		LOGGER.info("Access commits for page: " + pageFileName);
		final List<RevCommit> revCommits = GitVersioningUtils.reverseToList(revCommitsIterator);
		int versionNr = 1;
		for (final RevCommit revCommit : revCommits) {
//			final WikiPage version = createWikiPage(JSPUtils.unmangleName(pageName), versionNr, revCommit, engine, repo);
			final WikiPage version = WikiPageProxy.fromCommit(PageIdentifier.fromPagename(this.gitBridge.getFilesystemPath(), JSPUtils.unmangleName(pageName), versionNr), revCommit, engine, repo);
			pageVersions.add(version);
			versionNr++;
		}

		LOGGER.info("Found: " + pageVersions.size() + " different versions");
		return pageVersions;
	}
}