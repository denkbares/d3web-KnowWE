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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import static org.junit.Assume.assumeTrue;

/**
 * Runs the chunker over a real wiki instead of hand written fixtures, and asserts the property that actually matters:
 * every character of readable text ends up in exactly one chunk. Chunks are allowed to drop whitespace between blocks,
 * but never text.
 * <p>
 * Skips itself when no wiki is available, so it does not tie the build to a local directory. Point
 * {@code -Dknowwe.search.testWiki=/path/to/wikicontent} at any JSPWiki page directory to run it elsewhere.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class RealWikiChunkingTest {

	private static final String WIKI_PROPERTY = "knowwe.search.testWiki";
	private static final String DEFAULT_WIKI = "/home/cody/denkbares/Projects/WikiSearch/vanilla";

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
	public void everyPageIsCoveredWithoutLosingText() throws IOException {
		Path wiki = Path.of(System.getProperty(WIKI_PROPERTY, DEFAULT_WIKI));
		assumeTrue("no test wiki at " + wiki, Files.isDirectory(wiki));

		List<String> lostText = new ArrayList<>();
		int pages = 0;
		int chunks = 0;

		for (Path page : pages(wiki)) {
			String title = page.getFileName().toString().replaceFirst("\\.txt$", "").replace('+', ' ');
			Article article = register(title, Files.readString(page, StandardCharsets.UTF_8));
			List<IndexChunk> pageChunks = chunker.chunk(article);
			pages++;
			chunks += pageChunks.size();

			String covered = pageChunks.stream()
					.flatMap(chunk -> chunk.sections().stream())
					.map(Section::getText)
					.collect(Collectors.joining());
			String missing = missing(article.getRootSection().getText(), covered);
			if (!missing.isBlank()) {
				lostText.add(title + ": [" + missing.replace("\n", "\\n") + "]");
			}
		}

		assumeTrue("test wiki is empty", pages > 0);
		assertEquals("chunking must not drop readable text", List.of(), lostText);
		assertTrue("expected more chunks than pages, got " + chunks + " for " + pages + " pages", chunks > pages);
	}

	@Test
	public void everyChunkThatIsNotAHeadingHasReadableText() throws IOException {
		Path wiki = Path.of(System.getProperty(WIKI_PROPERTY, DEFAULT_WIKI));
		assumeTrue("no test wiki at " + wiki, Files.isDirectory(wiki));

		List<String> empty = new ArrayList<>();
		for (Path page : pages(wiki)) {
			String title = page.getFileName().toString().replaceFirst("\\.txt$", "").replace('+', ' ');
			Article article = register(title, Files.readString(page, StandardCharsets.UTF_8));
			for (IndexChunk chunk : chunker.chunk(article)) {
				// a heading is indexed for its title alone, everything else must carry text
				if (chunk.kind() == IndexChunk.Kind.HEADING) continue;
				ExtractedText text = extractor.extract(chunk.sections());
				if (text.isEmpty()) empty.add(title + " / " + chunk.kind() + " / " + chunk.breadcrumb(title));
			}
		}
		assertEquals("chunks without heading must contribute something searchable", List.of(), empty);
	}

	private static List<Path> pages(Path wiki) throws IOException {
		try (Stream<Path> stream = Files.list(wiki)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".txt")).sorted().toList();
		}
	}

	private static Article register(String title, String content) {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		return Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
	}

	/** The characters of full that covered does not contain, in order. */
	private static String missing(String full, String covered) {
		StringBuilder result = new StringBuilder();
		int coveredIndex = 0;
		for (int i = 0; i < full.length(); i++) {
			if (coveredIndex < covered.length() && full.charAt(i) == covered.charAt(coveredIndex)) {
				coveredIndex++;
			}
			else {
				result.append(full.charAt(i));
			}
		}
		return result.toString();
	}
}
