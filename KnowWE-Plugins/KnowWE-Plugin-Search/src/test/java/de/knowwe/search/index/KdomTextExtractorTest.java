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

package de.knowwe.search.index;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class KdomTextExtractorTest {

	private final ArticleChunker chunker = new ArticleChunker();
	private final KdomTextExtractor extractor = new KdomTextExtractor();

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
	}

	@Test
	public void proseIsKeptAndHeadingMarkersAreDropped() {
		ExtractedText text = extractOnly("Prose", """
				!! Sichtpruefung
				Den Steckverbinder auf Korrosion pruefen.
				""");

		assertEquals("Den Steckverbinder auf Korrosion pruefen.", text.body());
		assertFalse("heading markers must not reach the body", text.body().contains("!!"));
		assertFalse("the heading itself belongs into its own field", text.body().contains("Sichtpruefung"));
	}

	@Test
	public void markupSyntaxIsStrippedButItsContentIsKept() {
		ExtractedText text = extractLast("Markup", """
				intro

				%%Package
				demo package
				%
				""");

		assertTrue("markup payload must be searchable", text.body().contains("demo package"));
		assertFalse("markup delimiters must not reach the body", text.body().contains("%%"));
		assertFalse("closing delimiter must not reach the body", text.body().contains("\n%"));
	}

	@Test
	public void markupNameIsIndexedWithItsSigil() {
		ExtractedText text = extractLast("MarkupName", """
				intro

				%%Package
				demo package
				%
				""");

		assertEquals(List.of("%%Package"), List.copyOf(text.markupTokens()));
	}

	@Test
	public void plainProseHasNoMarkupTokens() {
		ExtractedText text = extractOnly("Plain", "just some running text about connectors\n");

		assertEquals("just some running text about connectors", text.body());
		assertTrue(text.markupTokens().isEmpty());
	}

	@Test
	public void whitespaceIsCollapsed() {
		ExtractedText text = extractOnly("Space", """
				first line

				second     line
				""");

		assertEquals("first line\nsecond line", text.body());
	}

	/** Extracts the single chunk of an article that has exactly one. */
	private ExtractedText extractOnly(String title, String content) {
		List<IndexChunk> chunks = chunk(title, content);
		assertEquals("expected exactly one chunk, got " + chunks.size(), 1, chunks.size());
		return extractor.extract(chunks.get(0).sections());
	}

	private ExtractedText extractLast(String title, String content) {
		List<IndexChunk> chunks = chunk(title, content);
		return extractor.extract(chunks.get(chunks.size() - 1).sections());
	}

	private List<IndexChunk> chunk(String title, String content) {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		return chunker.chunk(article);
	}
}
