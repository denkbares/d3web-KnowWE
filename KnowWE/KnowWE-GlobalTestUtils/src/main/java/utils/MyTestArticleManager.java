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

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import dummies.KnowWETestWikiConnector;

/**
 * 
 * @author Jochen
 * @created 01.09.2010
 */
public class MyTestArticleManager {

	private final Map<String, KnowWEArticle> articles = new HashMap<String, KnowWEArticle>();

	private static MyTestArticleManager instance = new MyTestArticleManager();

	/**
	 * Private Constructor insures noninstantiabilty.
	 */
	private MyTestArticleManager() {

	}

	public static MyTestArticleManager getInstance() {
		return instance;
	}

	/**
	 * Creates a KnowWEArticle and loads the created Knowledge.
	 * 
	 * filename == title
	 */
	public static KnowWEArticle getArticle(String filename) {

		if (!getInstance().articles.containsKey(filename)) {
			// Read File containing content
			String content = TestUtils.readTxtFile(filename);
			KnowWEArticle article = createArcticleFromSourceFile(content, filename);
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
	private static KnowWEArticle createArcticleFromSourceFile(String content, String filename) {
		// Initialize KnowWE
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());

		int start = filename.lastIndexOf("/") + 1;
		int end = filename.lastIndexOf(".");
		String topic = filename.substring(start, end);

		// Create Article
		KnowWEArticle article = KnowWEArticle.createArticle(content, topic,
				KnowWEEnvironment.getInstance().getRootType(), "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").registerArticle(
				article);
		return article;
	}

	public static void clear() {
		getInstance().articles.clear();
	}

}
