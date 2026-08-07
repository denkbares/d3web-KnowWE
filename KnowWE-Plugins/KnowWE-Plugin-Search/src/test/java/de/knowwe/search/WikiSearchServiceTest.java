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

package de.knowwe.search;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleManagerCommitDoneEvent;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.search.query.SearchRequest;
import de.knowwe.search.query.SearchResults;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Drives the service through the events the wiki actually fires, so the wiring is proven rather than only compiled.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchServiceTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private WikiSearchService service;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		service = new WikiSearchService(folder.getRoot().toPath().resolve("index"));
		service.openForTest();
	}

	@After
	public void tearDown() {
		service.shutdown();
	}

	@Test
	public void theInitialEventIndexesEverythingThatIsAlreadyThere() throws Exception {
		write("Kabelbaum", "!! Montage\nEnthaelt Quetschhuelse.\n");
		write("Prüfplan", "!! Turnus\nEnthaelt Ruettelpruefung.\n");

		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();

		assertEquals("Kabelbaum › Montage", firstHit("Quetschhuelse"));
		assertEquals("Prüfplan › Turnus", firstHit("Ruettelpruefung"));
	}

	@Test
	public void anEditedPageBecomesSearchableAfterTheCommit() throws Exception {
		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();

		write("Neu", "!! Kapitel\nEnthaelt Schrumpfschlauch.\n");
		service.notify(new ArticleRegisteredEvent(article("Neu")));
		service.notify(new ArticleManagerCommitDoneEvent(manager(), true));
		service.awaitIdle();

		assertEquals("Neu › Kapitel", firstHit("Schrumpfschlauch"));
	}

	@Test
	public void nothingIsIndexedBeforeTheCommitBoundary() throws Exception {
		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();

		write("Schwebend", "!! Kapitel\nEnthaelt Kabelbinderhalter.\n");
		service.notify(new ArticleRegisteredEvent(article("Schwebend")));
		service.awaitIdle();

		assertTrue("registration alone must not index; a thousand page import would commit a thousand times",
				search("Kabelbinderhalter").isEmpty());
	}

	@Test
	public void anEditReplacesTheFormerContentOfThePage() throws Exception {
		// distinctive words: the Environment is a singleton, so pages of other tests are in the index too
		write("Wandelbar", "!! Kapitel\nEnthaelt Zwirbelfassung.\n");
		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();
		assertEquals("Wandelbar › Kapitel", firstHit("Zwirbelfassung"));

		write("Wandelbar", "!! Kapitel\nEnthaelt Klemmleiste.\n");
		service.notify(new ArticleRegisteredEvent(article("Wandelbar")));
		service.notify(new ArticleManagerCommitDoneEvent(manager(), true));
		service.awaitIdle();

		assertTrue("the old text must be gone", search("Zwirbelfassung").isEmpty());
		assertEquals("Wandelbar › Kapitel", firstHit("Klemmleiste"));
	}

	@Test
	public void aDeletedPageDisappearsFromTheIndex() throws Exception {
		write("Fluechtig", "!! Kapitel\nEnthaelt Loetstuetzpunkt.\n");
		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();
		assertFalse(search("Loetstuetzpunkt").isEmpty());

		service.notify(new ArticleDeletedEvent(article("Fluechtig")));
		service.awaitIdle();

		assertTrue(search("Loetstuetzpunkt").isEmpty());
	}

	@Test
	public void aCommitThatChangedNothingDoesNotFlush() throws Exception {
		service.notify(new InitializedArticlesEvent(manager()));
		service.awaitIdle();

		write("Unbestaetigt", "!! Kapitel\nEnthaelt Sternenstaubkonverter.\n");
		service.notify(new ArticleRegisteredEvent(article("Unbestaetigt")));
		service.notify(new ArticleManagerCommitDoneEvent(manager(), false));
		service.awaitIdle();

		assertTrue("a commit that reports no changes must leave the pending page pending",
				search("Sternenstaubkonverter").isEmpty());

		// and the next real commit picks it up
		service.notify(new ArticleManagerCommitDoneEvent(manager(), true));
		service.awaitIdle();
		assertEquals("Unbestaetigt › Kapitel", firstHit("Sternenstaubkonverter"));
	}

	private String firstHit(String query) throws IOException {
		SearchResults results = search(query);
		assertFalse("expected a hit for '" + query + "'", results.isEmpty());
		return results.hits().get(0).breadcrumb();
	}

	private SearchResults search(String query) throws IOException {
		return service.getSearcher().search(new SearchRequest(query));
	}

	private static ArticleManager manager() {
		return Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
	}

	private static void write(String title, String content) {
		manager().registerArticle(title, content);
	}

	private static de.knowwe.core.kdom.Article article(String title) {
		return Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
	}
}
