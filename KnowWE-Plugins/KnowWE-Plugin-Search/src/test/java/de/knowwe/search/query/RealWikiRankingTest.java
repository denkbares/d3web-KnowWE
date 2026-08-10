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

package de.knowwe.search.query;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.ScoreDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.search.index.SearchFields;
import de.knowwe.search.index.SectionDocumentBuilder;
import de.knowwe.search.index.WikiSearchIndex;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Ranking on a real wiki rather than on fixtures. Fixtures prove the mechanics; only real pages show whether the
 * weights make sense against hundreds of competing sections.
 * <p>
 * Skips itself when no wiki is available; {@code -Dknowwe.search.testWiki} points it elsewhere.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class RealWikiRankingTest {

	private static final String WIKI_PROPERTY = "knowwe.search.testWiki";
	private static final String DEFAULT_WIKI = "/home/cody/denkbares/Projects/WikiSearch/vanilla";

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final WikiQueryBuilder queries = new WikiQueryBuilder();
	private WikiSearchIndex index;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		Path wiki = Path.of(System.getProperty(WIKI_PROPERTY, DEFAULT_WIKI));
		assumeTrue("no test wiki at " + wiki, Files.isDirectory(wiki));

		index = new WikiSearchIndex(folder.getRoot().toPath().resolve("index"));
		SectionDocumentBuilder documents = new SectionDocumentBuilder();
		List<Path> files;
		try (Stream<Path> stream = Files.list(wiki)) {
			files = stream.filter(path -> path.getFileName().toString().endsWith(".txt")).sorted().toList();
		}
		for (Path file : files) {
			String title = file.getFileName().toString().replaceFirst("\\.txt$", "").replace('+', ' ');
			Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB)
					.registerArticle(title, Files.readString(file, StandardCharsets.UTF_8));
			Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
			index.replacePage(title, documents.build(article));
		}
		index.commit();
	}

	@After
	public void tearDown() throws IOException {
		if (index != null) index.close();
	}

	@Test
	public void aTitleMatchLeadsAgainstHundredsOfCompetingSections() throws IOException {
		assertTrue("expected a KnowledgeBase page on top, got " + top("knowledge base", 3),
				first("knowledge base").startsWith("Doc KnowledgeBase"));
	}

	@Test
	public void anAcronymInTheTitleBeatsItsMentionInProse() throws IOException {
		assertTrue("expected an ST-BMI page on top, got " + top("bmi", 3), first("bmi").startsWith("ST-BMI"));
	}

	@Test
	public void markupIsFoundOnRealContent() throws IOException {
		assertFalse(top("%%Package", 5).isEmpty());
	}

	@Test
	public void aWordThatOccursNowhereFindsNothing() throws IOException {
		assertEquals(List.of(), top("Fluxkompensator", 5));
	}

	@Test
	public void everyHitCarriesWhatTheResultListNeeds() throws IOException {
		index.search(searcher -> {
			StoredFields stored = searcher.storedFields();
			ScoreDoc[] hits = searcher.search(queries.build(new SearchRequest("knowledge base")), 5).scoreDocs;
			assertTrue(hits.length > 0);
			for (ScoreDoc hit : hits) {
				var document = stored.document(hit.doc);
				assertFalse("breadcrumb", document.get(SearchFields.BREADCRUMB).isBlank());
				assertFalse("title", document.get(SearchFields.TITLE).isBlank());
				assertFalse("section anchor", document.get(SearchFields.SECTION_ID).isBlank());
				assertFalse("fallback anchor", document.get(SearchFields.SECTION_PATH).isBlank());
			}
			return null;
		});
	}

	private String first(String query) throws IOException {
		List<String> hits = top(query, 1);
		assertFalse("expected a hit for '" + query + "'", hits.isEmpty());
		return hits.get(0);
	}

	private List<String> top(String query, int count) throws IOException {
		return index.search(searcher -> {
			StoredFields stored = searcher.storedFields();
			List<String> breadcrumbs = new ArrayList<>();
			for (ScoreDoc hit : searcher.search(queries.build(new SearchRequest(query)), count).scoreDocs) {
				breadcrumbs.add(stored.document(hit.doc).get(SearchFields.BREADCRUMB));
			}
			return breadcrumbs;
		});
	}
}
