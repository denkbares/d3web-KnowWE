package org.apache.wiki.event;

import java.util.Collection;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 04.09.20
 */
public class GitRefreshCacheEvent extends GitVersioningWikiEvent {
	public static final int UPDATE = 0;
	public static final int DELETE = 1;
	public GitRefreshCacheEvent(Object src, int type, String page) {
		super(src, type, null, page, null);
	}

	public GitRefreshCacheEvent(Object src, int type, Collection<String> pages) {
		super(src, type, null, pages, null);
	}
}
