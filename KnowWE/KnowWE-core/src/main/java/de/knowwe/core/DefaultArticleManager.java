/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import de.d3web.utils.Log;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.event.ArticleRegisteredEvent;

/**
 * Manages all the articles of one web in a HashMap
 *
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */
public class DefaultArticleManager implements ArticleManager {

	/**
	 * Stores Articles for article-names
	 */
	private Map<String, Article> articleMap = Collections.synchronizedMap(new HashMap<String, Article>());

	private final String web;

	private final CompilerManager compilerManager;

	private final ReentrantLock mainLock = new ReentrantLock(true);

	private List<Section<?>> added = new ArrayList<Section<?>>();
	private List<Section<?>> removed = new ArrayList<Section<?>>();

	public DefaultArticleManager(String web) {
		this.web = web;
		this.compilerManager = new CompilerManager(this);
	}

	@Override
	public String getWeb() {
		return web;
	}

	/**
	 * Returns the Article for a given article name
	 *
	 * @param title the title of the article to return
	 */
	@Override
	public Article getArticle(String title) {
		return articleMap.get(title.toLowerCase());
	}

	@Override
	public Collection<Article> getArticles() {
		return Collections.unmodifiableCollection(articleMap.values());
	}

	/**
	 * Registers a changed or new article in the manager and also compiles it. If you opened a registration frame by
	 * calling {@link ArticleManager#open()}, the added articles since then will not be compiled before calling {@link
	 * ArticleManager#commit()}.<p>
	 * <b>Attention:</b> Do not call this method synchronously from within a compilation thread.
	 *
	 * @param article is the changed or new article to register
	 * @created 20.12.2013
	 */
	@Override
	public void registerArticle(Article article) {
		open();
		try {
			if (article == null) {
				throw new NullPointerException("Article is cannot be null");
			}

			String title = article.getTitle();

			added.add(article.getRootSection());

			Article lastVersion = getArticle(title);
			if (lastVersion != null) removed.add(lastVersion.getRootSection());

			articleMap.put(title.toLowerCase(), article);
			article.setArticleManager(this);

			EventManager.getInstance().fireEvent(
					new ArticleRegisteredEvent(article));
			article.clearLastVersion();
		}
		finally {
			commit();
		}
	}

	/**
	 * Deletes the given article from the article map and invalidates all
	 * knowledge content that was in the article.
	 *
	 * @param article The article to delete
	 */
	@Override
	public void deleteArticle(Article article) {
		Environment.getInstance().buildAndRegisterArticle(web,
				article.getTitle(), "", true);

		articleMap.remove(article.getTitle().toLowerCase());

		Log.info("-> Deleted article '" + article.getTitle() + "'" + " from " + web);
	}

	@Override
	public CompilerManager getCompilerManager() {
		return compilerManager;
	}

	/**
	 * Opens the manager for registration of articles. Only after calling the method {@link ArticleManager#commit()}
	 * the added articles will be compiled. Make sure to always call commit in an try-finally block!<p>
	 * <b>Attention:</b> Do not call this method synchronously from within a compilation thread.
	 */
	@Override
	public void open() {
		if (CompilerManager.isCompileThread()) {
			throw new IllegalStateException("Cannot register articles during compilation");
		}
		mainLock.lock();
		// Since we only start the compilation during the lock and compilation then happens asynchronously,
		// it is possible that a threads runs to this point while compilation is still ongoing from the last lock.
		// Since the next compilation process has to wait for the last one to finish anyway and adding new articles to
		// the manager during compilation might cause problems for some compilers, we just wait for the compilation
		// to finish.
		Compilers.awaitTermination(compilerManager);
	}

	/**
	 * Calls this method after opening with {@link ArticleManager#open()}. It causes the compilation of articles
	 * registered since calling the method {@link ArticleManager#open()}. Make sure to always call commit in an
	 * try-finally block!
	 */
	@Override
	public void commit() {
		try {
			if (mainLock.getHoldCount() == 1) {
				compilerManager.compile(added, removed);
				added = new ArrayList<Section<?>>();
				removed = new ArrayList<Section<?>>();
			}
		}
		finally {
			mainLock.unlock();
		}
	}
}
