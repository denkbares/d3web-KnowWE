package org.apache.wiki.providers.gitCache.history;

import java.util.List;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.structs.PageIdentifier;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackedJGitHistoryProvider implements GitHistoryProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(BackedJGitHistoryProvider.class);

	private RawGitHistoryProvider rawGitProvider;
	private JGitHistoryProvider gitHistoryProvider;

	public BackedJGitHistoryProvider(Repository repository, Engine engine, JspGitBridge gitBridge) {
		this.rawGitProvider = new RawGitHistoryProvider(repository, engine,gitBridge);
		this.gitHistoryProvider = new JGitHistoryProvider(repository, engine,gitBridge);
	}

	@Override
	public List<Page> getPageHistory(PageIdentifier pageIdentifier) throws ProviderException {

		List<Page> pageHistory;
		try {
			pageHistory = this.rawGitProvider.getPageHistory(pageIdentifier);
		}
		catch (ProviderException e) {
			LOGGER.info("Tried to get page history via raw git command, but failed with: " + e.getMessage());
			pageHistory = this.gitHistoryProvider.getPageHistory(pageIdentifier);
		}

		if (pageHistory.isEmpty()) {
			LOGGER.info("Tried to get page history, but it was empty!");
		}

		return pageHistory;
	}

	@Override
	public String basePath() {
		return rawGitProvider.basePath();
	}
}
