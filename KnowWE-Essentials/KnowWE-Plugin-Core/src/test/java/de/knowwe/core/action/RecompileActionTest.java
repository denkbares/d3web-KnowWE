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

package de.knowwe.core.action;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RecompileActionTest {

	@Test
	public void resolvesLatestArticleVersionsInsideRegistrationFrame() {
		Article staleArticle = temporaryArticle("Article", "stale content");
		Article currentArticle = temporaryArticle("Article", "current content");
		Article deletedArticle = temporaryArticle("Deleted", "deleted content");
		ArticleManager articleManager = new CurrentArticleManager(Map.of("Article", currentArticle));

		List<Article> resolvedArticles = RecompileAction.resolveCurrentArticles(
				articleManager, List.of(staleArticle, deletedArticle));

		assertEquals(1, resolvedArticles.size());
		assertSame(currentArticle, resolvedArticles.get(0));
	}

	private static Article temporaryArticle(String title, String content) {
		return Article.createTemporaryArticle(content, title, "test", new RootType());
	}

	private record CurrentArticleManager(Map<String, Article> articles) implements ArticleManager {

		@Override
		public CompilerManager getCompilerManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getWeb() {
			return "test";
		}

		@Override
		public Article getArticle(String title) {
			return articles.get(title);
		}

		@Override
		public Collection<Article> getArticles() {
			return articles.values();
		}

		@Override
		public Article registerArticle(String title, String content) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteArticle(String title) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeAllArticles() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<Article> getQueuedArticles() {
			return List.of();
		}

		@Override
		public void open() {
		}

		@Override
		public void commit() {
		}

		@Override
		public void rollback() {
		}

		@Override
		public boolean isLive(Section<?> section) {
			return false;
		}

		@Override
		public boolean isInitialized() {
			return true;
		}
	}
}
