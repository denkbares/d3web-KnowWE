/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.11.2013
 */
public interface ArticleManager {

	CompilerManager getCompilerManager();

	String getWeb();

	/**
	 * Returns the Article for a given article name/title. The case of the article is ignored.
	 */
	Article getArticle(String title);

	/**
	 * Returns all articles currently registered in this ArticleManager. The returned collection is unmodifiable and is
	 * not sorted.
	 */
	Collection<Article> getArticles();

	/**
	 * Creates and registers a changed or new article in the manager and also compiles it. If this manager already
	 * contains an
	 * article with the same name/title, the existing article will be replaced by the new one.
	 */
	Article registerArticle(String title, String content);

	/**
	 * Deletes the given article from this article manager
	 *
	 * @param title the article to delete
	 */
	void deleteArticle(String title);

	/**
	 * Provides an immutable snapshot of the internal queue of registered/queued, but not yet compiled articles. If
	 * there currently is no open registration frame, the list will always be empty.
	 *
	 * @return the queued articles of the current compilation frame
	 */
	@NotNull
	Collection<Article> getQueuedArticles();

	/**
	 * Opens the manager for registration of articles. Only after calling the method {@link ArticleManager#commit()}
	 * the added articles will be compiled. Make sure to always call commit in an try-finally block!<p>
	 * <b>Attention:</b> Do not call this method synchronously using a compilation thread, because it will cause a dead
	 * lock waiting for the compilation to finish.
	 *
	 * @created 20.12.2013
	 */
	void open();

	/**
	 * Calls this method after opening with {@link ArticleManager#open()}. It causes the compilation of articles
	 * registered since calling the method {@link ArticleManager#open()}. Make sure to always call commit in an
	 * try-finally block!
	 *
	 * @created 20.12.2013
	 */
	void commit();

	/**
	 * Call this method after opening with {@link ArticleManager#open()} in case an error occurred and the changes on
	 * file system have been rolled back also. This could be done in conjunction with a rollback abel FileProvider
	 * (i.e. GitVersioningFileProvider).
	 *
	 * @created 08.04.2019
	 */
	void rollback();

	/**
	 * Checks whether this section belongs to an article that is currently live/used in the article manager. This method
	 * will also work during compilation.
	 *
	 * @param section the section to check the status for
	 * @return whether the section is live/currently in use
	 */
	boolean isLive(Section<?> section);

	/**
	 * Checks whether all articles are initialized after wiki startup.
	 *
	 * @return whether all articles are initialized
	 */
	boolean isInitialized();
}
