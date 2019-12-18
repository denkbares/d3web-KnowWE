package org.apache.wiki.providers;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class PageCacheItem extends GitCacheItem {
	private String pageName;

	PageCacheItem(String pageName, String fullMessage, String name, Date date, long size, boolean delete, ObjectId id) {
		super(fullMessage, name, date, size, id, delete);
		this.pageName = pageName;
	}

	public PageCacheItem(String pageName, String commitMsg, String author, Date date, long size, ObjectId id) {
		this(pageName, commitMsg, author, date, size, false, id);
	}

	public String getPageName() {
		return pageName;
	}
}
