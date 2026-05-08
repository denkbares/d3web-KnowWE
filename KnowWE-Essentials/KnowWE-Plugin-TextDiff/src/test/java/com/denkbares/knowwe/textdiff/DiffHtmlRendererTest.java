package com.denkbares.knowwe.textdiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiffHtmlRendererTest {

	private final DiffRenderOptions noShadow = DiffRenderOptions.defaults().withShadowDom(false);

	@Test
	void rendersAddedRemovedAndCommonClasses() {
		TextDiff diff = new TextDiff("a\nB\nc", "a\nb\nc");
		String html = DiffHtmlRenderer.renderTextDiff(diff, noShadow);
		assertTrue(html.contains("class=\"line common"), html);
		assertTrue(html.contains("class=\"line added"), html);
		assertTrue(html.contains("class=\"line removed"), html);
		assertFalse(html.contains("<template"));
	}

	@Test
	void shadowDomWrapperIncludesStylesheet() {
		TextDiff diff = new TextDiff("a", "b");
		String html = DiffHtmlRenderer.renderTextDiff(diff);
		assertTrue(html.contains("<template shadowrootmode=\"open\">"));
		assertTrue(html.contains("KnowWE-Plugin-TextDiff.css"));
	}

	@Test
	void escapesHtmlSpecialChars() {
		TextDiff diff = new TextDiff("<a>&\"'", "different");
		String html = DiffHtmlRenderer.renderTextDiff(diff, noShadow);
		assertTrue(html.contains("&lt;a&gt;&amp;&quot;&apos;"), html);
		assertFalse(html.contains("<a>"), html);
	}

	@Test
	void elidedRegionsAppearWhenLongUnchanged() {
		StringBuilder a = new StringBuilder();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			a.append("line ").append(i).append('\n');
			b.append("line ").append(i).append('\n');
		}
		a.append("END_OLD\n");
		b.append("END_NEW\n");
		TextDiff diff = new TextDiff(a.toString(), b.toString());
		String html = DiffHtmlRenderer.renderTextDiff(diff, noShadow.withContextLines(2));
		assertTrue(html.contains("class=\"elided\""), html);
		assertTrue(html.contains("class=\"line common hidden\""), html);
	}

	@Test
	void contextLinesNegativeShowsAllNoElision() {
		StringBuilder a = new StringBuilder();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			a.append("line ").append(i).append('\n');
			b.append("line ").append(i).append('\n');
		}
		a.append("OLD\n");
		b.append("NEW\n");
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff(a.toString(), b.toString()),
				noShadow.withContextLines(-1));
		assertFalse(html.contains("class=\"elided\""));
		assertFalse(html.contains("hidden"));
	}

	@Test
	void emitsPartAttributes() {
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff("a\nB", "a\nb"), noShadow);
		assertTrue(html.contains("part=\"line common\""), html);
		assertTrue(html.contains("part=\"line added\""), html);
		assertTrue(html.contains("part=\"line removed\""), html);
		assertTrue(html.contains("part=\"num old\""), html);
		assertTrue(html.contains("part=\"num new\""), html);
		assertTrue(html.contains("part=\"sign\""), html);
		assertTrue(html.contains("part=\"text\""), html);
	}

	@Test
	void renderTextDiffShadowContentOmitsHostAndTemplate() {
		String html = DiffHtmlRenderer.renderTextDiffShadowContent(new TextDiff("a", "b"), DiffRenderOptions.defaults());
		assertFalse(html.contains("<knowwe-text-diff"));
		assertFalse(html.contains("<template"));
		assertTrue(html.contains("<link rel=\"stylesheet\""));
		assertTrue(html.contains("<table class=\"diff "), html);
	}

	@Test
	void addedFileUsesSingleNewLineNumberColumn() {
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff(null, "a\nb"), noShadow);
		assertTrue(html.contains("<table class=\"diff single-line-number new-only\""), html);
		assertFalse(html.contains("class=\"num old\""), html);
		assertTrue(html.contains("class=\"num new\""), html);
	}

	@Test
	void removedFileUsesSingleOldLineNumberColumn() {
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff("a\nb", null), noShadow);
		assertTrue(html.contains("<table class=\"diff single-line-number old-only\""), html);
		assertTrue(html.contains("class=\"num old\""), html);
		assertFalse(html.contains("class=\"num new\""), html);
	}

	@Test
	void rendersFileChangeWithEmbeddedDiffAndComputedStats() {
		FileChange change = new FileChange(
				FileChange.ChangeType.RENAMED_MODIFIED,
				"Old<Article>",
				"New&Article",
				null,
				"Wiki.jsp?page=Old&x=1",
				"Wiki.jsp?page=New\"Article\"",
				new TextDiff("a\nb", "a\nc\nd"),
				true);

		String html = DiffHtmlRenderer.renderFileChange(change, noShadow);
		assertTrue(html.startsWith("<knowwe-file-change"), html);
		assertTrue(html.contains("data-change=\"renamed-modified\""), html);
		assertTrue(html.contains("data-old-name=\"Old&lt;Article&gt;\""), html);
		assertTrue(html.contains("data-new-name=\"New&amp;Article\""), html);
		assertTrue(html.contains("data-old-url=\"Wiki.jsp?page=Old&amp;x=1\""), html);
		assertTrue(html.contains("data-new-url=\"Wiki.jsp?page=New&quot;Article&quot;\""), html);
		assertTrue(html.contains("data-additions=\"2\""), html);
		assertTrue(html.contains("data-deletions=\"1\""), html);
		assertTrue(html.contains("data-collapsed"), html);
		assertTrue(html.contains("<knowwe-text-diff"), html);
	}

	@Test
	void rendersRenameOnlyFileChangeWithoutDiffBodyAttributes() {
		FileChange change = new FileChange(
				FileChange.ChangeType.RENAMED,
				"OldArticle",
				"NewArticle",
				null,
				null,
				null,
				null,
				false);

		String html = DiffHtmlRenderer.renderFileChange(change, noShadow);
		assertTrue(html.contains("data-change=\"renamed\""), html);
		assertTrue(html.contains("data-old-name=\"OldArticle\""), html);
		assertTrue(html.contains("data-new-name=\"NewArticle\""), html);
		assertFalse(html.contains("<knowwe-text-diff"), html);
		assertFalse(html.contains("data-additions"), html);
		assertFalse(html.contains("data-deletions"), html);
	}

	@Test
	void sideBySideThrows() {
		assertThrows(UnsupportedOperationException.class,
				() -> DiffHtmlRenderer.renderTextDiff(TextDiff.empty(),
						DiffRenderOptions.defaults().withView(DiffView.SIDE_BY_SIDE)));
	}
}
