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

import groovy.lang.GroovyShell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilerConfiguration;

import de.d3web.we.ci4ke.groovy.GroovyCITestScript;
import de.d3web.we.ci4ke.groovy.GroovyCITestSubtreeHandler;
import de.d3web.we.ci4ke.groovy.GroovyCITestType;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.handling.CITest;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CIUtilities {

	/**
	 * Returns the savepath for ci-builds. If the path does not exist, it will
	 * instantly be created!
	 * 
	 * @return the save path for ci-build xml´s
	 */
	public static File getCIBuildDir() {
		KnowWEWikiConnector con = KnowWEEnvironment.getInstance().getWikiConnector();
		String wikiDir = con.getSavePath();
		if (wikiDir == null || wikiDir.isEmpty()) {
			Logger.getLogger(CIUtilities.class.getName()).log(
					Level.WARNING, "Wiki SavePath could not be retrieved! (null or empty!)");
		}

		if (!wikiDir.endsWith(File.separator)) {
			wikiDir = wikiDir + File.separator;
		}
		File buildDir = new File(wikiDir + "ci-builds");
		// check if the path exists
		if (!buildDir.exists()) buildDir.mkdirs();
		return buildDir;
	}

	/**
	 * This method finds a CIDashboard section only by its dashboard ID, by
	 * iterating over all wiki articles.
	 * 
	 * @param dashboardID the dashboard ID to look for
	 * @return the section where the dashboard with the given ID is defined, or
	 *         null if no section with this ID can be found
	 */
	public static Section<CIDashboardType> findCIDashboardSection(String dashboardID) {
		for (KnowWEArticle article : KnowWEEnvironment.getInstance().
				getArticleManager(KnowWEEnvironment.DEFAULT_WEB).getArticles()) {

			List<Section<CIDashboardType>> list = new ArrayList<Section<CIDashboardType>>();

			article.getSection().findSuccessorsOfType(CIDashboardType.class, list);

			for (Section<CIDashboardType> sec : list) {
				if (CIDashboardType.getDashboardID(sec).equals(dashboardID)) return sec;
			}
		}
		return null;
	}

	/**
	 * This method finds a CIDashboard section by its dashboard ID, searching on
	 * the article with the given title.
	 * 
	 * @param dashboardArticleTitle
	 * @param dashboardID
	 * @return
	 */
	public static Section<CIDashboardType> findCIDashboardSection(
			String dashboardArticleTitle, String dashboardID) {
		// get the article
		KnowWEArticle article = KnowWEEnvironment.getInstance().
				getArticleManager(KnowWEEnvironment.DEFAULT_WEB).
				getArticle(dashboardArticleTitle);
		// get all CIDashboardType-sections on this article
		List<Section<CIDashboardType>> list = new ArrayList<Section<CIDashboardType>>();
		article.getSection().findSuccessorsOfType(CIDashboardType.class, list);
		// iterate all sections and look for the given dashboard ID
		for (Section<CIDashboardType> sec : list) {
			if (CIDashboardType.getDashboardID(sec).equals(dashboardID)) return sec;
		}
		return null;
	}

	/**
	 * TODO: Javadoc
	 * 
	 * @param testClassNames
	 * @return
	 */
	public static Map<String, Class<? extends CITest>> parseTestClasses(String testClassNames) {
		// the test class names are separeted by colons... lets split() them!
		List<String> list = Arrays.asList(testClassNames.split(":"));
		return parseTestClasses(list);
	}

	/**
	 * TODO: Javadoc
	 * 
	 * @param testClassNames
	 * @return
	 */
	public static Map<String, Class<? extends CITest>> parseTestClasses(
			Collection<String> testClassNames) {
		// our returnMap
		Map<String, Class<? extends CITest>> classesMap =
				new TreeMap<String, Class<? extends CITest>>();

		// get all Sections containing a GroovyCITest
		Map<String, Section<GroovyCITestType>> groovyTestSections =
				getAllGroovyCITestSections(KnowWEEnvironment.DEFAULT_WEB);

		// the package prefix to find the
		String packagePrefix = "de.d3web.we.ci4ke.testmodules.";

		for (String c : testClassNames) {

			// a test can either be statically defined in a java class
			// or dynamically defined in a grovvy test section
			if (groovyTestSections.containsKey(c)) {
				// the current class name matches the name of a (groovy) test
				// section
				Section<GroovyCITestType> sec = groovyTestSections.get(c);
				// parse the content of the section into a groovy-script
				Class<? extends CITest> testClass = parseGroovyCITestSection(sec);

				classesMap.put(c, testClass);

			}
			else {

				// c is a "ordinary" java class. Try to load the class!
				Class<?> clazz = null;
				try {
					clazz = Class.forName(packagePrefix + c);

					// If our new class implements the CITest-interface...
					if (CITest.class.isAssignableFrom(clazz)) {
						// this cast is legit due to the type-checking
						// beforehand
						@SuppressWarnings("unchecked")
						Class<? extends CITest> testClass =
								(Class<? extends CITest>) clazz;
						classesMap.put(c, testClass);
					}
				}
				catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();

				}
			}
		}
		return classesMap;
	}

	public static Class<? extends CITest> parseGroovyCITestSection(Section<GroovyCITestType> testSection) {

		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setScriptBaseClass(GroovyCITestScript.class.getName());
		GroovyShell shell = new GroovyShell(cc);

		String groovycode = GroovyCITestSubtreeHandler.PREPEND
				+ DefaultMarkupType.getContent(testSection);

		@SuppressWarnings("unchecked")
		Class<? extends CITest> clazz =
				(Class<? extends CITest>) shell.parse(groovycode).getClass();

		return clazz;

	}

	public static Map<String, Section<GroovyCITestType>> getAllGroovyCITestSections(String web) {
		// return map
		Map<String, Section<GroovyCITestType>> sectionsMap = new HashMap<String, Section<GroovyCITestType>>();
		// a collection containing all wiki-articles
		Collection<KnowWEArticle> allWikiArticles = KnowWEEnvironment.getInstance().
						getArticleManager(web).getArticles();
		// iterate over all articles
		for (KnowWEArticle article : allWikiArticles) {
			List<Section<GroovyCITestType>> sectionsList = new ArrayList<Section<GroovyCITestType>>();
			// find all GroovyCITestType sections on this article...
			article.getSection().findSuccessorsOfType(GroovyCITestType.class, sectionsList);
			// ...and add them to our Map
			for (Section<GroovyCITestType> section : sectionsList) {
				// a GroovyCITest is uniquely identified by its name-annotation
				String testName = DefaultMarkupType.getAnnotation(section, "name");
				sectionsMap.put(testName, section);
			}
		}
		return sectionsMap;
	}

	// RENDER - HELPERS

	public static String renderResultType(TestResultType resultType, int pixelSize) {

		String imgBulb = "<img src='KnowWEExtension/ci4ke/images/" +
				pixelSize + "x" + pixelSize + "/%s.png' alt='%<s' title='%s'>";

		switch (resultType) {
		case SUCCESSFUL:
			imgBulb = String.format(imgBulb, "green", "Current build succeeded!");
		case FAILED:
			imgBulb = String.format(imgBulb, "red", "Current build failed!");
		case ERROR:
			imgBulb = String.format(imgBulb, "grey", "Current build had errors!");
		}

		return imgBulb;
	}

	public static String renderForecastIcon(int score, int lastBuilds, int lastFailedBuilds) {

		String imgForecast = "<img src='KnowWEExtension/ci4ke/images/" +
				"22x22/%s.png' alt='%<s' title='%s'>";

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
		if (orig == null) return null;
		if (src == null || dest == null) throw new NullPointerException();
		if (src.length() == 0) return orig;

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
}
