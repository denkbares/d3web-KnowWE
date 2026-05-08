package de.knowwe.jspwiki.changeannotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.pages.PageManager;
import org.junit.Test;

import com.denkbares.knowwe.changeannotations.PageAnnotation;
import com.denkbares.knowwe.changeannotations.VersionEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the JSPWiki-side bridge maps page metadata into {@link VersionEntry}s correctly,
 * including null/missing fields and empty histories. The pure annotator logic lives in
 * {@code KnowWE-Plugin-ChangeAnnotations} and is tested there.
 */
public class JspWikiPageAnnotatorTest {

	@Test
	public void mapsPageMetadataIntoVersionEntries() {
		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);

		Page v1 = pageMock(1, "alice", Date.from(Instant.parse("2026-01-01T00:00:00Z")), "initial");
		Page v2 = pageMock(2, "bob", Date.from(Instant.parse("2026-02-01T00:00:00Z")), "fix");
		when(pageManager.getVersionHistory("Main")).thenReturn(List.of(v1, v2));
		when(pageManager.getPureText("Main", 1)).thenReturn("alpha\n");
		when(pageManager.getPureText("Main", 2)).thenReturn("alpha\nbeta\n");

		List<VersionEntry> entries = JspWikiPageAnnotator.collectVersions(engine, "Main");

		assertEquals(2, entries.size());
		assertEquals("alice", entries.get(0).author());
		assertEquals("initial", entries.get(0).changeNote());
		assertEquals("alpha\n", entries.get(0).text());
		assertEquals("bob", entries.get(1).author());
		assertEquals("fix", entries.get(1).changeNote());
		assertEquals("alpha\nbeta\n", entries.get(1).text());
	}

	@Test
	public void substitutesUnknownAuthorAndEmptyText() {
		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);

		Page v1 = pageMock(1, null, null, null);
		when(pageManager.getVersionHistory("Main")).thenReturn(List.of(v1));
		when(pageManager.getPureText("Main", 1)).thenReturn(null);

		List<VersionEntry> entries = JspWikiPageAnnotator.collectVersions(engine, "Main");

		assertEquals(1, entries.size());
		VersionEntry entry = entries.get(0);
		assertEquals("unknown", entry.author());
		assertEquals(Instant.EPOCH, entry.date());
		assertEquals("", entry.text());
	}

	@Test
	public void emptyHistoryYieldsEmptyList() {
		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		when(pageManager.getVersionHistory(anyString())).thenReturn(new ArrayList<>());

		assertEquals(List.of(), JspWikiPageAnnotator.collectVersions(engine, "Main"));
	}

	@Test
	public void annotateRejectsEmptyHistory() {
		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		when(pageManager.getVersionHistory(anyString())).thenReturn(new ArrayList<>());

		assertThrows(IllegalArgumentException.class,
				() -> JspWikiPageAnnotator.annotate(engine, "Main"));
	}

	@Test
	public void annotateProducesBlameForLatestVersion() {
		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);

		Page v1 = pageMock(1, "alice", Date.from(Instant.parse("2026-01-01T00:00:00Z")), null);
		Page v2 = pageMock(2, "bob", Date.from(Instant.parse("2026-02-01T00:00:00Z")), null);
		when(pageManager.getVersionHistory("Main")).thenReturn(List.of(v1, v2));
		when(pageManager.getPureText("Main", 1)).thenReturn("alpha\n");
		when(pageManager.getPureText("Main", 2)).thenReturn("alpha\nbeta\n");

		PageAnnotation annotation = JspWikiPageAnnotator.annotate(engine, "Main");

		assertEquals(2, annotation.currentVersion());
		assertEquals(2, annotation.lines().size());
		assertEquals(1, annotation.lines().get(0).introducedInVersion());
		assertEquals(2, annotation.lines().get(1).introducedInVersion());
	}

	private static Page pageMock(int version, String author, Date lastModified, String changeNote) {
		Page page = mock(Page.class);
		when(page.getVersion()).thenReturn(version);
		when(page.getAuthor()).thenReturn(author);
		when(page.getLastModified()).thenReturn(lastModified);
		// CHANGENOTE is read via getAttribute("changenote") — match either by key or generically.
		when(page.<String>getAttribute(eq(Page.CHANGENOTE))).thenReturn(changeNote);
		return page;
	}
}
