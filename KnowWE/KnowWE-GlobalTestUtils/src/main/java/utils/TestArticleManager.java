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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import connector.DummyConnector;
import connector.DummyPageProvider;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Jochen
 * @created 01.09.2010
 */
public class TestArticleManager {

	private final Map<String, Article> articles = new HashMap<String, Article>();

	private static TestArticleManager instance = new TestArticleManager();

	/**
	 * Private Constructor insures noninstantiabilty.
	 */
	private TestArticleManager() {

	}

	public static TestArticleManager getInstance() {
		return instance;
	}

	/**
	 * Creates a Article and loads the created Knowledge.
	 * 
	 * filename == title
	 */
	public static Article getArticle(String filename) {

		if (!getInstance().articles.containsKey(filename)) {
			// Read File containing content
			String content = KnowWEUtils.readFile(filename);
			Article article = createArcticleFromSourceFile(content, filename);
			getInstance().articles.put(filename, article);

			return article;
		}

		return getInstance().articles.get(filename);
	}

	/**
	 * 
	 * @created 01.09.2010
	 * @param content
	 * @return
	 */
	private static Article createArcticleFromSourceFile(String content, String filename) {
		// Initialize KnowWE
		DummyConnector connector = null;
		try {
			// added a DummyPageProvider justs prevents NullPointers from beeing
			// throws.
			// TODO: rewrite/replace this TestArticleManager with one that
			// properly uses the DummyConnector and DummyPageProvider
			connector = new DummyConnector(new DummyPageProvider(new File("")));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		Environment.initInstance(connector);

		int start = filename.lastIndexOf("/") + 1;
		int end = filename.lastIndexOf(".");
		String topic = filename.substring(start, end);
		ArticleManager articleManager = Environment.getInstance().getArticleManager(
				"default_web");
		articleManager.setArticlesInitialized(true);

		// Create Article
		Article article = Article.createArticle(content, topic, "default_web");
		articleManager.registerArticle(article);
		return article;
	}

	public static void clear() {
		getInstance().articles.clear();
	}

}
