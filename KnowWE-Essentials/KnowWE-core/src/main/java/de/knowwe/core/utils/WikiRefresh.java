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

package de.knowwe.core.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * Brings the wiki back in sync with page files that were changed outside the normal wiki save process, for example by
 * a script, a git operation or an agent writing the content directory directly.
 *
 * <p>Such a change is invisible to the running wiki, because two layers hold a copy of the old content. The wiki
 * engine caches page text and page info, and KnowWE holds the sectionized article. Recompiling does not help, as it
 * re-sectionizes the text KnowWE already has. So the file has to be made visible again from the bottom up, which is
 * what this class does, in one batch so that everything is compiled in a single pass.
 *
 * <p>Attachments are not covered. Neither is the page history of a wiki whose persistence keeps one, which stays as
 * inconsistent as the external write left it.
 */
public final class WikiRefresh {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiRefresh.class);

	private WikiRefresh() {
	}

	/**
	 * Refreshes every article whose file on disk differs from what the wiki holds, including files that were added or
	 * removed. Reads the whole content directory, so prefer {@link #refresh(Collection, String)} when the changed
	 * titles are known.
	 *
	 * @param reason a short description of what caused the refresh, for the compile message and the log
	 * @throws IOException if the wiki persistence cannot be read
	 */
	@NotNull
	public static Result refreshAll(@NotNull String reason) throws IOException {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		ArticleManager articleManager = Environment.getInstance().getArticleManager();

		Set<String> candidates = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		candidates.addAll(connector.readArticleTitlesFromPersistence());
		articleManager.getArticles().forEach(article -> candidates.add(article.getTitle()));

		return apply(connector, articleManager, candidates, reason);
	}

	/**
	 * Refreshes the given titles, dropping those whose file is gone. Titles whose file matches what the wiki already
	 * holds are left alone, so calling this with more titles than necessary is safe.
	 *
	 * @param reason a short description of what caused the refresh, for the compile message and the log
	 */
	@NotNull
	public static Result refresh(@NotNull Collection<String> titles, @NotNull String reason) {
		return apply(
				Environment.getInstance().getWikiConnector(),
				Environment.getInstance().getArticleManager(),
				titles,
				reason
		);
	}

	private static Result apply(
			WikiConnector connector,
			ArticleManager articleManager,
			Collection<String> titles,
			String reason
	) {
		if (titles.isEmpty()) return Result.EMPTY;

		// the wiki still serves the text it read before the external write, so its caches have to go first
		connector.invalidatePageCache(titles);

		List<String> added = new ArrayList<>();
		List<String> updated = new ArrayList<>();
		List<String> removed = new ArrayList<>();
		Map<String, String> toRegister = new LinkedHashMap<>();
		for (String title : titles) {
			String text = connector.getArticleText(title);
			Article article = articleManager.getArticle(title);
			if (text == null) {
				if (article != null) removed.add(title);
			}
			else {
				String cleaned = Article.cleanupText(text);
				if (article == null) {
					added.add(title);
					toRegister.put(title, cleaned);
				}
				else if (!article.getRootSection().getText().equals(cleaned)) {
					updated.add(title);
					toRegister.put(title, cleaned);
				}
			}
		}

		Result result = new Result(List.copyOf(added), List.copyOf(updated), List.copyOf(removed));
		if (result.isEmpty()) return result;

		Stopwatch stopwatch = new Stopwatch();
		LOGGER.info("Refreshing externally changed articles ({} added, {} updated, {} removed). Reason: {}",
				added.size(), updated.size(), removed.size(), reason);
		articleManager.open();
		try {
			articleManager.getCompilerManager().setCompileMessage("Reason: external change refresh, " + reason);
			toRegister.forEach(articleManager::registerArticle);
			removed.forEach(articleManager::deleteArticle);
		}
		finally {
			articleManager.commit();
		}
		stopwatch.log(LOGGER, "Queued " + result.total() + " externally changed article(s) for compilation");
		return result;
	}

	/**
	 * What a refresh changed, as article titles per kind.
	 *
	 * @param added   articles whose file exists but that the wiki did not know
	 * @param updated articles whose file differs from the text the wiki held
	 * @param removed articles the wiki knew but whose file is gone
	 */
	public record Result(@NotNull List<String> added, @NotNull List<String> updated, @NotNull List<String> removed) {

		private static final Result EMPTY = new Result(List.of(), List.of(), List.of());

		public boolean isEmpty() {
			return total() == 0;
		}

		public int total() {
			return added.size() + updated.size() + removed.size();
		}
	}
}
