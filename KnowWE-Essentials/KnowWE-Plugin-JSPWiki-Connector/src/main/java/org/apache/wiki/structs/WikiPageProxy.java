package org.apache.wiki.structs;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.providers.GitVersioningUtils;
import org.apache.wiki.providers.gitCache.history.GitHistoryProvider;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;

public class WikiPageProxy extends WikiPage {

	private GitHistoryProvider historyProvider;

	public WikiPageProxy(Engine engine, String name) {
		super(engine, name);
	}

	public void setHistoryProvider(GitHistoryProvider historyProvider) {
		this.historyProvider = historyProvider;
	}

	@Override
	public int getVersion() {
		int version = super.getVersion();

		if (version != -1) {
			return version;
		}
		else {
			//would be required to set from git, however i dont think there is a way to trigger this aside from the initial call
			// to get all pages and i cant do this during that time as it would end up being way too slow!
			int a = 2;
		}

		return super.getVersion();
	}

	@Override
	public String getAuthor() {
		String author = super.getAuthor();

		if (author != null) {
			return author;
		}


		try {
			List<Page> pageHistory = this.historyProvider.getPageHistory(PageIdentifier.fromPagename(this.historyProvider.basePath(), this.getName(), -1));
			if (pageHistory == null || pageHistory.isEmpty()) {
				return null;
			}
			String gitAuthor = pageHistory.get(pageHistory.size() - 1).getAuthor();
			super.setAuthor(gitAuthor);
			//Note: this is a workaround, if i would determine the version in the corresponding method the code would end up to slow..
			if (this.getVersion() == -1) {
				super.setVersion(pageHistory.size());
			}
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
		return super.getAuthor();
	}

	@NotNull
	public static WikiPage fromCommit(PageIdentifier pageIdentifier, final RevCommit revCommit, Engine engine, Repository repository) {
		final WikiPage page = new WikiPage(engine, pageIdentifier.pageName());
		page.setAuthor(revCommit.getCommitterIdent().getName());
		page.setLastModified(new Date(1000L * revCommit.getCommitTime()));
		page.setVersion(pageIdentifier.version());
		try {
			page.setSize(GitVersioningUtils.getObjectSize(revCommit, pageIdentifier, repository));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		page.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
		return page;
	}
}
