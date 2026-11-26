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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import connector.DummyConnector;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import utils.OwnedEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecompileActionTest {

	@Test
	public void resolvesLatestArticleVersionsInsideRegistrationFrame() {
		// the temporary articles belong to an environment, a core-only one is enough here
		try (OwnedEnvironment owned = OwnedEnvironment.create(new DummyConnector(), new RootType())) {
			Environment environment = owned.get();
			Article staleArticle = temporaryArticle(environment, "Article", "stale content");
			Article currentArticle = temporaryArticle(environment, "Article", "current content");
			Article deletedArticle = temporaryArticle(environment, "Deleted", "deleted content");
			ArticleManager articleManager = new CurrentArticleManager(Map.of("Article", currentArticle));

			List<Article> resolvedArticles = RecompileAction.resolveCurrentArticles(
					articleManager, List.of(staleArticle, deletedArticle));

			assertEquals(1, resolvedArticles.size());
			assertSame(currentArticle, resolvedArticles.get(0));
		}
	}

	/**
	 * Recompiling a single article also updates the attachments its markups load from URLs. Loading them may take
	 * long, or hang on an unresponsive server, so it must not happen while the recompilation holds the article manager:
	 * that would block every save of the wiki meanwhile.
	 */
	@Test
	public void attachmentsAreUpdatedAfterTheRegistrationFrameIsClosed() throws Exception {
		RecordingUpdateMarkup markup = new RecordingUpdateMarkup();
		RootType rootType = new RootType();
		rootType.addChildType(0, markup);
		try (OwnedEnvironment owned = OwnedEnvironment.create(new DummyConnector(), rootType)) {
			ArticleManager articleManager = owned.get().getArticleManager();
			Article article = articleManager.registerArticle("Page", "%%RecordingUpdate\n%\n");
			articleManager.getCompilerManager().awaitTermination();

			RecompileAction.recompile(List.of(article), RecompileAction.Mode.single, "test", "tester");
			articleManager.getCompilerManager().awaitTermination();

			assertEquals("The markup must be updated once", 1, markup.updates.size());
			Update update = markup.updates.get(0);
			assertFalse("The update must not hold the article manager", update.articleManagerOpened());
			Article recompiled = articleManager.getArticle("Page");
			assertTrue("The update must get the sections of the recompiled article",
					update.sections().stream().allMatch(section -> section.getArticle() == recompiled));
			assertEquals(1, update.sections().size());
		}
	}

	private record Update(boolean articleManagerOpened, List<Section<?>> sections) {
	}

	private static final class RecordingUpdateMarkup extends AttachmentUpdateMarkup {

		private static final DefaultMarkup MARKUP = new DefaultMarkup("RecordingUpdate");
		private final List<Update> updates = new ArrayList<>();

		RecordingUpdateMarkup() {
			super(MARKUP);
		}

		@Override
		public void performUpdates(Collection<? extends Section<? extends AttachmentUpdateMarkup>> sections, boolean force, boolean allowWaitForOtherDownloads) {
			Section<?> first = sections.iterator().next();
			updates.add(new Update(first.getArticleManager().isOpenedByCurrentThread(), List.copyOf(sections)));
		}

		@Override
		public WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) {
			return null;
		}

		@Override
		public String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section) {
			return section.getTitle() + "/data.txt";
		}

		@Override
		public URL getUrl(Section<? extends AttachmentUpdateMarkup> section) {
			return null;
		}
	}

	private static Article temporaryArticle(Environment environment, String title, String content) {
		return Article.createTemporaryArticle(environment, content, title, new RootType());
	}

	private record CurrentArticleManager(Map<String, Article> articles) implements ArticleManager {

		@Override
		public CompilerManager getCompilerManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NotNull Environment getEnvironment() {
			throw new UnsupportedOperationException();
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
