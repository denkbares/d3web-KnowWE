package org.apache.wiki.providers.gitCache.async;

import java.util.List;

import org.apache.wiki.api.core.Page;

public class SetPageHistoryDelayedRequest implements DelayedRequest {
	private final String pageName;
	private final List<Page> pages;

	public SetPageHistoryDelayedRequest(String pageName, List<Page> pages) {
		this.pageName = pageName;
		this.pages = pages;
	}

	@Override
	public void executeRequest(AsyncInitGitVersionCache cache) {
		cache.setPageHistory(this.pageName, this.pages);
	}
}
