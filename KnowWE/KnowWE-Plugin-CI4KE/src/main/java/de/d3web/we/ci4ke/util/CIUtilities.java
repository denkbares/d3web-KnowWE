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

package de.d3web.we.ci4ke.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.testing.CITest;
import de.d3web.we.ci4ke.testing.DynamicCITestManager;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CIUtilities {

	/**
	 * Returns the savepath for ci-builds. If the path does not exist, it will
	 * instantly be created!
	 * 
	 * @return the save path for ci-build xml´s
	 */
	public static File getCIBuildDir() {
		String wikiDir = getWikiContentDirectory();
		File buildDir = new File(wikiDir + "ci-builds");
		// check if the path exists
		if (!buildDir.exists()) {
			buildDir.mkdirs();
		}
		return buildDir;
	}

	/**
	 * 
	 * @created 10.11.2010
	 * @return
	 */
	private static String getWikiContentDirectory() {
		KnowWEWikiConnector con = KnowWEEnvironment.getInstance().getWikiConnector();
		String wikiDir = con.getSavePath();
		if (wikiDir == null || wikiDir.isEmpty()) {
			Logger.getLogger(CIUtilities.class.getName()).log(
					Level.WARNING, "Wiki SavePath could not be retrieved! (null or empty!)");
		}

		if (!wikiDir.endsWith(File.separator)) {
			wikiDir = wikiDir + File.separator;
		}
		return wikiDir;
	}

	/**
	 * Returns the attachment-directory for a specific article.
	 * 
	 * @created 10.11.2010
	 * @param articleTitle
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static File getAttachmentsDirectory(String articleTitle) throws UnsupportedEncodingException {
		String wikiDir = getWikiContentDirectory();
		String folderName = URLEncoder.encode(articleTitle, "UTF-8") + "-att" + File.separator;
		File attachmentsDir = new File(wikiDir + folderName);
		if (!attachmentsDir.exists()) {
			attachmentsDir.mkdirs();
		}
		return attachmentsDir;
	}

	/**
	 * This method finds a CIDashboard section only by its dashboard ID, by
	 * iterating over all wiki articles.
	 * 
	 * @param dashboardName the dashboard ID to look for
	 * @return the section where the dashboard with the given ID is defined, or
	 *         null if no section with this ID can be found
	 */
	public static Section<CIDashboardType> findCIDashboardSection(String dashboardName) {
		for (KnowWEArticle article : KnowWEEnvironment.getInstance().
				getArticleManager(KnowWEEnvironment.DEFAULT_WEB).getArticles()) {

			List<Section<CIDashboardType>> list = new ArrayList<Section<CIDashboardType>>();

			Sections.findSuccessorsOfType(article.getSection(), CIDashboardType.class, list);

			for (Section<CIDashboardType> sec : list) {
				if (CIDashboardType.getAnnotation(sec, CIDashboardType.NAME_KEY).equals(
						dashboardName)) {
					return sec;
				}
			}
		}
		return null;
	}

	/**
	 * This method finds a CIDashboard section by its dashboard ID, searching on
	 * the article with the given title.
	 * 
	 * @param dashboardArticleTitle
	 * @param dashboardName
	 * @return
	 */
	public static Section<CIDashboardType> findCIDashboardSection(
			String dashboardArticleTitle, String dashboardName) {
		// get the article
		KnowWEArticle article = KnowWEEnvironment.getInstance().
				getArticleManager(KnowWEEnvironment.DEFAULT_WEB).
				getArticle(dashboardArticleTitle);
		// get all CIDashboardType-sections on this article
		List<Section<CIDashboardType>> list = new ArrayList<Section<CIDashboardType>>();
		Sections.findSuccessorsOfType(article.getSection(), CIDashboardType.class, list);
		// iterate all sections and look for the given dashboard ID
		for (Section<CIDashboardType> sec : list) {
			if (CIDashboardType.getAnnotation(sec, CIDashboardType.NAME_KEY).equals(dashboardName)) {
				return sec;
			}
		}
		return null;
	}

	/**
	 * TODO: Javadoc
	 * 
	 * @param testClassNames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static Map<String, Class<? extends CITest>> parseTestClasses(Collection<String> testClassNames) {

		Map<String, Class<? extends CITest>> classesMap =
				new TreeMap<String, Class<? extends CITest>>();

		List<Extension> allTests = Arrays.asList(PluginManager.getInstance().
				getExtensions("KnowWEExtensionPoints", "CITest"));

		for (String testClassName : testClassNames) {
			boolean javaClassFound = false;
			for (Extension e : allTests) {
				if (testClassName.equals(e.getName())) {
					try {
						Class<?> clazz = Class.forName(e.getParameter("class"));
						if (CITest.class.isAssignableFrom(clazz)) {
							classesMap.put(testClassName, (Class<? extends CITest>) clazz);
							javaClassFound = true;
						}
					}
					catch (ClassNotFoundException e1) {
					}
				}
			}
			// if no corresponding Java CITest Class was found, try to search
			// for dynamically implemented CITests!
			if (!javaClassFound) {
				Class<? extends CITest> testClazz = DynamicCITestManager.
						INSTANCE.getCITestClass(testClassName);
				if (testClazz != null) {
					classesMap.put(testClassName, testClazz);
				}
			}
		}
		return classesMap;
		// the test class names are separated by colons... lets split() them!
		// List<String> list = Arrays.asList(testClassNames.split(":"));

		// return parseTestClasses(list);
	}

	public static Map<String, Class<? extends CITest>> getAllCITestClasses() {
		Map<String, Class<? extends CITest>> classesMap =
				new TreeMap<String, Class<? extends CITest>>();

		List<Extension> allPluggedTests = Arrays.asList(PluginManager.getInstance().
				getExtensions("KnowWEExtensionPoints", "CITest"));

		for (Extension e : allPluggedTests) {
			String testClassName = e.getName();
			try {
				Class<?> clazz = Class.forName(e.getParameter("class"));
				if (CITest.class.isAssignableFrom(clazz)) {
					classesMap.put(testClassName, clazz.asSubclass(CITest.class));
				}
			}
			catch (ClassNotFoundException e1) {
			}
		}

		// add all dynamically registered CITests:
		classesMap.putAll(DynamicCITestManager.INSTANCE.getAllDynamicCITestClasses());

		return Collections.unmodifiableMap(classesMap);
	}

	// RENDER - HELPERS

	public static String renderResultType(TestResultType resultType, int pixelSize) {

		String imgBulb = "<img src='KnowWEExtension/ci4ke/images/" +
				pixelSize + "x" + pixelSize
				+ "/%s.png' alt='%<s' align='absmiddle' title='%s'>";

		switch (resultType) {
		case SUCCESSFUL:
			imgBulb = String.format(imgBulb, "green", "Build successful!");
		case FAILED:
			imgBulb = String.format(imgBulb, "red", "Build failed!");
		case ERROR:
			imgBulb = String.format(imgBulb, "grey", "Build has errors!");
		}

		return imgBulb;
	}

	public static String renderForecastIcon(int score, int lastBuilds, int lastFailedBuilds, int pixelSize) {

		String imgForecast = "<img src='KnowWEExtension/ci4ke/images/" +
				pixelSize + "x" + pixelSize + "/%s.png' align='absmiddle' alt='%<s' title='%s'>";

		if (score == 0) {
			imgForecast = String.format(imgForecast, "health-00to19",
					"All recent builds failed.");
		}
		else if (score <= 20) {
			imgForecast = String.format(imgForecast, "health-00to19",
					lastFailedBuilds + " out of the last " + lastBuilds + " builds failed.");
		}
		else if (score <= 40) {
			imgForecast = String.format(imgForecast, "health-20to39", lastFailedBuilds
					+ " out of the last " + lastBuilds + " builds failed.");
		}
		else if (score <= 60) {
			imgForecast = String.format(imgForecast, "health-40to59", lastFailedBuilds
					+ " out of the last " + lastBuilds + " builds failed.");
		}
		else if (score <= 80) {
			imgForecast = String.format(imgForecast, "health-60to79", lastFailedBuilds
					+ " out of the last " + lastBuilds + " builds failed.");
		}
		else if (score < 100) {
			imgForecast = String.format(imgForecast, "health-80plus",
					lastFailedBuilds + " out of the last " + lastBuilds + " builds failed.");
		}
		else if (score == 100) {
			imgForecast = String.format(imgForecast, "health-80plus",
					"No recent builds failed.");
		}
		else {
			imgForecast = "";
		}

		return imgForecast;
	}

	/**
	 * Replaces the relevant entities inside the String. All &amp; &gt;, &lt;,
	 * and &quot; are replaced by their respective names.
	 * 
	 * @param src The source string.
	 * @return The encoded string.
	 */
	public static String replaceEntities(String src) {
		src = replaceString(src, "&", "&amp;");
		src = replaceString(src, "<", "&lt;");
		src = replaceString(src, ">", "&gt;");
		src = replaceString(src, "\"", "&quot;");

		return src;
	}

	/**
	 * Replaces a string with an other string.
	 * 
	 * @param orig Original string. Null is safe.
	 * @param src The string to find.
	 * @param dest The string to replace <I>src</I> with.
	 * @return A string with the replacement done.
	 */
	public static final String replaceString(String orig, String src, String dest) {
		if (orig == null) {
			return null;
		}
		if (src == null || dest == null) {
			throw new NullPointerException();
		}
		if (src.length() == 0) {
			return orig;
		}

		StringBuffer res = new StringBuffer(orig.length() + 20); // Pure
		// guesswork
		int start = 0;
		int end = 0;
		int last = 0;

		while ((start = orig.indexOf(src, end)) != -1) {
			res.append(orig.substring(last, start));
			res.append(dest);
			end = start + src.length();
			last = start + src.length();
		}

		res.append(orig.substring(end));

		return res.toString();
	}

	/**
	 * Escapes a string using UTF-8
	 * 
	 * @created 01.12.2010
	 * @param toEscape
	 * @return
	 */
	public static String utf8Escape(String toEscape) {
		try {
			return URLEncoder.encode(toEscape, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return toEscape;
		}
	}
}
