package com.denkbares.knowwe.textdiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiffHtmlRendererTest {

	@Test
	void hostCarriesPlaceholderAndJsonInShadowRoot() {
		TextDiff diff = new TextDiff("a\nb\nc\n", "a\nB\nc\n");
		String html = DiffHtmlRenderer.renderTextDiff(diff);
		assertTrue(html.startsWith("<knowwe-text-diff data-layout=\"unified\" data-context-lines=\"3\">"), html);
		assertTrue(html.contains("<template shadowrootmode=\"open\">"), html);
		assertTrue(html.contains("KnowWE-Plugin-TextDiff.css"), html);
		assertTrue(html.contains("class=\"diff-frame diff-placeholder\" part=\"frame\" style=\"--diff-placeholder-lines: 5\""), html);
		assertTrue(html.contains("<script type=\"application/json\" class=\"diff-data\">{\"old\":\"a\\nb\\nc\\n\",\"new\":\"a\\nB\\nc\\n\"}</script>"), html);
		assertTrue(html.endsWith("</template></knowwe-text-diff>"), html);
	}

	@Test
	void textInJsonIsSafeInsideTheScriptElement() {
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff("</script><a>", "x"));
		assertFalse(html.contains("</script><a>"), html);
		assertTrue(html.contains("\\u003c/script>\\u003ca>"), html);
	}

	@Test
	void placeholderCountsEveryLineWithoutElision() {
		StringBuilder a = new StringBuilder();
		for (int i = 0; i < 30; i++) a.append("line ").append(i).append('\n');
		String b = a + "extra\n";
		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff(a.toString(), b),
				DiffRenderOptions.defaults().withContextLines(-1));
		assertTrue(html.contains("--diff-placeholder-lines: 32"), html);
	}

	@Test
	void emitsTheRequestedLayoutAndContext() {
		TextDiff diff = new TextDiff("a", "b");
		DiffRenderOptions options = DiffRenderOptions.defaults().withContext("page-history");
		assertTrue(DiffHtmlRenderer.renderTextDiff(diff, options.withView(DiffView.SIDE_BY_SIDE))
				.startsWith("<knowwe-text-diff data-layout=\"split\" data-context=\"page-history\""));
		assertTrue(DiffHtmlRenderer.renderTextDiff(diff, options.withView(DiffView.AUTO))
				.startsWith("<knowwe-text-diff data-layout=\"auto\" data-context=\"page-history\""));
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
				new TextDiff("a\nb", "a\nc\nd"));

		String html = DiffHtmlRenderer.renderFileChange(change);
		assertTrue(html.startsWith("<knowwe-file-change"), html);
		assertTrue(html.contains("data-change=\"renamed-modified\""), html);
		assertTrue(html.contains("data-old-name=\"Old&lt;Article&gt;\""), html);
		assertTrue(html.contains("data-new-name=\"New&amp;Article\""), html);
		assertTrue(html.contains("data-old-url=\"Wiki.jsp?page=Old&amp;x=1\""), html);
		assertTrue(html.contains("data-new-url=\"Wiki.jsp?page=New&quot;Article&quot;\""), html);
		assertTrue(html.contains("data-additions=\"2\""), html);
		assertTrue(html.contains("data-deletions=\"1\""), html);
		assertTrue(html.contains("<knowwe-text-diff"), html);
	}

	@Test
	void rendersRenameOnlyFileChangeWithoutDiffBodyAttributes() {
		FileChange change = new FileChange(FileChange.ChangeType.RENAMED, "OldArticle", "NewArticle", null, null, null, null);

		String html = DiffHtmlRenderer.renderFileChange(change);
		assertTrue(html.contains("data-change=\"renamed\""), html);
		assertTrue(html.contains("data-old-name=\"OldArticle\""), html);
		assertTrue(html.contains("data-new-name=\"NewArticle\""), html);
		assertFalse(html.contains("<knowwe-text-diff"), html);
		assertFalse(html.contains("data-additions"), html);
		assertFalse(html.contains("data-deletions"), html);
	}
}
