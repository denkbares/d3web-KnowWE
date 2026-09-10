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

	private static Environment environment;

	/**
	 * Private Constructor insures noninstantiabilty.
	 */
	private TestArticleManager() {
	}

	/**
	 * The shared test environment of the tests using this utility: a real environment on a {@link DummyConnector},
	 * created on first use. Tests that need an environment of their own create it via
	 * {@link Environment#create(de.knowwe.core.wikiConnector.WikiConnector)} and close it themselves instead.
	 */
	public static synchronized Environment getEnvironment() {
		if (environment == null || environment.isClosed()) {
			environment = Environment.create(new DummyConnector());
		}
		return environment;
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

		Environment environment = getEnvironment();
		Article article = environment.getArticleManager().getArticle(articleName);
		if (article == null) {
			// Read File containing content
			String content = KnowWEUtils.readFile(filename).replace("\r", "");
			environment.getWikiConnector().createArticle(articleName, TestArticleManager.class.getSimpleName(), content);
			article = environment.getArticleManager().getArticle(articleName);
		}
		try {
			environment.getArticleManager().getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			return null;
		}
		return article;
	}

	public static void clear() {
		ArticleManager articleManager = getEnvironment().getArticleManager();
		Collection<Article> articles = articleManager.getArticles();
		for (Article article : new ArrayList<>(articles)) {
			articleManager.deleteArticle(article.getTitle());
		}
	}
}
