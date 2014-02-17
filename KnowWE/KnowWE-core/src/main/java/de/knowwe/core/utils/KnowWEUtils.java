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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class KnowWEUtils {

	private static void mask(StringBuilder buffer, String toReplace) {
		int index = buffer.indexOf(toReplace);
		while (index >= 0) {
			// string starts with substring which should be replaced
			// or the char before the substring is not ~
			if (index == 0 || !buffer.substring(index - 1, index).equals("~")) {
				buffer.replace(index, index + toReplace.length(), "~" + toReplace);
			}
			index = buffer.indexOf(toReplace, index + 1);
		}
	}

	private static void unmask(StringBuilder buffer, String toReplace) {
		int index = buffer.indexOf(toReplace);
		while (index >= 0) {
			// string does not start with substring which should be replaced
			// or the char before the substring is ~
			if (index != 0 || buffer.substring(index - 1, index).equals("~")) {
				buffer.replace(index - 1, index + toReplace.length(), toReplace);
			}
			index = buffer.indexOf(toReplace, index + 1);
		}
	}

	/**
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not
	 * interpret them, if the characters are already escaped, it will do nothing
	 * 
	 * @created 03.03.2011
	 */
	public static String maskJSPWikiMarkup(String string) {
		StringBuilder temp = new StringBuilder(string);
		maskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Unmasks [, ], ----, {{{, }}} and %% so that tests of error messages run
	 * properly.
	 * 
	 * @created 28.09.2012
	 * @param builder
	 */
	public static String unmaskJSPWikiMarkup(String string) {
		StringBuilder temp = new StringBuilder(string);
		unmaskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not
	 * interpret them, if the characters are already escaped, it will do nothing
	 * 
	 * @created 03.03.2011
	 * @param builder
	 */
	public static void maskJSPWikiMarkup(StringBuilder builder) {
		mask(builder, "[");
		mask(builder, "]");
		mask(builder, "----");
		mask(builder, "{{{");
		mask(builder, "}}}");
		mask(builder, "%%");
		mask(builder, "\\");
		mask(builder, "__");
	}

	/**
	 * Unmasks [, ], ----, {{{, }}} and %% so that tests of error messages run
	 * properly.
	 * 
	 * @created 28.09.2012
	 * @param builder
	 */
	public static void unmaskJSPWikiMarkup(StringBuilder builder) {
		unmask(builder, "[");
		unmask(builder, "]");
		unmask(builder, "----");
		unmask(builder, "{{{");
		unmask(builder, "}}}");
		unmask(builder, "%%");
		unmask(builder, "\\");
	}

	public static void appendToFile(String path, String entry) {

		try {
			FileWriter fstream = new FileWriter(path, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(entry);
			out.close();
		}
		catch (Exception e) {
			Log.warning("Unable to append to File", e);
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
	 * Creates a unique anchor name for the section to link to. See method
	 * {@link #getWikiLink(Section)} for more details on how to use this method.
	 * 
	 * @param section the section to create the anchor for.
	 * @return the unique anchor name
	 */
	public static String getAnchor(Section<?> section) {
		// TODO: figure out how JSPWiki builds section anchor names
		String anchor = "section-"
				+ section.getArticle().getTitle().replace(' ', '+') + "-"
				+ Math.abs(section.getID().hashCode());
		return anchor;
	}

	/**
	 * Renders a unique anchor name for the section to link to. See method
	 * {@link #getWikiLink(Section)} and {@link #getURLLink(Section)} for more
	 * details on how to use this method.
	 * 
	 * @created 16.08.2013
	 * @param section the section to create the anchor for
	 * @param result the output target to be wriotten to
	 */
	public static void renderAnchor(Section<?> section, RenderResult result) {
		String anchorName = KnowWEUtils.getAnchor(section);
		result.appendHtml("<a name='" + anchorName + "'></a>");
	}

	/**
	 * Returns the ConnectorAttachment for a specified filename on a specified
	 * article. Either the title matches the article and the fileName matches
	 * the filename of the attachment or the filename matches the complete path
	 * for the attachment.
	 * 
	 * @created 27.01.2012
	 * @param title the title of the article of the attachment
	 * @param fileName the filename of the attachment
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
	 * title
	 * 
	 * @created 09.02.2012
	 * @param title the title of the article
	 * @param regex regular expression the attachments should match to
	 * @return Collection of {@link WikiAttachment}s
	 */
	public static Collection<WikiAttachment> getAttachments(String title, String regex) throws IOException {
		Collection<WikiAttachment> result = new LinkedList<WikiAttachment>();
		Collection<WikiAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
		Pattern pattern;
		try {
			pattern = Pattern.compile(regex);
		}
		catch (PatternSyntaxException e) {
			pattern = Pattern.compile(Pattern.quote(regex));
		}
		for (WikiAttachment attachment : attachments) {
			// we return the attachment if:
			// the regex argument directly equals the path
			if (!regex.equals(attachment.getPath())) {
				// or the pattern matches the path
				if (!pattern.matcher(attachment.getPath()).matches()) {
					// or we have the correct title and
					if (!attachment.getParentName().equals(title)) continue;
					// the regex either directly equals the file name
					if (!regex.equals(attachment.getFileName())) {
						// or the pattern matches the filename
						if (!pattern.matcher(attachment.getFileName()).matches()) continue;
					}
				}
			}
			result.add(attachment);
		}
		return result;
	}

	public static ResourceBundle getConfigBundle() {
		return ResourceBundle.getBundle("KnowWE_config");
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

	/**
	 * Returns if the user has the read access rights to the specified article.
	 * 
	 * @created 29.11.2013
	 * @param article the article to check the access rights for
	 * @param user the user context
	 * @return true if the user has the read access rights to the article
	 */
	public static boolean canView(Article article, UserContext user) {
		return Environment.getInstance().getWikiConnector().userCanViewArticle(
				article.getTitle(), user.getRequest());
	}

	/**
	 * Returns if the user has the read access rights to all of the specified
	 * articles.
	 * 
	 * @created 29.11.2013
	 * @param articles the articles to check the access rights for
	 * @param user the user context
	 * @return true if the user has the read access rights to all of the
	 *         articles
	 */
	private static boolean canView(Set<Article> articles, UserContext user) {
		for (Article article : articles) {
			if (!canView(article, user)) return false;
		}
		return true;
	}

	/**
	 * Returns if the user has the read access rights to the specified section.
	 * To be more specific, it checks if the user has the read access rights to
	 * the article that contains the specified section.
	 * 
	 * @created 29.11.2013
	 * @param section the section to check the access rights for
	 * @param user the user context
	 * @return true if the user has the read access rights to the section
	 */
	public static boolean canView(Section<?> section, UserContext user) {
		return canView(section.getArticle(), user);
	}

	/**
	 * Returns if the user has the read access rights to all of the specified
	 * sections. To be more specific, it checks if the user has the read access
	 * rights to the set of all articles that contain the specified sections.
	 * 
	 * @created 29.11.2013
	 * @param sections the sections to check the access rights for
	 * @param user the user context
	 * @return true if the user has the read access rights to all of the
	 *         sections
	 */
	public static boolean canView(Collection<Section<?>> sections, UserContext user) {
		return canView(Sections.getArticles(sections), user);
	}

	/**
	 * Returns if the user has the write access rights to the specified article.
	 * 
	 * @created 29.11.2013
	 * @param article the article to check the access rights for
	 * @param user the user context
	 * @return true if the user has the write access rights to the article
	 */
	public static boolean canWrite(Article article, UserContext user) {
		return Environment.getInstance().getWikiConnector().userCanEditArticle(
				article.getTitle(), user.getRequest());
	}

	/**
	 * Returns if the user has the write access rights to all of the specified
	 * articles.
	 * 
	 * @created 29.11.2013
	 * @param articles the articles to check the access rights for
	 * @param user the user context
	 * @return true if the user has the write access rights to all of the
	 *         articles
	 */
	private static boolean canWrite(Set<Article> articles, UserContext user) {
		for (Article article : articles) {
			if (!canWrite(article, user)) return false;
		}
		return true;
	}

	/**
	 * Returns if the user has the write access rights to the specified section.
	 * To be more specific, it checks if the user has the write access rights to
	 * the article that contains the specified section.
	 * 
	 * @created 29.11.2013
	 * @param section the section to check the access rights for
	 * @param user the user context
	 * @return true if the user has the write access rights to the section
	 */
	public static boolean canWrite(Section<?> section, UserContext user) {
		return canWrite(section.getArticle(), user);
	}

	/**
	 * Returns if the user has the write access rights to all of the specified
	 * sections. To be more specific, it checks if the user has the write access
	 * rights to the set of all articles that contain the specified sections.
	 * 
	 * @created 29.11.2013
	 * @param sections the sections to check the access rights for
	 * @param user the user context
	 * @return true if the user has the write access rights to all of the
	 *         sections
	 */
	public static boolean canWrite(Collection<Section<?>> sections, UserContext user) {
		return canWrite(Sections.getArticles(sections), user);
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

	/**
	 * Returns the term identifier if the given Section has the type SimpleTerm,
	 * the text of the Section else.
	 * 
	 * @created 08.02.2012
	 * @param termSection the Section which should implement the interface
	 *        SimpleTerm
	 */
	public static Identifier getTermIdentifier(Section<?> termSection) {
		if (termSection.get() instanceof Term) {
			Section<? extends Term> simpleSection = Sections.cast(termSection,
					Term.class);
			return simpleSection.get().getTermIdentifier(simpleSection);
		}
		else {
			return new Identifier(
					Strings.trimQuotes(termSection.getText()));
		}
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
		if (termSection.get() instanceof Term) {
			Section<? extends Term> simpleSection = Sections.cast(termSection,
					Term.class);
			return simpleSection.get().getTermName(simpleSection);
		}
		else {
			return Strings.trimQuotes(termSection.getText());
		}
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
	public static String getLinkHTMLToArticle(String title) {
		return getLinkHTMLToArticle(title, title);
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article
	 * including the HTML-anchor tag.
	 * 
	 * @param title the article title to create the link for
	 * @param linkText the text for the link
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getLinkHTMLToArticle(String title, String linkText) {
		return "<a href='" + getURLLink(title) + "' >" + linkText + "</a>";
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
	 * Creates a &lt;a href="..."&gt; styled link to the specified attachment of
	 * the specified article.
	 * 
	 * @param article the article to create the link for
	 * @param attachment the attachment to create the link for
	 * @return the created link
	 */
	public static String getURLLink(Article article, String attachment) {
		return "attach/" + article.getTitle() + "/" + attachment;
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
		return "Wiki.jsp?page=" + Strings.encodeURL(section.getTitle()) + "#" + getAnchor(section);
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

	public static String getURLLinkToTermDefinition(ArticleManager manager, Identifier identifier) {
		String url = null;
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(manager);
		for (TerminologyManager terminologyManager : terminologyManagers) {
			Collection<Section<?>> termDefiningSections = terminologyManager.getTermDefiningSections(identifier);
			if (termDefiningSections.size() > 1) break;
			if (termDefiningSections.size() == 1) {
				if (url == null) {
					url = KnowWEUtils.getURLLink(termDefiningSections.iterator().next());
				}
				else {
					url = null;
					break;
				}
			}
		}
		if (url == null) url = getURLLinkToObjectInfoPage(identifier);
		return url;
	}

	public static String getURLLinkToObjectInfoPage(Identifier identifier) {

		String objectName = Strings.encodeURL(new Identifier(
				identifier.getLastPathElement()).toExternalForm());
		String termIdentifier = Strings.encodeURL(identifier.toExternalForm());
		return "Wiki.jsp?page=ObjectInfoPage&amp;objectname="
				+ objectName + "&amp;termIdentifier="
				+ termIdentifier;
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

	public static String readFile(String fileName) {
		try {
			return Strings.readFile(fileName);

		}
		catch (IOException e) {
			Log.severe("Unable to read File", e);
			return "";
		}
	}

	public static String readStream(InputStream inputStream) {
		try {
			return Strings.readStream(inputStream);
		}
		catch (IOException e) {
			Log.severe("Unable to read stream", e);
			return "";
		}
	}

	public static void writeFile(String path, String content) {
		try {
			Strings.writeFile(path, content);
		}
		catch (Exception e) {
			Log.severe("Unable to write file", e);
		}
	}

	/**
	 * @deprecated use the method
	 *             {@link KnowWEUtils#getTerminologyManagers(Section)}
	 * @return the {@link TerminologyManager} for the given (master) article.
	 */
	@Deprecated
	public static TerminologyManager getTerminologyManager(Article article) {
		if (article == null) {
			return Compilers.getCompiler(KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB),
					DefaultGlobalCompiler.class).getTerminologyManager();
		}
		Section<PackageCompileType> compileSection = Sections.findSuccessor(
				article.getRootSection(), PackageCompileType.class);
		// to emulate old behavior (not return null) we return an empty
		// TerminologyManager
		if (compileSection == null) return new TerminologyManager();
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(compileSection);
		if (terminologyManagers.isEmpty()) return null;
		return terminologyManagers.iterator().next();
	}

	/**
	 * Returns the TerminologyManagers of the {@link TermCompiler}s compiling
	 * the give compile Section.
	 * 
	 * @created 15.11.2013
	 * @param section the section to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given section
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(Section<?> section) {
		Collection<TermCompiler> compilers = Compilers.getCompilers(section, TermCompiler.class);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager}.
	 * 
	 * @created 15.11.2013
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers
	 *        for
	 * @return the {@link TerminologyManager}s of the given manager
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager) {
		Collection<TermCompiler> compilers = Compilers.getCompilers(manager, TermCompiler.class);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager} that
	 * are from {@link Compiler} of the given compiler class.
	 * 
	 * @created 15.11.2013
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers
	 *        for
	 * @param compilerClass the type of the {@link Compiler}s we want the
	 *        {@link TerminologyManager}s from
	 * @return the {@link TerminologyManager}s of the given manager
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager, Class<? extends TermCompiler> compilerClass) {
		Collection<? extends TermCompiler> compilers = Compilers.getCompilers(manager,
				compilerClass);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	private static Collection<TerminologyManager> getTerminologyManagers(Collection<? extends TermCompiler> compilers) {
		Collection<TerminologyManager> managers = new ArrayList<TerminologyManager>(
				compilers.size());
		for (TermCompiler packageCompiler : compilers) {
			managers.add(packageCompiler.getTerminologyManager());
		}
		return managers;
	}

	/**
	 * Returns the article with the given web and title from the
	 * {@link DefaultArticleManager}.
	 * 
	 * @created 07.01.2014
	 */
	public static Article getArticle(String web, String title) {
		return Environment.getInstance().getArticle(web, title);
	}

	/**
	 * Returns the default {@link ArticleManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link ArticleManager} from
	 */
	public static ArticleManager getArticleManager(String web) {
		return Environment.getInstance().getArticleManager(web);
	}

	/**
	 * Returns the {@link PackageManager} of the given {@link ArticleManager}.
	 * 
	 * @created 07.01.2014
	 * @param manager the ArticleManager we want the {@link PackageManager} from
	 */
	public static PackageManager getPackageManager(ArticleManager manager) {
		Collection<PackageRegistrationCompiler> compilers = Compilers.getCompilers(manager,
				PackageRegistrationCompiler.class);
		if (compilers.isEmpty()) return null;
		return compilers.iterator().next().getPackageManager();
	}

	/**
	 * Returns the {@link PackageManager} for the given {@link Section}.
	 * 
	 * @created 07.01.2014
	 * @param section the {@link Section} we want the {@link PackageManager} for
	 */
	public static PackageManager getPackageManager(Section<?> section) {
		return getPackageManager(section.getArticleManager());
	}

	/**
	 * Returns the default {@link PackageManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link PackageManager} from
	 */
	public static PackageManager getPackageManager(String web) {
		return getPackageManager(getArticleManager(web));
	}

	public static void storeObject(Section<?> s, String key, Object o) {
		KnowWEUtils.storeObject(null, s, key, o);
	}

	public static void storeObject(Compiler compiler, Section<?> s, String key, Object o) {
		s.getSectionStore().storeObject(compiler, key, o);
	}

	public static Object getStoredObject(Section<?> s, String key) {
		return KnowWEUtils.getStoredObject(null, s, key);
	}

	public static Object getStoredObject(Compiler compiler, Section<?> s, String key) {
		return s.getSectionStore().getObject(compiler, key);
	}

	/**
	 * Returns the date the specified article has been modified the last time.
	 * This method also works if the specified article is not the latest one.
	 * Therefore this method does NOT (!) return the modification date of the
	 * specified article, but the modification date of the specified article's
	 * name within the corresponding article manager.
	 * 
	 * @created 16.02.2014
	 * @param article the article to get the latest modification date for
	 * @return the last modification date
	 */
	public static Date getLastModified(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();
		int version = connector.getVersion(title);
		return connector.getLastModifiedDate(title, version);
	}

	/**
	 * Returns the version number of the latest version of the specified
	 * article. This method also works if the specified article is not the
	 * latest instance. Therefore this method does NOT (!) return the version of
	 * the specified article, but the latest version of the specified article's
	 * name within the corresponding article manager.
	 * 
	 * @created 16.02.2014
	 * @param article the article to get the latest version for
	 * @return the latest version
	 */
	public static int getLatestVersion(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();
		return connector.getVersion(title);
	}

}
