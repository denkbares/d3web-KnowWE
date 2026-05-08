package de.knowwe.jspwiki.changeannotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.pages.PageManager;

import com.denkbares.knowwe.changeannotations.PageAnnotation;
import com.denkbares.knowwe.changeannotations.PageAnnotator;
import com.denkbares.knowwe.changeannotations.VersionEntry;
import com.denkbares.strings.Strings;

/**
 * Bridges {@link PageManager}'s version history into {@link VersionEntry} input for the
 * pure {@link PageAnnotator}. Thin adapter — the algorithm itself stays JSPWiki-free for
 * unit testing.
 */
public final class JspWikiPageAnnotator {

	private JspWikiPageAnnotator() {
	}

	/**
	 * Loads the full version history of {@code pageName} and computes the line-by-line
	 * annotation for the latest version.
	 *
	 * @throws IllegalArgumentException if the page has no versions on record
	 */
	public static PageAnnotation annotate(Engine engine, String pageName) {
		if (engine == null) throw new NullPointerException("engine");
		if (pageName == null) throw new NullPointerException("pageName");
		List<VersionEntry> versions = collectVersions(engine, pageName);
		return PageAnnotator.annotate(pageName, versions);
	}

	static List<VersionEntry> collectVersions(Engine engine, String pageName) {
		PageManager pageManager = engine.getManager(PageManager.class);
		List<? extends Page> history = pageManager.getVersionHistory(pageName);
		if (history == null || history.isEmpty()) return List.of();
		List<VersionEntry> result = new ArrayList<>(history.size());
		for (Page page : history) {
			String text = pageManager.getPureText(pageName, page.getVersion());
			String author = page.getAuthor();
			Date lastModified = page.getLastModified();
			Instant date = lastModified == null ? Instant.EPOCH : lastModified.toInstant();
			String changeNote = page.getAttribute(Page.CHANGENOTE);
			result.add(new VersionEntry(
					page.getVersion(),
					Strings.isBlank(author) ? "unknown" : author,
					date,
					changeNote,
					text == null ? "" : text));
		}
		return result;
	}
}
