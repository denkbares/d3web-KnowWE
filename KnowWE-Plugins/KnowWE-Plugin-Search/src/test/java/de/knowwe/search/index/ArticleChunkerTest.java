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
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class ArticleChunkerTest {

	private static final String WEB = Environment.DEFAULT_WEB;

	private final ArticleChunker chunker = new ArticleChunker();

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
	public void headingsCutDisjointChunks() {
		List<IndexChunk> chunks = chunk("Headings", """
				lead in text
				!! Interview
				ask the user
				!! The Knowledge Base
				overview
				! Terminology
				terms live here
				! Package
				package info
				""");

		assertEquals(List.of(
				"Headings",
				"Headings › Interview",
				"Headings › The Knowledge Base",
				"Headings › The Knowledge Base › Terminology",
				"Headings › The Knowledge Base › Package"
		), breadcrumbs(chunks, "Headings"));

		// no text is covered twice: concatenating all chunks reproduces the article
		assertEquals(articleText(chunks), concatenated(chunks));
	}

	@Test
	public void headingLevelsPopTheStack() {
		List<IndexChunk> chunks = chunk("Levels", """
				!!! Top
				a
				!! Middle
				b
				! Deep
				c
				!! Second Middle
				d
				""");

		assertEquals(List.of(
				"Levels › Top",
				"Levels › Top › Middle",
				"Levels › Top › Middle › Deep",
				"Levels › Top › Second Middle"
		), breadcrumbs(chunks, "Levels"));
	}

	@Test
	public void pageWithoutHeadingsSplitsAtMarkupBlocks() {
		List<IndexChunk> chunks = chunk("NoHeadings", """
				some intro prose about connectors

				%%Package
				demo package
				%

				prose between the blocks

				%%Package
				other package
				%

				trailing prose
				""");

		assertEquals(List.of(
				IndexChunk.Kind.INTRO,
				IndexChunk.Kind.MARKUP,
				IndexChunk.Kind.TEXT,
				IndexChunk.Kind.MARKUP,
				IndexChunk.Kind.TEXT
		), chunks.stream().map(IndexChunk::kind).toList());

		// every chunk of a page without headings still carries the page as breadcrumb
		assertTrue(breadcrumbs(chunks, "NoHeadings").stream().allMatch("NoHeadings"::equals));
		assertEquals(articleText(chunks), concatenated(chunks));
	}

	@Test
	public void markupBlockInsideHeadingKeepsTheHeadingPath() {
		List<IndexChunk> chunks = chunk("Mixed", """
				!! Montage
				check the connector

				%%Package
				demo package
				%

				then continue
				""");

		assertEquals(List.of(
				"Mixed › Montage",
				"Mixed › Montage",
				"Mixed › Montage"
		), breadcrumbs(chunks, "Mixed"));
		assertEquals(IndexChunk.Kind.MARKUP, chunks.get(1).kind());
	}

	@Test
	public void articleWithoutAnyStructureIsOneChunk() {
		List<IndexChunk> chunks = chunk("Flat", "just a paragraph\nand another line\n");

		assertEquals(1, chunks.size());
		assertEquals(IndexChunk.Kind.INTRO, chunks.get(0).kind());
		assertEquals("Flat", chunks.get(0).breadcrumb("Flat"));
	}

	@Test
	public void headingWithoutBodyIsStillIndexed() {
		List<IndexChunk> chunks = chunk("Empty", """
				!! Lonely Heading
				!! Second Heading
				body
				""");

		assertEquals(List.of("Empty › Lonely Heading", "Empty › Second Heading"),
				breadcrumbs(chunks, "Empty"));
	}

	private List<IndexChunk> chunk(String title, String content) {
		Environment.getInstance().getArticleManager(WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(WEB, title);
		return chunker.chunk(article);
	}

	private static List<String> breadcrumbs(List<IndexChunk> chunks, String title) {
		return chunks.stream().map(c -> c.breadcrumb(title)).toList();
	}

	private static String articleText(List<IndexChunk> chunks) {
		return chunks.get(0).anchor().getArticle().getRootSection().getText();
	}

	private static String concatenated(List<IndexChunk> chunks) {
		return chunks.stream()
				.flatMap(c -> c.sections().stream())
				.map(Section::getText)
				.collect(Collectors.joining());
	}
}
