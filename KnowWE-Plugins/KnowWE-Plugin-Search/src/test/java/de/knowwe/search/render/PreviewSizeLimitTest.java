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
import static org.junit.Assert.assertNull;

/**
 * Guards that a hit on a very large section falls back to its snippet instead of rendering the whole section.
 */
public class PreviewSizeLimitTest {

	private static final String WEB = Environment.DEFAULT_WEB;

	private final ArticleChunker chunker = new ArticleChunker();
	private UserContext user;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		PreviewCache.getInstance().clear();
	}

	@Test
	public void aSmallSectionIsRendered() {
		Article article = article("Limit Small", table(3));
		assertNotNull(render(new SearchResultRenderer(10_000, 10_000), article, 0));
	}

	@Test
	public void aSectionOverTheSourceLimitIsNotRendered() {
		Article article = article("Limit Large Source", table(500));
		assertNull(render(new SearchResultRenderer(1_000, 1_000_000), article, 0));
	}

	@Test
	public void aHeadingIsMeasuredWithEverythingItsPreviewShows() {
		// the heading chunk itself is short, but its preview renders the sub heading and the table below it
		Article article = article("Limit Heading", "!! Top\nshort\n\n! Below\n" + table(500));
		assertNull(render(new SearchResultRenderer(1_000, 1_000_000), article, 0));
	}

	@Test
	public void aZeroSourceLimitSwitchesPreviewsOff() {
		Article article = article("Limit Off", table(3));
		assertNull(render(new SearchResultRenderer(0, 1_000_000), article, 0));
	}

	@Test
	public void anOutputOverTheHtmlLimitIsNotRenderedAgain() {
		Article article = article("Limit Large Output", table(50));
		SearchResultRenderer renderer = new SearchResultRenderer(1_000_000, 100);
		assertNull(render(renderer, article, 0));
		String sectionId = chunker.chunk(article).get(0).anchor().getID();
		assertEquals("", PreviewCache.getInstance().get(article.getTitle(), sectionId, user.getUserName()));
		assertNull(render(renderer, article, 0));
	}

	private static String table(int rows) {
		StringBuilder table = new StringBuilder("|| Name || Value\n");
		for (int i = 0; i < rows; i++) {
			table.append("| row ").append(i).append(" | value ").append(i).append('\n');
		}
		return table.toString();
	}

	private Article article(String title, String content) {
		Environment.getInstance().getArticleManager(WEB).registerArticle(title, content);
		Article article = Environment.getInstance().getArticle(WEB, title);
		user = new TestUserContext(article);
		return article;
	}

	private String render(SearchResultRenderer renderer, Article article, int ordinal) {
		IndexChunk chunk = chunker.chunk(article).get(ordinal);
		String path = String.valueOf(article.getRootSection().getChildren().indexOf(chunk.anchor()));
		SectionAnchor anchor = new SectionAnchor(article.getTitle(), chunk.anchor().getID(), path, chunk.heading());
		SearchResultRenderer.Rendered rendered = renderer.render(anchor, user);
		return rendered == null ? null : rendered.html();
	}
}
