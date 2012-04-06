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

package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;

public class TestUtils {

	public static String readTxtFile(String fileName) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			int char1 = bufferedReader.read();
			while (char1 != -1) {
				inContent.append((char) char1);
				char1 = bufferedReader.read();
			}
			bufferedReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}

	/**
	 * Creates an @link{UserActionContext} for test purposes. Both parameters
	 * are optional (in case you don't need the Action...)
	 * 
	 * @created Apr 28, 2011
	 * @param actionName <strong>optional:</strong> The name of the desired
	 *        Action
	 * @param path <strong>optional:</strong> special path (very unlikely that
	 *        you need this)
	 * @return
	 */
	public static UserActionContext createTestActionContext(String actionName, String path) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(Attributes.WEB, Environment.DEFAULT_WEB);
		map.put(Attributes.USER, "Test User");
		return new ActionContext(actionName != null ? actionName : "", path != null ? path : "",
				map, null, null, null, null);
	}

	public static int countMatches(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * Called by the Core-Junit-Tests
	 */
	public static void processAndUpdateArticleJunit(String username, String content,
			String topic, String web) {
		Environment.getInstance().getArticleManager(web).registerArticle(
				Article.createArticle(content, topic, web));
	}
}
