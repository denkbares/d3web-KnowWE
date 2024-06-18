package org.apache.wiki.providers.gitCache.history;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.providers.GitVersioningUtils;
import org.apache.wiki.structs.DefaultPageIdentifier;
import org.apache.wiki.structs.PageIdentifier;
import org.apache.wiki.structs.WikiPageProxy;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class RawGitHistoryProvider implements GitHistoryProvider {

	private final File directory;
	private final Engine engine;
	private final Repository repo;
	private final JspGitBridge gitBridge;

	public RawGitHistoryProvider(Repository repo, Engine engine, JspGitBridge gitBridge ) {
		directory = repo.getDirectory();
		this.engine = engine;
		this.repo = repo;
		this.gitBridge = gitBridge ;
	}

	@Override
	public String basePath() {
		return this.gitBridge.getFilesystemPath();
	}

	@Override
	public List<Page> getPageHistory(PageIdentifier pageIdentifier) throws ProviderException {

		String filename = pageIdentifier.fileName();
		List<String> commitHashesForPage = new ArrayList<>();
		try {
			commitHashesForPage = gitBridge.getCommitHashesForPageRaw(filename, false, -1);
		}
		catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}

		List<RevCommit> revCommits = new ArrayList<>();
		for (String commitHash : commitHashesForPage) {
			RevCommit revCommit = GitVersioningUtils.getRevCommit(new Git(this.repo), commitHash);
			if (revCommit != null) {
				revCommits.add(revCommit);
			}
		}

		Collections.reverse(revCommits);
		List<Page> pageVersions = new ArrayList<>();

		int versionNr = 1;
		for (final RevCommit revCommit : revCommits) {
			DefaultPageIdentifier defaultPageIdentifier = PageIdentifier.fromPagename(pageIdentifier.basePath(), pageIdentifier.pageName(), versionNr);
			final WikiPage version = WikiPageProxy.fromCommit(defaultPageIdentifier, revCommit, engine, repo);
			pageVersions.add(version);
			versionNr++;
		}

		return pageVersions;
	}
}


