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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.search.index.SectionAnchor;
import de.knowwe.search.index.SectionDocumentBuilder;
import de.knowwe.search.index.WikiSearchIndex;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearcherTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final SectionDocumentBuilder documents = new SectionDocumentBuilder();
	private WikiSearchIndex index;
	private WikiSearcher searcher;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		index = new WikiSearchIndex(folder.getRoot().toPath().resolve("index"));
		searcher = new WikiSearcher(index);
	}

	@After
	public void tearDown() throws IOException {
		index.close();
	}

	@Test
	public void aHitCarriesBreadcrumbSnippetAndAnchor() throws IOException {
		page("Kabelbaum", "!! Montage\nDen Steckverbinder auf Korrosion pruefen.\n");

		SearchHit hit = searcher.search(new SearchRequest("Korrosion")).hits().get(0);

		assertEquals("Kabelbaum", hit.title());
		assertEquals("Kabelbaum › Montage", hit.breadcrumb());
		assertEquals("Montage", hit.heading());
		assertTrue("the match must be marked up in the snippet: " + hit.snippet(),
				hit.snippet().contains("<mark>Korrosion</mark>"));

		SectionAnchor.Resolution resolution =
				hit.anchor().resolve(Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB));
		assertTrue("the hit must lead back to a live section", resolution.isResolved());
		assertFalse("and it is current", resolution.stale());
	}

	@Test
	public void aShortQueryWithOneMissingWordRelaxesInsteadOfFailing() throws IOException {
		page("Kabelbaum", "!! Montage\nDie Batterie ist geladen.\n");

		// "leere" occurs nowhere; strictly both words are required, which would return nothing
		SearchResults results = searcher.search(new SearchRequest("leere Batterie"));

		assertFalse("a near miss must still show what matched", results.isEmpty());
		assertTrue("and must say that it was relaxed", results.relaxed());
	}

	@Test
	public void aQueryThatMatchesFullyIsNotMarkedRelaxed() throws IOException {
		page("Kabelbaum", "!! Montage\nDie Batterie ist geladen.\n");

		SearchResults results = searcher.search(new SearchRequest("Batterie geladen"));

		assertFalse(results.isEmpty());
		assertFalse(results.relaxed());
	}

	@Test
	public void anImpossibleQueryReportsWhichWordsAreUnknown() throws IOException {
		page("Kabelbaum", "!! Montage\nDie Batterie ist geladen.\n");

		SearchResults results = searcher.search(new SearchRequest("Fluxkompensator Warpkern"));

		assertTrue(results.isEmpty());
		assertEquals("the user should learn why nothing was found",
				2, results.unmatched().size());
	}

	@Test
	public void paginationReturnsTheRequestedWindow() throws IOException {
		for (int i = 0; i < 5; i++) {
			page("Seite " + i, "!! Kapitel\nDer Steckverbinder Nummer " + i + ".\n");
		}

		SearchResults firstPage = searcher.search(new SearchRequest("Steckverbinder", false, 0, 2));
		SearchResults secondPage = searcher.search(new SearchRequest("Steckverbinder", false, 2, 2));

		assertEquals(2, firstPage.hits().size());
		assertEquals(5, firstPage.total());
		assertTrue(firstPage.exact());
		assertFalse("the second page must show other hits",
				firstPage.hits().get(0).breadcrumb().equals(secondPage.hits().get(0).breadcrumb()));
	}

	@Test
	public void anEmptyQueryIsAnsweredWithoutSearching() throws IOException {
		page("Kabelbaum", "!! Montage\nInhalt\n");

		SearchResults results = searcher.search(new SearchRequest("  "));

		assertTrue(results.isEmpty());
		assertEquals(0, results.total());
	}

	@Test
	public void thePageThatIsNamedTheQueryComesFirst() throws IOException {
		page("Cable Nr-24", "!! Beschreibung\nDieses Kabel verbindet die Steuerung mit dem Aktor.\n");
		page("Cable Nr-24 Uebersicht", "!! Liste\nHier steht Cable Nr-24 mehrfach: Cable Nr-24, Cable Nr-24.\n");

		SearchResults results = searcher.search(new SearchRequest("Cable Nr-24"));
		assertEquals("Cable Nr-24 › Beschreibung", results.hits().get(0).breadcrumb());
	}

	@Test
	public void theNameIsRecognisedHoweverItIsTyped() throws IOException {
		// the key is punctuation blind on both sides, so a reader need not reproduce the hyphen
		page("Cable Nr-24", "!! Beschreibung\nEin Kabel.\n");
		page("Anderes", "!! Text\nHier steht cable nr 24 nur im Fliesstext, mehrfach: cable nr 24.\n");

		SearchResults results = searcher.search(new SearchRequest("cable nr 24"));
		assertEquals("Cable Nr-24 › Beschreibung", results.hits().get(0).breadcrumb());
	}

	@Test
	public void wordsStandingTogetherOutrankWordsScattered() throws IOException {
		// rescoring, not a clause of the search: the words apart still match, they just rank below the words together
		page("Zusammen", "!! Angabe\nHier steht Steckverbinder Montage als zusammenhaengende Angabe.\n");
		page("Verstreut", "!! Angabe\nMontage steht hier. Weiter unten und ganz woanders steht Steckverbinder.\n");

		SearchResults results = searcher.search(new SearchRequest("Steckverbinder Montage"));
		assertEquals("Zusammen › Angabe", results.hits().get(0).breadcrumb());
	}

	@Test
	public void theWrongOrderRanksBelowTheRightOne() throws IOException {
		page("Richtig", "!! Angabe\nDer Steckverbinder Montage Hinweis steht so da.\n");
		page("Verdreht", "!! Angabe\nDer Montage Steckverbinder Hinweis steht verdreht da.\n");

		SearchResults results = searcher.search(new SearchRequest("Steckverbinder Montage"));
		assertEquals("Richtig › Angabe", results.hits().get(0).breadcrumb());
	}

	@Test
	public void aSingleWordHasNothingToBeNear() throws IOException {
		// nothing to rescore, and asking for a phrase of one word would be a query that always matches
		assertNull(new WikiQueryBuilder().nearInBody(new SearchRequest("Steckverbinder")));
	}

	private void page(String title, String content) throws IOException {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		index.replacePage(title, documents.build(article));
		index.refresh();
	}
}
