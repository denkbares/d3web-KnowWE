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

package de.knowwe.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.SectionStore;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;

public class KnowWEUtils {

	public static void appendToFile(String path, String entry) {

		try {
			FileWriter fstream = new FileWriter(path, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(entry);
			out.close();
		}
		catch (Exception e) {
			Logger.getLogger(KnowWEUtils.class.getName()).log(
					Level.WARNING, "Unable to append to File: " + e.getMessage());
		}
	}

	public static String convertUmlaut(String text) {
		if (text == null) return null;
		String result = text;
		result = result.replaceAll("Ä", "&Auml;");
		result = result.replaceAll("Ö", "&Ouml;");
		result = result.replaceAll("Ü", "&Uuml;");
		result = result.replaceAll("ä", "&auml;");
		result = result.replaceAll("ö", "&ouml;");
		result = result.replaceAll("ü", "&uuml;");
		result = result.replaceAll("ß", "&szlig;");
		return result;
	}

	/**
	 * Escapes the given string for safely using user-input in web sites.
	 * 
	 * @param text Text to escape
	 * @return Sanitized text
	 */
	public static String escapeHTML(String text) {
		if (text == null) return null;

		return text.replaceAll("&", "&amp;").
				replaceAll("\"", "&quot;").
				replaceAll("'", "&#x27;").
				replaceAll("<", "&lt;").
				replaceAll(">", "&gt;");
	}

	/**
	 * Creates a unique anchor name for the section to link to. See method
	 * {@link #getWikiLink(Section)} for more details on how to use this method.
	 * 
	 * @param section the section to create the anchor for.
	 * @return the unique anchor name
	 */
	public static String getAnchor(Section<?> section) {
		// TODO: figure out how JSPWiki builds section anchor names
		return "section-"
				+ section.getArticle().getTitle().replace(' ', '+') + "-"
				+ Math.abs(section.getID().hashCode());
	}

	/**
	 * Returns the ConnectorAttachment for a specified filename on a specified
	 * wikipage
	 * 
	 * @created 27.01.2012
	 * @param title Title of the wikipage
	 * @param fileName filename of the attachment
	 * @return {@link WikiAttachment} fulfilling the specified parameters or
	 *         null, if no such attachment exists
	 */
	public static WikiAttachment getAttachment(String title, String fileName) throws IOException {
		Collection<WikiAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
		WikiAttachment actualAttachment = null;
		for (WikiAttachment attachment : attachments) {
			if ((attachment.getFileName().equals(fileName)
					&& attachment.getParentName().equals(title))
					|| attachment.getPath().equals(fileName)) {
				actualAttachment = attachment;
				break;
			}
		}
		return actualAttachment;
	}

	/**
	 * Returns all {@link WikiAttachment}s which full name fits to the regex or
	 * which filename matches to the regexp and which parent has the specified
	 * topic
	 * 
	 * @created 09.02.2012
	 * @param regex regular expression the attachments should match to
	 * @param topic Topic of the article
	 * @return Collection of {@link WikiAttachment}s
	 */
	public static Collection<WikiAttachment> getAttachments(String regex, String topic) throws IOException {
		Collection<WikiAttachment> result = new LinkedList<WikiAttachment>();
		Collection<WikiAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
		Pattern pattern = Pattern.compile(regex);
		for (WikiAttachment attachment : attachments) {
			if (pattern.matcher(attachment.getPath()).matches()
					|| (pattern.matcher(attachment.getFileName()).matches() && attachment.getParentName().equals(
							topic))) {
				result.add(attachment);
			}
		}
		return result;
	}

	/**
	 * Returns all master articles that compile the given Section. If no master
	 * article compiles the Section, at least the article of the Section itself
	 * is returned, so the Collection always at least contains one article.
	 * 
	 * @created 16.02.2012
	 * @param section is the Section for which you want to know the compiling
	 *        articles
	 * @return a non empty Collection of articles that compile the given Section
	 */
	public static Collection<Article> getCompilingArticles(Section<?> section) {
		Collection<Article> articles = getCompilingArticleObjects(section);
		if (articles.isEmpty()) articles.add(section.getArticle());
		return articles;
	}

	public static Collection<Article> getCompilingArticleObjects(Section<?> section) {
		Collection<Article> articles = new ArrayList<Article>();
		Environment env = Environment.getInstance();
		Set<String> referingArticleTitles = env.getPackageManager(section.getWeb()).getCompilingArticles(
				section);
		ArticleManager articleManager = env.getArticleManager(section.getWeb());
		for (String title : referingArticleTitles) {
			Article article =
					Article.getCurrentlyBuildingArticle(section.getWeb(), title);
			if (article == null) article = articleManager.getArticle(title);
			if (article == null) continue;
			articles.add(article);
		}
		return articles;
	}

	public static ResourceBundle getConfigBundle() {
		return ResourceBundle.getBundle("KnowWE_config");
	}

	public static String getErrorQ404(String question, String text) {
		String rendering = "<span class=\"semLink\"><a href=\"#\" title=\""
				+ "Question not found:"
				+ question
				+ "\" >"
				+ text
				+ "</a></span>";
		return rendering;

	}

	/**
	 * @return the {@link TerminologyManager} that handles global terms for this
	 *         web (similar to the former {@link TermRegistrationScope#GLOBAL}).
	 */
	public static TerminologyManager getGlobalTerminologyManager(String web) {
		return Environment.getInstance().getTerminologyManager(web, null);
	}

	// public static String repairUmlauts(String s) {
	// // then replace special characters
	// s = s.replaceAll("&szlig;","ß");
	// s = s.replaceAll("&auml;","ä");
	// s = s.replaceAll("&uuml;","ü");
	// s = s.replaceAll("&ouml;","ö");
	// s = s.replaceAll("&Auml;","Ä");
	// s = s.replaceAll("&Uuml;","Ü");
	// s = s.replaceAll("&Ouml;","Ö");
	// s = s.replaceAll("&deg;","°");
	// s = s.replaceAll("&micro;","µ");
	// s = s.replaceAll("&apos;", "'");
	// return(s);
	// }

	public static String getKnowWEExtensionPath() {
		return Environment.getInstance().getWikiConnector().getKnowWEExtensionPath();
	}

	public static String getApplicationRootPath() {
		return Environment.getInstance().getWikiConnector().getApplicationRootPath();
	}

	public static String getPageChangeLogPath() {
		return getVersionsSavePath() + "PageChangeLog.txt";
	}

	public static String getRealPath(String varPath) {
		if (varPath.contains("$root_path$")) {
			String rootPath = Environment.getInstance().getWikiConnector()
					.getApplicationRootPath();
			rootPath = rootPath.replace('\\', '/');
			rootPath = rootPath.replaceAll("/+$", "");
			varPath = varPath.replace("$root_path$", rootPath);
		}
		return varPath;
	}

	public static Object getStoredObject(Article article, Section<?> s, String key) {
		return s.getSectionStore().getObject(article, key);
	}

	public static Object getStoredObject(Section<?> s, String key) {
		return getStoredObject(null, s, key);
	}

	/**
	 * Returns the term identifier if the given Section has the type SimpleTerm,
	 * the text of the Section else.
	 * 
	 * @created 06.05.2012
	 * @param termSection the Section which should implement the interface
	 *        SimpleTerm
	 */
	public static String getTermName(Section<?> termSection) {
		if (termSection.get() instanceof SimpleTerm) {
			Section<? extends SimpleTerm> simpleSection = Sections.cast(termSection,
					SimpleTerm.class);
			return simpleSection.get().getTermName(simpleSection);
		}
		else {
			return Strings.trimQuotes(termSection.getText());
		}
	}

	/**
	 * Returns the term identifier if the given Section has the type SimpleTerm,
	 * the text of the Section else.
	 * 
	 * @created 08.02.2012
	 * @param termSection the Section which should implement the interface
	 *        SimpleTerm
	 */
	public static TermIdentifier getTermIdentifier(Section<?> termSection) {
		if (termSection.get() instanceof SimpleTerm) {
			Section<? extends SimpleTerm> simpleSection = Sections.cast(termSection,
					SimpleTerm.class);
			return simpleSection.get().getTermIdentifier(simpleSection);
		}
		else {
			return new TermIdentifier(
					Strings.trimQuotes(termSection.getText()));
		}
	}

	/**
	 * @return the {@link TerminologyManager} for the given (master) article.
	 */
	public static TerminologyManager getTerminologyManager(Article article) {
		String web = article == null ? Environment.DEFAULT_WEB : article.getWeb();
		String title = article == null ? null : article.getTitle();
		return Environment.getInstance().getTerminologyManager(web, title);
	}

	public static PackageManager getPackageManager(String web) {
		return Environment.getInstance().getPackageManager(web);
	}

	public static TerminologyManager getTerminologyManager(Article article, TermRegistrationScope scope) {
		TerminologyManager tHandler;
		if (scope == TermRegistrationScope.GLOBAL) {
			tHandler = getGlobalTerminologyManager(article.getWeb());
		}
		else {
			tHandler = getTerminologyManager(article);
		}
		return tHandler;
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article
	 * including the HTML-anchor tag.
	 * 
	 * @param title the article title to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLinkHTMLToArticle(String title) {
		return "<a href='" + getURLLink(title) + "' >" + title + "</a>";
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
	 * 
	 * @param title the article title to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(String title) {
		return getURLLink(title, -1);
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article in
	 * the specified version.
	 * 
	 * @param title the article title to create the link for
	 * @param version the article version to link to
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(String title, int version) {
		return "Wiki.jsp?page=" + Strings.encodeURL(title)
				+ (version != -1 ? "&version=" + version : "");
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
	 * 
	 * @param article the article to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(Article article) {
		return getURLLink(article.getTitle());
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to this section. The created
	 * link navigates the user to the article of the section. If the section is
	 * rendered with an anchor (see method {@link #getAnchor(Section)}) the page
	 * is also scrolled to the section.
	 * 
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Article)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(Section<?> section) {
		return "Wiki.jsp?page=" + section.getTitle() + "#" + getAnchor(section);
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the diff of the specified
	 * article. Usually, version1 is the newer version and version2 the older
	 * one.
	 * 
	 * @param title the article title to create the link for
	 * @param version1 the version containing the changes
	 * @param version2 the base version
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getDiffURLLink(String title, int version1, int version2) {
		return "Diff.jsp?page=" + title + "&r1=" + version1 + "&r2=" + version2;
	}

	/**
	 * Returns the String stored as a cookies with the given name from the
	 * UserContext. If no cookie exists, null is returned.
	 * 
	 * @created 11.02.2013
	 * @param name the name of the cookie
	 * @param context the user context
	 * @return the stored cookie or null, if no cookie with the given name
	 *         exists
	 */
	public static String getCookie(String name, UserContext context) {
		HttpServletRequest request = context.getRequest();
		if (request == null) return null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Convenience method to get the value of the cookie. This method does the
	 * same as the Method {@link KnowWEUtils#getCookie(String, UserContext)},
	 * but never returns null, except null is given as the default value. The
	 * default value will not be set as the cookie, but returned, if no cookie
	 * is given yet (for example with new users).
	 * 
	 * @created 11.02.2013
	 * @param name the name of the cookie
	 * @param defaultValue the value to be returned, if the cookie could not be
	 *        found
	 * @param context the user context
	 * @return the stored cookie or null, if no cookie with the given name
	 *         exists
	 */
	public static String getCookie(String name, String defaultValue, UserContext context) {
		String cookie = getCookie(name, context);
		if (cookie == null) cookie = defaultValue;
		return cookie;
	}

	public static String getVersionsSavePath() {
		String path = Environment.getInstance().getWikiConnector().getSavePath();
		if (path != null && !path.endsWith(File.pathSeparator)) path += File.separator;
		path += "OLD/";
		return path;
	}

	/**
	 * Creates a wiki-markup-styled link to this section. The created link
	 * navigates the user to the article of the section. If the section is
	 * rendered with an anchor (see method {@link #getAnchor(Section)}) the page
	 * is also scrolled to the section.
	 * <p>
	 * Please not that the link will only work if it is put into "[" ... "]"
	 * brackets and rendered through the wiki rendering pipeline.
	 * 
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getURLLink(Article)
	 */
	public static String getWikiLink(Section<?> section) {
		return section.getTitle() + "#" + Math.abs(section.getID().hashCode());
	}

	/**
	 * Do not use this method anymore, use
	 * {@link SectionStore#storeObject(String, Object)} or
	 * {@link SectionStore#storeObject(Article, String, Object)} instead. Use
	 * {@link Section#getSectionStore()} to get the right {@link SectionStore}.
	 * 
	 * @created 08.07.2011
	 * @param article is the article you want to store the Object for... if the
	 *        Object is relevant for all articles, you can set the argument to
	 *        null
	 * @param s is the {@link Section} you want to store the object for
	 * @param key is key used to store and retrieve the Object
	 * @param o is the Object to store
	 */
	public static void storeObject(Article article, Section<?> s, String key, Object o) {
		s.getSectionStore().storeObject(article, key, o);
	}

	public static String readFile(String fileName) {
		try {
			return readFile(new FileInputStream(fileName));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String readFile(InputStream inputStream) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));
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

	public static void writeFile(String path, String content) {

		try {
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		}
		catch (Exception e) {
			Logger.getLogger(KnowWEUtils.class.getName()).log(
					Level.WARNING, "Unable to write File: " + e.getMessage());
		}
	}

}
