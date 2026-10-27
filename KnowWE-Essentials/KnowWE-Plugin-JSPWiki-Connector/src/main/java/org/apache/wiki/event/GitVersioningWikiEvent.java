package org.apache.wiki.event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wiki.providers.GitVersioningAttachmentProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;

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
	 * @param pages        file names or repository paths of an update, which are reduced to page names
	 * @param gitCommitRev revision number of the commit
	 * @see #ofWikiNames(Object, int, String, Collection, String)
	 */
	public GitVersioningWikiEvent(Object src, int type, String author, Collection<String> pages, String gitCommitRev) {
		super(src, type);
		this.author = author;
		this.pages = pages.stream()
				.map(s -> s.replaceAll(GitVersioningAttachmentProvider.DIR_EXTENSION, "")
						.replaceAll(GitVersioningFileProvider.FILE_EXT, "").replaceAll("\\+", " "))
				.collect(Collectors.toList());
		this.gitCommitRev = gitCommitRev;
	}

	private GitVersioningWikiEvent(Object src, int type, String author, String gitCommitRev, Collection<String> wikiNames) {
		super(src, type);
		this.author = author;
		this.pages = List.copyOf(wikiNames);
		this.gitCommitRev = gitCommitRev;
	}

	/**
	 * Constructs an event for pages and attachments ({@code <page>/<file>}) given by their wiki names, which are
	 * taken as they are.
	 */
	public static GitVersioningWikiEvent ofWikiNames(Object src, int type, String author, Collection<String> wikiNames,
													 String gitCommitRev) {
		return new GitVersioningWikiEvent(src, type, author, gitCommitRev, wikiNames);
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
