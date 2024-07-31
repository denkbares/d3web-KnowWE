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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.user.AuthenticationManager;

public class TestUtils {

	public static String readBytes(Reader r) {
		int zeichen = 0;
		LinkedList<Integer> ints = new LinkedList<>();
		while (true) {

			try {
				zeichen = r.read();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				break;
			}
			catch (OutOfMemoryError e1) {
				break;
			}

			// Ende des Stream erreicht
			if (zeichen == -1) {
				break;
			}

			ints.add(zeichen);
		}

		StringBuilder buffi = new StringBuilder(5000000);
		for (Integer i : ints) {

			if ((i == 128) || (i == 228)
					|| (i == 252) || (i == 246)
					|| (i == 214) || (i == 196)
					|| (i == 220) || (i == 223)) {
				if (i == 128) {
					buffi.append('');
				}
				if (i == 228) {
					buffi.append('ä');
				}
				if (i == 252) {
					buffi.append('ü');
				}
				if (i == 246) {
					buffi.append('ö');
				}
				if (i == 214) {
					buffi.append('ü');
				}
				if (i == 196) {
					buffi.append('Ö');
				}
				if (i == 220) {
					buffi.append('Ü');
				}
				if (i == 223) {
					buffi.append('ß');
				}
			}
			else {
				buffi.append(((char) i.intValue()));
			}
		}
		return buffi.toString();
	}

	public static String ReaderToString(Reader r) {
		return readBytes(r).replace('@', '%');
	}

	/**
	 * Creates an @link{UserActionContext} for test purposes. Both parameters
	 * are optional (in case you don't need the Action...)
	 *
	 * @param actionName <strong>optional:</strong> The name of the desired
	 *                   Action
	 * @param path       <strong>optional:</strong> special path (very unlikely that
	 *                   you need this)
	 * @created Apr 28, 2011
	 */
	public static UserActionContext createTestActionContext(String actionName, String path) {
		Map<String, String> map = new HashMap<>();
		map.put(Attributes.WEB, Environment.DEFAULT_WEB);
		map.put(Attributes.USER, "Test User");
		return new ActionContext(actionName != null ? actionName : "", path != null ? path : "",
				map, new TestHttpServletRequest(), null, null, new TestAuthenticationManager());
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
	public static void processAndUpdateArticleJunit(String username, String content, String topic, String web) {
		Environment.getInstance().getArticleManager(web).registerArticle(topic, content);
	}

	public static String createKnowWEExtensionPath() {
		String hackedPath = System.getProperty("user.dir");
		hackedPath = hackedPath.replaceAll("Research", "KnowWE");
		if (hackedPath.contains("KnowWE-App")) {
			hackedPath = hackedPath.replaceAll("KnowWE-App", "KnowWE");
		}
		else {
			hackedPath += "/..";
		}
		hackedPath += "/KnowWE-Resources/src/main/webapp/KnowWEExtension/";
		File file = new File(hackedPath);
		try {
			return file.getCanonicalPath();
		}
		catch (IOException e) {
			throw new Error(e);
		}
	}

	private static class TestAuthenticationManager implements AuthenticationManager {
		@Override
		public boolean userIsAdmin() {
			return false;
		}

		@Override
		public boolean userIsAsserted() {
			return false;
		}

		@Override
		public boolean userIsAuthenticated() {
			return false;
		}

		@Override
		public String getUserName() {
			return "Test";
		}
	}
}
