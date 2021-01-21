/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package utils;

import java.util.ArrayList;
import java.util.Collection;

import connector.DummyConnector;

import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Jochen
 * @created 01.09.2010
 */
public class TestArticleManager {

	private static TestArticleManager instance = null;

	/**
	 * Private Constructor insures noninstantiabilty.
	 */
	private TestArticleManager() {
		Environment.initInstance(new DummyConnector());
	}

	public static TestArticleManager getInstance() {
		if (instance == null) {
			//noinspection InstantiationOfUtilityClass
			instance = new TestArticleManager();
		}
		return instance;
	}

	/**
	 * Creates a Article and loads the created Knowledge.
	 * <p>
	 * filename == title
	 */
	public static Article getArticle(String filename) {

		String articleName = filename;

		if (filename.contains("/") && filename.contains(".")) {
			articleName = Strings.decodeURL(filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.')));
		}

		Article article = KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB).getArticle(
				articleName);
		if (article == null) {
			// Read File containing content
			String content = KnowWEUtils.readFile(filename).replace("\r", "");
			Environment.getInstance()
					.getWikiConnector()
					.createArticle(articleName, TestArticleManager.class.getSimpleName(), content
					);
			article = KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB).getArticle(
					articleName);
		}
		try {
			Compilers.getCompilerManager(Environment.DEFAULT_WEB).awaitTermination();
		}
		catch (InterruptedException e) {
			return null;
		}
		return article;
	}

	public static void clear() {
		ArticleManager articleManager = KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB);
		Collection<Article> articles = articleManager.getArticles();
		for (Article article : new ArrayList<>(articles)) {
			articleManager.deleteArticle(article.getTitle());
		}
	}
}
