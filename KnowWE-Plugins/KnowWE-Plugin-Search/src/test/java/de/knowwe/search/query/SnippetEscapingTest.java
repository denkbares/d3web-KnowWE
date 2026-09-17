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
import de.knowwe.search.index.SectionDocumentBuilder;
import de.knowwe.search.index.WikiSearchIndex;
import utils.TestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The snippet is inserted into the page as HTML, and the body it is cut from is wiki text that anyone with edit rights
 * can write. Both the highlighted and the fallback path must escape it.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SnippetEscapingTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

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
	public void markupInThePageDoesNotSurviveIntoTheSnippetAsHtml() throws IOException {
		page("Boeswillig", "!! Kapitel\nHarmlos <script>alert('x')</script> Zwirbelkontakt danach.\n");

		String snippet = searcher.search(new SearchRequest("Zwirbelkontakt")).hits().get(0).snippet();

		assertFalse("a script tag from the page must not reach the result list: " + snippet,
				snippet.contains("<script>"));
		assertTrue("it must be visible as text instead", snippet.contains("&lt;script&gt;"));
		assertTrue("and the match is still highlighted", snippet.contains("<mark>"));
	}

	@Test
	public void theFallbackSnippetIsEscapedAsWell() throws IOException {
		// a hit whose match is in the title produces no body passage, so the fallback runs
		page("Zwirbelkontakt Handbuch", "!! Kapitel\n<b>Fetter</b> Text ohne Treffer.\n");

		String snippet = searcher.search(new SearchRequest("Zwirbelkontakt")).hits().get(0).snippet();

		assertFalse("the fallback must escape too: " + snippet, snippet.contains("<b>"));
	}

	private void page(String title, String content) throws IOException {
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		index.replacePage(title, new SectionDocumentBuilder().build(article));
		index.refresh();
	}
}
