/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.search.render;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.user.UserContext;
import de.knowwe.search.index.ArticleChunker;
import de.knowwe.search.index.IndexChunk;
import de.knowwe.search.index.SectionAnchor;
import utils.TestUserContext;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Every chunk that can be found must also be able to show itself.
 * <p>
 * A hit whose preview cannot be rendered falls back to the indexed text, and that text is the wiki source: a reader
 * searching for a table saw {@code | Number | <DD> | ...} and one searching a page that starts with
 * {@code [{TableOfContents}]} saw exactly that, as source. Measured on a real wiki, this hit every chunk before the
 * first heading -- 18 of 100 results -- because the only preview renderer registered above such a section is the one
 * for the whole article, and that one accepts nothing but the root section itself.
 * <p>
 * These tests therefore do not check a rendering, they check <i>coverage</i>: for every chunk of an article the renderer
 * must produce something, and it must be the text of that chunk. What the produced wiki syntax finally looks like as
 * HTML is up to the wiki's own renderer, which is not available here -- {@link DummyConnector} hands the syntax back
 * unchanged, which is what makes the content assertions below readable in the first place.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class PreviewCoverageTest {

	private static final String WEB = Environment.DEFAULT_WEB;

	private final ArticleChunker chunker = new ArticleChunker();
	private final SearchResultRenderer renderer = new SearchResultRenderer();
	private UserContext user;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		// after the environment, and built from a registered article: a context built from a name alone carries a
		// temporary article, whose article manager is null -- and the renderer resolves its anchor through that manager
		user = null;
		// the fixtures reuse section ids across tests, and a cached preview would answer for the wrong one
		PreviewCache.getInstance().clear();
	}

	@Test
	public void aTableBeforeTheFirstHeadingIsRendered() {
		// the case that started this: a page that opens with a plugin call and a table, no heading anywhere above it
		Article article = article("Preview Intro Table", """
				[{TableOfContents }]

				|| Type || Syntax
				| Number | <DD>.<DD>
				| Boolean | "true"

				!! Later Heading
				text below the heading
				""");

		String html = render(article, 0);
		assertNotNull("the intro chunk has no preview at all", html);
		assertTrue("the table is missing: " + html, html.contains("| Number | <DD>.<DD>"));
		assertTrue("the second row is missing: " + html, html.contains("| Boolean |"));
	}

	@Test
	public void aTableKeepsOneLinePerRow() {
		// JSPWiki reads a table row by row: a row that lost its line break is no longer a row
		Article article = article("Preview Table Rows", """
				|| A || B
				| 1 | 2
				| 3 | 4
				""");

		String html = render(article, 0);
		assertNotNull(html);
		List<String> rows = List.of("|| A || B", "| 1 | 2", "| 3 | 4");
		for (String row : rows) {
			assertTrue("row not on a line of its own: " + html, html.contains(row + "\n") || html.endsWith(row));
		}
	}

	@Test
	public void everyChunkOfAnArticleCanBeRendered() {
		// intro, headings, a markup block and the text between blocks -- each of them is a hit a reader can get
		Article article = article("Preview Every Chunk", """
				intro text with a [link|Somewhere]

				|| Head || Cell
				| a | b

				%%Question
				Sex [oc]
				- male
				- female
				%
				text between the blocks

				!!! Top Heading
				body of the top heading

				!! Sub Heading
				| table | inside a heading
				""");

		List<IndexChunk> chunks = chunker.chunk(article);
		assertTrue("the fixture should produce several chunks, got " + kinds(chunks), chunks.size() >= 3);
		for (IndexChunk chunk : chunks) {
			String html = render(article, chunk.ordinal());
			assertNotNull("chunk " + chunk.ordinal() + " (" + chunk.kind() + ") has no preview", html);
			assertTrue("chunk " + chunk.ordinal() + " (" + chunk.kind() + ") renders empty",
					!html.isBlank());
		}
	}

	@Test
	public void thePreviewShowsTheTextOfItsOwnChunk() {
		// a preview that shows the wrong chunk is worse than none: the reader looks for the match and cannot find it
		Article article = article("Preview Chunk Identity", """
				intro mentions ANTELOPE

				!! First
				body mentions BADGER

				!! Second
				body mentions CAPYBARA
				""");

		assertTrue(render(article, 0).contains("ANTELOPE"));
		assertTrue(render(article, 1).contains("BADGER"));
		assertTrue(render(article, 2).contains("CAPYBARA"));

		// and it does not spill over into the neighbouring chunks
		assertTrue(!render(article, 1).contains("ANTELOPE"));
		assertTrue(!render(article, 1).contains("CAPYBARA"));
	}

	@Test
	public void aPageWithoutAnyHeadingIsRendered() {
		Article article = article("Preview Flat Page", """
				just running text, no heading at all, and a table right below

				| one | two
				""");

		String html = render(article, 0);
		assertNotNull(html);
		assertTrue(html.contains("no heading at all"));
		assertTrue(html.contains("| one | two"));
	}

	@Test
	public void anAnchorThatOnlyResolvesToTheArticleShowsItsBeginning() {
		// the last step of the anchor cascade. Rendering the article's own preview renderer here would yield the single
		// link "Go to article", which is not a preview of anything
		Article article = article("Preview Lost Anchor", """
				the beginning of the article

				!! Heading
				something else
				""");

		SectionAnchor anchor = new SectionAnchor(article.getTitle(), "no-such-id", "", null);
		SearchResultRenderer.Rendered rendered = renderer.render(anchor, user);
		assertNotNull("a lost anchor must still show something", rendered);
		assertTrue(rendered.html().contains("the beginning of the article"));
		assertTrue("a fallback anchor has to be marked stale", rendered.stale());
	}

	private Article article(String title, String content) {
		Environment.getInstance().getArticleManager(WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(WEB, title);
		user = new TestUserContext(article);
		return article;
	}

	/** Renders the chunk with the given ordinal the way the search action does it for a hit. */
	private String render(Article article, int ordinal) {
		IndexChunk chunk = chunker.chunk(article).get(ordinal);
		SectionAnchor anchor = new SectionAnchor(article.getTitle(), chunk.anchor().getID(),
				pathOf(chunk), chunk.heading());
		SearchResultRenderer.Rendered rendered = renderer.render(anchor, user);
		return rendered == null ? null : rendered.html();
	}

	private static List<String> kinds(List<IndexChunk> chunks) {
		return chunks.stream().map(chunk -> chunk.kind().name()).toList();
	}

	private static String pathOf(IndexChunk chunk) {
		return String.valueOf(chunk.anchor().getArticle().getRootSection().getChildren().indexOf(chunk.anchor()));
	}
}
