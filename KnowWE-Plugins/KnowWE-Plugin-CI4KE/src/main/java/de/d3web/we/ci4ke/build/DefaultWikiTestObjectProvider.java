/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.testing.TestObjectContainer;
import de.d3web.testing.TestObjectProvider;
import com.denkbares.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Jochen Reutelshöfer
 * @created 16.05.2012
 */
public class DefaultWikiTestObjectProvider implements TestObjectProvider {

	private static DefaultWikiTestObjectProvider instance = null;

	private DefaultWikiTestObjectProvider() {

	}

	public static DefaultWikiTestObjectProvider getInstance() {
		if (instance == null) {
			instance = new DefaultWikiTestObjectProvider();
		}
		return instance;
	}

	@Override
	public <T> List<TestObjectContainer<T>> getTestObjects(Class<T> c, String testObjectName) {
		if (c == null) {
			Log.warning("Class given to TestObjectProvider was 'null'");
			return Collections.emptyList();
		}
		List<TestObjectContainer<T>> result = new ArrayList<>();
		if (c.equals(Article.class)) {

			Collection<Article> articlesMatchingPattern = getArticlesMatchingPattern(testObjectName);
			for (Article article : articlesMatchingPattern) {
				result.add(new TestObjectContainer<>(article.getTitle(), c.cast(article)));
			}

		}
		String web = Environment.DEFAULT_WEB;
		if (c.equals(ArticleManager.class)) {
			Object byName = Environment.getInstance().getArticleManager(web);
			if (byName != null) {
				result.add(new TestObjectContainer<>(web, c.cast(byName)));
			}
		}
		if (c.equals(PackageManager.class)) {
			Object byName = KnowWEUtils.getPackageManager(web);
			if (byName != null) {
				result.add(new TestObjectContainer<>(web, c.cast(byName)));
			}
		}

		return result;
	}

	private Collection<Article> getArticlesMatchingPattern(String s) {
		ArticleManager mgr = Environment.getInstance().getArticleManager(
				Environment.DEFAULT_WEB);
		Pattern pattern = Pattern.compile(s);
		List<Article> matchingArticles = new ArrayList<>();
		// we are asynchronous here, so we need a copy of the collection
		// the collection returned from the manager is synchronized, so creating
		// the new collection also happens synchronous
		for (Article article : new ArrayList<>(mgr.getArticles())) {
			String articleName = article.getTitle();
			if (pattern.matcher(articleName).matches()) {
				matchingArticles.add(article);
			}
		}
		return Collections.unmodifiableCollection(matchingArticles);
	}

}
