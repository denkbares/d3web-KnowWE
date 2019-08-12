package org.apache.wiki.providers;

import java.util.Collection;
import java.util.Collections;

import org.apache.wiki.event.WikiEvent;

/**
 * Signals the update of one or more pages in a commit
 *
 * @author Josua Nürnberger
 * @created 2019-08-08
 */
public class GitVersioningWikiEvent extends WikiEvent {

	public static int UPDATE = 1;
	public static int DELETE = 2;
	public static int MOVED = 3;

	/**
	 * Constructs an instance of this event.
	 *
	 * @param src          the Object that is the source of the event.
	 * @param type         the event type.
	 * @param author       author of the page update
	 * @param page         page name of the updated page
	 * @param gitCommitRev revision number of the commit
	 */
	public GitVersioningWikiEvent(Object src, int type, String author, String page, String gitCommitRev) {
		super(src, type);
		this.author = author;
		this.pages = Collections.singletonList(page);
		this.gitCommitRev = gitCommitRev;
	}

	private final String author;
	private final Collection<String> pages;
	private final String gitCommitRev;

	/**
	 * Constructs an instance of this event.
	 *
	 * @param src          the Object that is the source of the event.
	 * @param type         the event type.
	 * @param author       author of the update
	 * @param pages        list of page names of an update
	 * @param gitCommitRev revision number of the commit
	 */
	public GitVersioningWikiEvent(Object src, int type, String author, Collection<String> pages, String gitCommitRev) {
		super(src, type);
		this.author = author;
		this.pages = pages;
		this.gitCommitRev = gitCommitRev;
	}

	public String getAuthor() {
		return author;
	}

	public Collection<String> getPages() {
		return pages;
	}

	public String getGitCommitRev() {
		return gitCommitRev;
	}
}
