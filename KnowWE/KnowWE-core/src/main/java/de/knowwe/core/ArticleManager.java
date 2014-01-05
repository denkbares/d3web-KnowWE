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
import java.util.Iterator;
import java.util.Set;

import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.11.2013
 */
public interface ArticleManager {

	/**
	 * Returns the Article for a given article name
	 * 
	 * @param title the title of the article to return
	 */
	public abstract Article getArticle(String title);

	public abstract Iterator<Article> getArticleIterator();

	public abstract Collection<Article> getArticles();

	public abstract Set<String> getTitles();

	/**
	 * Registers an changed article in the manager and also updates depending
	 * articles.
	 * 
	 * @created 14.12.2010
	 * @param article is the changed article to register
	 */
	public abstract void registerArticle(Article article);

	public CompilerManager getCompilerManager();

	public String getWeb();

	public abstract void deleteArticle(Article article);

	/**
	 * 
	 * 
	 * @created 20.12.2013
	 */
	void open();

	void commit();

}