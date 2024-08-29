package org.apache.wiki.event;

import java.util.Collection;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 04.09.20
 */
public class GitUpdateByPullPageEvent extends GitVersioningWikiEvent {
	public GitUpdateByPullPageEvent(Object src, String page) {
		super(src,GitVersioningWikiEvent.UPDATE, null, page, null);
	}

	public GitUpdateByPullPageEvent(Object src, int type, Collection<String> pages) {
		super(src, type, null, pages, null);
	}
}
