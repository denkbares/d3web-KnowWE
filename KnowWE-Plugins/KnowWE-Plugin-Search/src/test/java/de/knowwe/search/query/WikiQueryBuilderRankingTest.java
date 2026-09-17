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
import java.util.ArrayList;
import java.util.List;

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

/**
 * The ranking contract of the new search, stated as assertions rather than as an impression. Every case here is one
 * that the current JSPWiki search gets wrong.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiQueryBuilderRankingTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final SectionDocumentBuilder documents = new SectionDocumentBuilder();
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
		index = new WikiSearchIndex(folder.getRoot().toPath().resolve("index"));
	}

	@After
	public void tearDown() throws IOException {
		index.close();
	}

	@Test
	public void aTitleMatchOutranksABodyMatch() throws IOException {
		page("Steckverbinder", "!! Allgemein\nEinleitung zum Thema.\n");
		page("Wartungsplan", "!! Turnus\nDer Steckverbinder wird halbjaehrlich geprueft.\n");

		assertEquals("Steckverbinder", firstTitle("Steckverbinder"));
	}

	@Test
	public void aHeadingMatchOutranksABodyMatch() throws IOException {
		page("Handbuch A", "!! Korrosion\nHinweise dazu.\n");
		page("Handbuch B", "!! Allgemein\nHier wird Korrosion nur nebenbei erwaehnt.\n");

		assertEquals("Handbuch A › Korrosion", first("Korrosion"));
	}

	@Test
	public void matchingAllWordsOutranksMatchingOne() throws IOException {
		page("Beides", "!! Montage\nSteckverbinder bei der Montage pruefen.\n");
		page("Nur eins", "!! Lager\nDie Montage erfolgt spaeter.\n");

		assertEquals("Beides › Montage", first("Steckverbinder Montage"));
	}

	@Test
	public void aLongQueryWithOneUnknownWordStillFinds() throws IOException {
		page("Kabelbaum", "!! Montage\nDen Steckverbinder vor der Montage auf Korrosion pruefen.\n");

		// "Ersatzteil" appears nowhere -- with a plain AND this would return nothing
		List<String> hits = search("Steckverbinder Montage Korrosion Ersatzteil");
		assertFalse("a single unknown word must not empty the result", hits.isEmpty());
	}

	@Test
	public void aTypoStillFindsTheSection() throws IOException {
		page("Kabelbaum", "!! Montage\nDen Steckverbinder pruefen.\n");

		assertEquals("Kabelbaum › Montage", first("Steckverbinter"));
	}

	@Test
	public void anExactMatchOutranksATypoMatch() throws IOException {
		page("Exakt", "!! Kapitel\nDer Steckverbinder ist gemeint.\n");
		page("Vertippt", "!! Kapitel\nDer Steckverbinter ist gemeint.\n");

		assertEquals("Exakt › Kapitel", first("Steckverbinder"));
	}

	@Test
	public void typingAPrefixAlreadyFindsWhileStillTyping() throws IOException {
		page("Steckverbinder Handbuch", "!! Allgemein\nEinleitung.\n");

		assertFalse("as-you-type must find on a prefix",
				search(new SearchRequest("steckver", true, 0, 10)).isEmpty());
		assertTrue("without the partial flag a prefix is just an unknown word",
				search(new SearchRequest("steckver", false, 0, 10)).isEmpty());
	}

	@Test
	public void spellingTheSigilRanksTheMarkupFirst() throws IOException {
		page("Erklaerseite", "!! Fragen\nAuf dieser Seite geht es um Package und nochmal Package.\n");
		page("Wissensbasis", "!! Definition\ntext\n\n%%Package\ndemo\n%\n");

		assertEquals("typing the sigil is a deliberate request for the markup",
				"Wissensbasis › Definition", first("%%Package"));
	}

	@Test
	public void theBareNameStillFindsTheMarkupButDoesNotDominate() throws IOException {
		page("Erklaerseite", "!! Fragen\nAuf dieser Seite geht es um Package und nochmal Package.\n");
		page("Wissensbasis", "!! Definition\ntext\n\n%%Package\ndemo\n%\n");

		List<String> hits = search("Package");
		assertTrue("the markup must still be findable by its bare name: " + hits,
				hits.contains("Wissensbasis › Definition"));
		assertEquals("but prose about the word stays ahead of it", "Erklaerseite › Fragen", hits.get(0));
	}

	@Test
	public void aQuotedPhraseOnlyMatchesTheWordsInOrder() throws IOException {
		page("Richtig", "!! Kapitel\nDie Montage der Steckverbinder erfolgt zuletzt.\n");
		page("Falsch", "!! Kapitel\nDie Steckverbinder liegen bereit, die Montage folgt.\n");

		assertEquals(List.of("Richtig › Kapitel"), search("\"Montage der Steckverbinder\""));
	}

	@Test
	public void anEmptyQueryFindsNothingRatherThanEverything() throws IOException {
		page("Irgendwas", "!! Kapitel\nInhalt\n");

		assertEquals(List.of(), search("   "));
	}

	private void page(String title, String content) throws IOException {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		index.replacePage(title, documents.build(article));
		index.refresh();
	}

	private String first(String query) throws IOException {
		List<String> hits = search(query);
		assertFalse("expected a hit for '" + query + "'", hits.isEmpty());
		return hits.get(0);
	}

	private String firstTitle(String query) throws IOException {
		return first(query).split(" › ")[0];
	}

	private List<String> search(String query) throws IOException {
		return search(new SearchRequest(query));
	}

	private List<String> search(SearchRequest request) throws IOException {
		return index.search(searcher -> {
			StoredFields stored = searcher.storedFields();
			List<String> breadcrumbs = new ArrayList<>();
			for (ScoreDoc hit : searcher.search(queries.build(request), 20).scoreDocs) {
				breadcrumbs.add(stored.document(hit.doc).get(SearchFields.BREADCRUMB));
			}
			return breadcrumbs;
		});
	}
}
