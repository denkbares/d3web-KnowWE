package com.denkbares.knowwe.changeannotations;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageAnnotationRendererTest {

	private static final Instant T1 = Instant.parse("2026-01-15T10:30:00Z");
	private static final Instant T2 = Instant.parse("2026-02-20T14:45:00Z");

	@Test
	void rendersOneRowPerLine() {
		PageAnnotation annotation = new PageAnnotation("Main", 2, List.of(
				new LineBlame(1, 1, "alice", T1, null),
				new LineBlame(2, 2, "bob", T2, "fix typo")));

		String html = PageAnnotationRenderer.render(annotation, "alpha\nbeta\n", DiffLinkBuilder.NONE);

		assertEquals(2, countOccurrences(html, "<tr part=\"line\">"));
		assertTrue(html.contains("<knowwe-page-annotate>"), html);
		assertTrue(html.contains("<template shadowrootmode=\"open\">"), html);
		assertTrue(html.contains("KnowWE-Plugin-ChangeAnnotations.css"), html);
	}

	@Test
	void rendersVersionAuthorDateAndText() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of(
				new LineBlame(1, 1, "alice", T1, null)));

		String html = PageAnnotationRenderer.render(annotation, "alpha\n", DiffLinkBuilder.NONE);

		assertTrue(html.contains(">v1<"), html);
		assertTrue(html.contains(">alice<"), html);
		assertTrue(html.contains(">2026-01-15<"), html);
		assertTrue(html.contains(">alpha<"), html);
	}

	@Test
	void linkBuilderProducesAnchorWhenUrlGiven() {
		PageAnnotation annotation = new PageAnnotation("Main", 2, List.of(
				new LineBlame(1, 2, "bob", T2, null)));
		DiffLinkBuilder linkBuilder = version -> "Diff.jsp?page=Main&r1=" + (version - 1) + "&r2=" + version;

		String html = PageAnnotationRenderer.render(annotation, "alpha\n", linkBuilder);

		assertTrue(html.contains("<a href=\"Diff.jsp?page=Main&amp;r1=1&amp;r2=2\""), html);
		assertTrue(html.contains(">v2</a>"), html);
	}

	@Test
	void linkBuilderReturningNullSkipsAnchor() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of(
				new LineBlame(1, 1, "alice", T1, null)));
		DiffLinkBuilder linkBuilder = version -> null;

		String html = PageAnnotationRenderer.render(annotation, "alpha\n", linkBuilder);

		assertFalse(html.contains("<a "), html);
		assertTrue(html.contains(">v1<"), html);
	}

	@Test
	void changeNoteBecomesAuthorTooltip() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of(
				new LineBlame(1, 1, "alice", T1, "initial commit")));

		String html = PageAnnotationRenderer.render(annotation, "alpha\n", DiffLinkBuilder.NONE);

		assertTrue(html.contains("title=\"initial commit\""), html);
	}

	@Test
	void escapesHostileContent() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of(
				new LineBlame(1, 1, "<alice>", T1, "</td><script>alert(1)</script>")));

		String html = PageAnnotationRenderer.render(annotation, "<b>oops</b>\n", DiffLinkBuilder.NONE);

		assertFalse(html.contains("<script>alert(1)</script>"), html);
		assertFalse(html.contains("<b>oops</b>"), html);
		assertTrue(html.contains("&lt;alice&gt;"), html);
		assertTrue(html.contains("&lt;b&gt;oops&lt;/b&gt;"), html);
	}

	@Test
	void mismatchedLineCountThrows() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of(
				new LineBlame(1, 1, "alice", T1, null)));

		assertThrows(IllegalArgumentException.class,
				() -> PageAnnotationRenderer.render(annotation, "alpha\nbeta\n", DiffLinkBuilder.NONE));
	}

	@Test
	void emptyAnnotationProducesEmptyTable() {
		PageAnnotation annotation = new PageAnnotation("Main", 1, List.of());

		String html = PageAnnotationRenderer.render(annotation, "", DiffLinkBuilder.NONE);

		assertTrue(html.contains("<knowwe-page-annotate>"), html);
		assertEquals(0, countOccurrences(html, "<tr part=\"line\">"));
	}

	private static int countOccurrences(String haystack, String needle) {
		int count = 0;
		int idx = 0;
		while ((idx = haystack.indexOf(needle, idx)) != -1) {
			count++;
			idx += needle.length();
		}
		return count;
	}
}
