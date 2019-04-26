/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.NotAuthorizedException;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiAttachmentInfo;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.core.wikiConnector.WikiObjectInfo;
import de.knowwe.core.wikiConnector.WikiPageInfo;
import de.knowwe.plugin.Plugins;
import de.knowwe.plugin.StatusProvider;

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
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not interpret them, if the characters are
	 * already escaped, it will do nothing
	 *
	 * @created 03.03.2011
	 */
	public static String maskJSPWikiMarkup(String string) {
		StringBuilder temp = new StringBuilder(string);
		maskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Masks JSPWiki links (like [ArticleName]), but only if the link is invalid. A link is invalid, if there is no
	 * article with the given name and the links is also no to an external site or an attachment. This method can be
	 * used to gracefully handle rendering of copy&pasted texts containing [ and ].
	 */
	public static String maskInvalidJSPWikiLinks(ArticleManager articleManager, String text) {
		Pattern linkFinder = Pattern.compile("\\[(?:[^\\]\\|]+\\|)?([^\\]]+)\\]");
		Matcher matcher = linkFinder.matcher(text);
		List<Pair<Integer, Integer>> escapeIndices = new ArrayList<>();
		while (matcher.find()) {
			String link = matcher.group(1);
			if (articleManager.getArticle(link.replaceAll("#.*$", "")) == null
					&& !link.startsWith("http")
					&& !link.startsWith("file")
					&& !link.startsWith("attach")) {
				escapeIndices.add(new Pair<>(matcher.start(), matcher.end()));
			}
		}
		StringBuilder builder = new StringBuilder(text);
		int shift = 0;
		for (Pair<Integer, Integer> indices : escapeIndices) {
			int start = indices.getA();
			int end = indices.getB();
			builder.insert(indices.getA() + shift++, "~");
			// also escape opening brackets after the first one, but before the closing one
			// if they are not escaped, JSPWiki will also make links fore these accidental link subsection
			char[] chars = new char[end - start - 1];
			builder.getChars(start + shift + 1, end + shift, chars, 0);
			for (int i = 0; i < chars.length; i++) {
				char aChar = chars[i];
				if (aChar == '[') {
					builder.insert(start + i + 1 + shift++, "~");
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Unmasks [, ], ----, {{{, }}} and %% so that tests of error messages run properly.
	 *
	 * @param string the string to be unmasked
	 * @created 28.09.2012
	 */
	public static String unmaskJSPWikiMarkup(String string) {
		StringBuilder temp = new StringBuilder(string);
		unmaskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not interpret them, if the characters are
	 * already escaped, it will do nothing
	 *
	 * @param builder the builder to destructively mask its contents
	 * @created 03.03.2011
	 */
	public static void maskJSPWikiMarkup(StringBuilder builder) {
		mask(builder, "[");
		mask(builder, "]");
		mask(builder, "----");
		mask(builder, "{{{");
		mask(builder, "}}}");
		mask(builder, "{{");
		mask(builder, "}}");
		mask(builder, "%%");
		mask(builder, "\\");
		mask(builder, "__");
	}

	/**
	 * Unmasks [, ], ----, {{{, }}} and %% so that tests of error messages run properly.
	 *
	 * @param builder the builder to destructively unmask its contents
	 * @created 28.09.2012
	 */
	public static void unmaskJSPWikiMarkup(StringBuilder builder) {
		unmask(builder, "[");
		unmask(builder, "]");
		unmask(builder, "----");
		unmask(builder, "{{{");
		unmask(builder, "}}}");
		unmask(builder, "{{");
		unmask(builder, "}}");
		unmask(builder, "%%");
		unmask(builder, "\\");
	}

	public static void appendToFile(String path, String entry) {

		try {
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(path, true),
					Charset.forName("UTF-8").newEncoder()
			);
			BufferedWriter out = new BufferedWriter(writer);
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
	 * Creates a unique anchor name for the section to link to. See method {@link #getWikiLink(Section)} for more
	 * details on how to use this method.
	 *
	 * @param section the section to create the anchor for.
	 * @return the unique anchor name
	 */
	public static String getAnchor(Section<?> section) {
		return "section-" + section.getID();
	}

	/**
	 * Renders a unique anchor name for the section to link to. See method {@link #getWikiLink(Section)} and {@link
	 * #getURLLink(Section)} for more details on how to use this method.
	 *
	 * @param section the section to create the anchor for
	 * @param result  the output target to be written to
	 * @created 16.08.2013
	 */
	public static void renderAnchor(Section<?> section, RenderResult result) {
		String anchorName = KnowWEUtils.getAnchor(section);
		result.appendHtml("<a class='anchor' name='" + anchorName + "'></a>");
	}

	/**
	 * Returns the ConnectorAttachment for a specified filename on a specified article. Either the title matches the
	 * article and the fileName matches the filename of the attachment or the filename matches the complete path for the
	 * attachment.
	 *
	 * @param title    the title of the article of the attachment
	 * @param fileName the filename of the attachment
	 * @return {@link WikiAttachment} fulfilling the specified parameters or null, if no such attachment exists
	 * @created 27.01.2012
	 */
	public static WikiAttachment getAttachment(String title, String fileName) throws IOException {
		WikiAttachment actualAttachment = Environment.getInstance()
				.getWikiConnector().getAttachment(title + "/" + fileName);
		if (actualAttachment != null) return actualAttachment;
		Collection<WikiAttachment> attachments = Environment.getInstance()
				.getWikiConnector()
				.getAttachments();
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
	 * Returns all {@link WikiAttachment}s which full name fits to the regex or which filename matches to the regexp and
	 * which parent has the specified title
	 *
	 * @param title the title of the article
	 * @param regex regular expression the attachments should match to
	 * @return Collection of {@link WikiAttachment}s
	 * @created 09.02.2012
	 */
	public static Collection<WikiAttachment> getAttachments(String title, String regex) throws IOException {
		Collection<WikiAttachment> result = new LinkedList<>();
		Collection<WikiAttachment> attachments = Environment.getInstance()
				.getWikiConnector().getAttachments();
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
	 * @param article the article to check the access rights for
	 * @param context the user context
	 * @return true if the user has the read access rights to the article
	 * @created 29.11.2013
	 */
	public static boolean canView(Article article, UserContext context) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		// try nine times with catching unexpected exception from AuthorizationManager
		for (int i = 0; i < 9; i++) {
			try {
				return connector.userCanViewArticle(article.getTitle(), context.getRequest());
			}
			catch (ConcurrentModificationException e) {
				// do nothing a few times, because we have no influence here
				Thread.yield();
			}
		}
		// finally, if not passed successfully,
		// try last time throwing the exception
		return connector.userCanViewArticle(article.getTitle(), context.getRequest());
	}

	/**
	 * Returns if the user has the read access rights to all of the specified articles.
	 *
	 * @param articles the articles to check the access rights for
	 * @param context  the user context
	 * @return true if the user has the read access rights to all of the articles
	 * @created 29.11.2013
	 */
	private static boolean canView(Set<Article> articles, UserContext context) {
		for (Article article : articles) {
			if (!canView(article, context)) return false;
		}
		return true;
	}

	/**
	 * Returns if the user has the read access rights to the specified section. To be more specific, it checks if the
	 * user has the read access rights to the article that contains the specified section.
	 *
	 * @param section the section to check the access rights for
	 * @param context the user context
	 * @return true if the user has the read access rights to the section
	 * @created 29.11.2013
	 */
	public static boolean canView(Section<?> section, UserContext context) {
		return canView(section.getArticle(), context);
	}

	/**
	 * Returns if the user has the read access rights to all of the specified sections. To be more specific, it checks
	 * if the user has the read access rights to the set of all articles that contain the specified sections.
	 *
	 * @param sections the sections to check the access rights for
	 * @param context  the user context
	 * @return true if the user has the read access rights to all of the sections
	 * @created 29.11.2013
	 */
	public static boolean canView(Collection<Section<?>> sections, UserContext context) {
		return canView(Sections.collectArticles(sections), context);
	}

	/**
	 * Checks whether the user has read access rights to the specified section. To be more specific, it checks if the
	 * user has the read access rights to the article that contains the specified section. If not, a {@link
	 * NotAuthorizedException} is thrown.
	 *
	 * @param section the section to check the access rights for
	 * @param context the user context
	 * @throws NotAuthorizedException is thrown if the user has no view rights to the article of the section
	 */
	public static void assertCanView(Section<?> section, UserContext context) throws NotAuthorizedException {
		if (!canView(section, context)) {
			throw new NotAuthorizedException("No view access for section '" + section.getID() + "'.");
		}
	}

	/**
	 * Returns if the user has the write access rights to the specified article.
	 *
	 * @param articleTitle the title of the article to check the access rights for
	 * @param user         the user context
	 * @return true if the user has the write access rights to the article
	 * @created 29.11.2013
	 */
	public static boolean canWrite(String articleTitle, UserContext user) {
		return Environment.getInstance().getWikiConnector().userCanEditArticle(
				articleTitle, user.getRequest());
	}

	/**
	 * Returns if the user has the write access rights to the specified article.
	 *
	 * @param article the article to check the access rights for
	 * @param user    the user context
	 * @return true if the user has the write access rights to the article
	 * @created 29.11.2013
	 */
	public static boolean canWrite(Article article, UserContext user) {
		return Environment.getInstance().getWikiConnector().userCanEditArticle(
				article.getTitle(), user.getRequest());
	}

	/**
	 * Returns if the user has the write access rights to all of the specified articles.
	 *
	 * @param articles the articles to check the access rights for
	 * @param user     the user context
	 * @return true if the user has the write access rights to all of the articles
	 * @created 29.11.2013
	 */
	private static boolean canWrite(Set<Article> articles, UserContext user) {
		for (Article article : articles) {
			if (!canWrite(article, user)) return false;
		}
		return true;
	}

	/**
	 * Returns if the user has the write access rights to the specified section. To be more specific, it checks if the
	 * user has the write access rights to the article that contains the specified section.
	 *
	 * @param section the section to check the access rights for
	 * @param user    the user context
	 * @return true if the user has the write access rights to the section
	 * @created 29.11.2013
	 */
	public static boolean canWrite(Section<?> section, UserContext user) {
		return canWrite(section.getArticle(), user);
	}

	/**
	 * Checks whether the user has write access rights to the specified section. To be more specific, it checks if the
	 * user has the write access rights to the article that contains the specified section. If not, a {@link
	 * NotAuthorizedException} is thrown.
	 *
	 * @param section the section to check the access rights for
	 * @param context the user context
	 * @throws NotAuthorizedException is thrown if the user has no view rights to the article of the section
	 */
	public static void assertCanWrite(Section<?> section, UserContext context) throws NotAuthorizedException {
		if (!canWrite(section, context)) {
			throw new NotAuthorizedException("No write access for section '" + section.getID() + "'.");
		}
	}

	/**
	 * Checks whether the user is in a given wiki group.
	 *
	 * @param groupName the group name to check for
	 * @param user      the user to check the group for
	 * @return true, if the user is member of the group, false else
	 */
	public static boolean isInGroup(String groupName, UserContext user) {
		return Environment.getInstance()
				.getWikiConnector()
				.userIsMemberOfGroup(groupName, user.getRequest());
	}

	/**
	 * Checks whether the given user is a wiki admin.
	 *
	 * @param user the user to check
	 * @return true, if the user is an admin, false else
	 */
	public static boolean isAdmin(UserContext user) {
		return user.userIsAdmin();
	}

	/**
	 * Returns if the user has the write access rights to all of the specified sections. To be more specific, it checks
	 * if the user has the write access rights to the set of all articles that contain the specified sections.
	 *
	 * @param sections the sections to check the access rights for
	 * @param user     the user context
	 * @return true if the user has the write access rights to all of the sections
	 * @created 29.11.2013
	 */
	public static boolean canWrite(Collection<Section<?>> sections, UserContext user) {
		return canWrite(Sections.collectArticles(sections), user);
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
	 * Returns the term identifier if the given Section has the type SimpleTerm, the text of the Section else.
	 *
	 * @param section the Section which should implement the interface SimpleTerm
	 * @created 08.02.2012
	 */
	public static Identifier getTermIdentifier(Section<?> section) {
		if (section.get() instanceof Term) {
			Section<Term> termSection = Sections.cast(section, Term.class);
			return termSection.get().getTermIdentifier(termSection);
		}
		else {
			return new Identifier(Strings.trimQuotes(section.getText()));
		}
	}

	/**
	 * Returns the term identifier if the given Section has the type SimpleTerm, the text of the Section else.
	 *
	 * @param termSection the Section which should implement the interface SimpleTerm
	 * @created 06.05.2012
	 */
	public static String getTermName(Section<?> termSection) {
		if (termSection.get() instanceof Term) {
			Section<Term> simpleSection = Sections.cast(termSection, Term.class);
			return simpleSection.get().getTermName(simpleSection);
		}
		else {
			return Strings.trimQuotes(termSection.getText());
		}
	}

	/**
	 * Will convert the given link to an absolute URL by pre-fixing the base-url configured in the wiki-properties under
	 * 'jspwiki.baseURL'.
	 *
	 * @param relativeLink The relative link to convert to an absolute link such as "Wiki.jsp?page=ABC", is allowed to
	 *                     either start with or without a leading slash
	 * @return an absolute URL such as "http://localhost:8080/KnowWE/Wiki.jsp?page=ABC"
	 */
	public static String getAsAbsoluteLink(String relativeLink) {
		String baseUrl = StringUtils.stripEnd(Environment.getInstance().getWikiConnector().getBaseUrl(), "/");
		relativeLink = StringUtils.stripStart(relativeLink, "/");
		return baseUrl + "/" + relativeLink;
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
	 *
	 * @param article the article title to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getLinkHTMLToArticle(Article article) {
		return getLinkHTMLToArticle(article.getTitle());
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
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
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
	 *
	 * @param title    the article title to create the link for
	 * @param linkText the text for the link
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getLinkHTMLToArticle(String title, String linkText) {
		return "<a href='" + getURLLink(title) + "' >" + linkText + "</a>";
	}

	/**
	 * Creates a url link that can be placed e.g. into a &lt;a href="..."&gt; tag to reference the specified article.
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
	 * Creates a &lt;a href="..."&gt; styled link to the specified article in the specified version.
	 *
	 * @param title   the article title to create the link for
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
	 * Creates a &lt;a href="..."&gt; styled link to the specified attachment of the specified article.
	 *
	 * @param article    the article to create the link for
	 * @param attachment the attachment to create the link for
	 * @return the created link
	 */
	public static String getURLLink(Article article, String attachment) {
		return "attach/" + article.getTitle() + "/" + attachment;
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified attachment of the specified article.
	 *
	 * @param attachment the attachment to create the link for
	 * @return the created link
	 */
	public static String getURLLink(WikiAttachment attachment) {
		return "attach/" + attachment.getPath();
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to this section. The created link navigates the user to the article of
	 * the section. If the section is rendered with an anchor (see method {@link #getAnchor(Section)}) the page is also
	 * scrolled to the section.
	 *
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Article)
	 * @see #getWikiLink(Section)
	 */
	public static String getLinkHTMLToSection(Section<?> section) {
		return "<a href='" + getURLLink(section) + "'>" + section
				.getTitle() + "</a>";
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to this section. The created link navigates the user to the article of
	 * the section. If the section is rendered with an anchor (see method {@link #getAnchor(Section)}) the page is also
	 * scrolled to the section.
	 *
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Article)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(Section<?> section) {
		String title = section.getTitle();
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager instanceof DefaultArticleManager) {
			Set<Section<AttachmentType>> compilingAttachmentSections = ((DefaultArticleManager) articleManager).getAttachmentManager()
					.getCompilingAttachmentSections(section.getArticle());
			if (!compilingAttachmentSections.isEmpty()) {
				title = compilingAttachmentSections.iterator().next().getTitle();
			}
		}
		return "Wiki.jsp?page=" + Strings.encodeURL(title) + "#" + getAnchor(section);
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the diff of the specified article. Usually, version1 is the newer
	 * version and version2 the older one.
	 *
	 * @param title    the article title to create the link for
	 * @param version1 the version containing the changes
	 * @param version2 the base version
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getDiffURLLink(String title, int version1, int version2) {
		return "Diff.jsp?page=" + title + "&r1=" + version1 + "&r2=" + version2;
	}

	public static String getURLLinkToObjectInfoPage(Identifier identifier) {
		return "javascript:KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(&quot;" + Strings.unquote(identifier.toExternalForm()) + "&quot;);";
	}

	/**
	 * Returns the String stored as a cookies with the given name from the UserContext. If no cookie exists, null is
	 * returned.
	 *
	 * @param name    the name of the cookie
	 * @param context the user context
	 * @return the stored cookie or null, if no cookie with the given name exists
	 * @created 11.02.2013
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
	 * Cleans cookies for Sections that do no longer exist. This only works, if the section is sometime called from
	 * within an ajax Action (like RerenderContentPartAction), because an ActionContext is needed.
	 *
	 * @param cookieNamePattern a pattern that matches the name for the cookie to be cleaned up exactly
	 * @param sectionIdGroup    the capture group in the pattern that contains the section id.
	 */
	public static void cleanupSectionCookies(UserContext context, Pattern cookieNamePattern, int sectionIdGroup) {
		if (!(context instanceof UserActionContext)) return;
		HttpServletRequest request = context.getRequest();
		if (request == null) return;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String name = cookie.getName();
				Matcher matcher = cookieNamePattern.matcher(name);
				if (!matcher.find()) continue;
				String sectionId = matcher.group(sectionIdGroup);
				try {
					Section<?> section = Sections.get(sectionId);
					if (section != null) continue;
				}
				catch (NumberFormatException ignore) {
					// if the parse section id is not a valid section id, we also clean up the cookie
				}
				cookie.setMaxAge(0);
				((UserActionContext) context).getResponse().addCookie(cookie);
			}
		}
	}

	/**
	 * Convenience method to get the value of the cookie. This method does the same as the Method {@link
	 * KnowWEUtils#getCookie(String, UserContext)}, but never returns null, except null is given as the default value.
	 * The default value will not be set as the cookie, but returned, if no cookie is given yet (for example with new
	 * users).
	 *
	 * @param name         the name of the cookie
	 * @param defaultValue the value to be returned, if the cookie could not be found
	 * @param context      the user context
	 * @return the stored cookie or null, if no cookie with the given name exists
	 * @created 11.02.2013
	 */
	public static String getCookie(String name, String defaultValue, UserContext context) {
		String cookie = getCookie(name, context);
		if (cookie == null) cookie = defaultValue;
		return cookie;
	}

	/**
	 * Returns the version of the article at the given date or the version saved closest before the given date. If there
	 * is no such version or article, -1 is returned. In JSPWiki -1 represents the latest version of an article.
	 */
	public static int getArticleVersionAtDate(String title, Date date) throws IOException {
		WikiPageInfo articleVersionInfoAtDate = getArticleVersionInfoAtDate(title, date);
		return articleVersionInfoAtDate == null ? -1 : articleVersionInfoAtDate.getVersion();
	}

	/**
	 * Returns an info object representing the version of the article that was saved at the given date or the version
	 * saved closest and before the given date. If there is no such version or the article does not exist, null is
	 * returned.
	 *
	 * @return an info object of the article for the given date
	 */
	public static WikiPageInfo getArticleVersionInfoAtDate(String title, Date date) throws IOException {
		return getObjectInfoAtDate(Environment.getInstance().getWikiConnector().getArticleHistory(title), date);
	}

	/**
	 * Returns an info object representing the version of the attachment that was saved at the given date or the version
	 * saved closest and before the given date. If there is no such version or the attachment does not exist, null is
	 * returned.
	 *
	 * @return an info object of the attachment for the given date
	 */
	public static WikiAttachmentInfo getAttachmentVersionInfoAtDate(String title, Date date) throws IOException {
		return getObjectInfoAtDate(Environment.getInstance().getWikiConnector().getAttachmentHistory(title), date);
	}

	/**
	 * Returns the version of the attachment at the given date or the version saved closest before the given date. If
	 * there is no such version or article, -1 is returned. In JSPWiki -1 represents the latest version of an
	 * attachment.
	 */
	public static int getAttachmentVersionAtDate(String title, Date date) throws IOException {
		WikiAttachmentInfo attachmentVersionInfoAtDate = getAttachmentVersionInfoAtDate(title, date);
		return attachmentVersionInfoAtDate == null ? -1 : attachmentVersionInfoAtDate.getVersion();
	}

	private static <T extends WikiObjectInfo> T getObjectInfoAtDate(List<T> objectHistory, Date date) throws IOException {
		return objectHistory.stream()
				// get the first that was saved before or equal to the given date
				.filter(pageInfo -> pageInfo.getSaveDate().before(date) || pageInfo.getSaveDate().equals(date))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Creates a wiki-markup-styled link to this section. The created link navigates the user to the article of the
	 * section. If the section is rendered with an anchor (see method {@link #getAnchor(Section)}) the page is also
	 * scrolled to the section.
	 * <p>
	 * Please note that the link will only work if it is put into "[" ... "]" brackets and rendered through the wiki
	 * rendering pipeline.
	 *
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getURLLink(Article)
	 */
	public static String getWikiLink(Section<?> section) {
		String link = section.getTitle();
		// append section id if the section is not the root section
		if (section.getParent() != null) {
			link += "#" + Math.abs(section.getID().hashCode());
		}
		return link;
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
		return Strings.readStream(inputStream);
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
	 * @return the {@link TerminologyManager} for the given (master) article.
	 * @deprecated use the method {@link KnowWEUtils#getTerminologyManagers(Section)}
	 */
	@Deprecated
	public static TerminologyManager getTerminologyManager(Article article) {
		if (article == null) {
			//noinspection ConstantConditions
			return Compilers.getCompiler(KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB),
					DefaultGlobalCompiler.class).getTerminologyManager();
		}
		Section<PackageCompileType> compileSection = Sections.successor(article.getRootSection(), PackageCompileType.class);
		// to emulate old behavior (not return null) we return an empty
		// TerminologyManager
		if (compileSection == null) return new TerminologyManager();
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(compileSection);
		if (terminologyManagers.isEmpty()) return null;
		return terminologyManagers.iterator().next();
	}

	/**
	 * Returns the TerminologyManagers of the {@link TermCompiler}s compiling the give compile Section.
	 *
	 * @param section the section to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given section
	 * @created 15.11.2013
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(Section<?> section) {
		Collection<TermCompiler> compilers = Compilers.getCompilers(section, TermCompiler.class);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager}.
	 *
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given manager
	 * @created 15.11.2013
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager) {
		Collection<TermCompiler> compilers = Compilers.getCompilers(manager, TermCompiler.class);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager} that are from {@link Compiler} of the given
	 * compiler class.
	 *
	 * @param manager       the {@link ArticleManager} to get the TerminologyManagers for
	 * @param compilerClass the type of the {@link Compiler}s we want the {@link TerminologyManager}s from
	 * @return the {@link TerminologyManager}s of the given manager
	 * @created 15.11.2013
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager, Class<? extends TermCompiler> compilerClass) {
		Collection<? extends TermCompiler> compilers = Compilers.getCompilers(manager,
				compilerClass);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	private static Collection<TerminologyManager> getTerminologyManagers(Collection<? extends TermCompiler> compilers) {
		return compilers.stream()
				.map(TermCompiler::getTerminologyManager)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the article with the given web and title from the {@link DefaultArticleManager}.
	 *
	 * @created 07.01.2014
	 */
	public static Article getArticle(String web, String title) {
		return Environment.getInstance().getArticle(web, title);
	}

	/**
	 * Returns the {@link ArticleManager} for the default web.
	 *
	 * @created 18.04.2019
	 */
	public static ArticleManager getDefaultArticleManager() {
		return Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
	}

	/**
	 * Returns the default {@link ArticleManager} of the given web.
	 *
	 * @param web the web we want the {@link ArticleManager} from
	 * @created 07.01.2014
	 */
	public static ArticleManager getArticleManager(String web) {
		return Environment.getInstance().getArticleManager(web);
	}

	/**
	 * Returns the {@link PackageManager} of the given {@link ArticleManager}.
	 *
	 * @param manager the ArticleManager we want the {@link PackageManager} from
	 * @created 07.01.2014
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
	 * @param section the {@link Section} we want the {@link PackageManager} for
	 * @created 07.01.2014
	 */
	public static PackageManager getPackageManager(Section<?> section) {
		ArticleManager manager = Environment.getInstance().getArticleManager(section.getWeb());
		return getPackageManager(manager);
	}

	/**
	 * Returns the default {@link PackageManager} of the given web.
	 *
	 * @param web the web we want the {@link PackageManager} from
	 * @created 07.01.2014
	 */
	public static PackageManager getPackageManager(String web) {
		return getPackageManager(getArticleManager(web));
	}

	public static void storeObject(Section<?> s, String key, Object o) {
		KnowWEUtils.storeObject(null, s, key, o);
	}

	public static void storeObject(Compiler compiler, Section<?> s, String key, Object o) {
		s.storeObject(compiler, key, o);
	}

	public static Object getStoredObject(Section<?> s, String key) {
		return KnowWEUtils.getStoredObject(null, s, key);
	}

	public static Object getStoredObject(Compiler compiler, Section<?> s, String key) {
		return s.getObject(compiler, key);
	}

	/**
	 * Returns the date the specified article has been modified the last time. This method also works if the specified
	 * article is not the latest one. Therefore this method does NOT (!) return the modification date of the specified
	 * article, but the modification date of the specified article's name within the corresponding article manager.
	 *
	 * @param article the article to get the latest modification date for
	 * @return the last modification date
	 * @created 16.02.2014
	 */
	public static Date getLastModified(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();
		int version = connector.getVersion(title);
		return connector.getLastModifiedDate(title, version);
	}

	/**
	 * Returns the latest @{@link WikiPageInfo instance from the given article's history.
	 *
	 * @param article Required
	 * @return null if no history exists
	 */
	public static WikiPageInfo getLatestArticleHistory(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();

		try {
			List<WikiPageInfo> articleHistory = connector.getArticleHistory(title);
			return (articleHistory != null && articleHistory.size() > 0) ? articleHistory.get(0) : null;
		}
		catch (IOException e) {
			Log.warning("Error fetching article history for " + article, e);
			return null;
		}
	}

	/**
	 * Returns the version number of the latest version of the specified article. This method also works if the
	 * specified article is not the latest instance. Therefore this method does NOT (!) return the version of the
	 * specified article, but the latest version of the specified article's name within the corresponding article
	 * manager.
	 *
	 * @param article the article to get the latest version for
	 * @return the latest version
	 * @created 16.02.2014
	 */
	public static int getLatestVersion(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();
		return connector.getVersion(title);
	}

	/**
	 * Returns a unique hash or integer for the status of the wiki for a given user. The hash should stay the same as
	 * long as the status does not change, but change if the status changes.
	 *
	 * @param context context of the user
	 * @return current wiki status for a user
	 */
	public static String getOverallStatus(UserContext context) {
		int overAllStatus = 0;
		for (StatusProvider statusProvider : Plugins.getStatusProviders()) {
			overAllStatus += statusProvider.getStatus(context);
		}
		return Integer.toHexString(overAllStatus);
	}

	/**
	 * Renames and article
	 */
	public static void renameArticle(String oldArticleTitle, String newArticleTitle) {
		ArticleManager manager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		Article oldArticle = manager.getArticle(oldArticleTitle);
		manager.open();
		try {
			manager.deleteArticle(oldArticle);
			Environment.getInstance().buildAndRegisterArticle(
					Environment.DEFAULT_WEB, newArticleTitle, oldArticle.getText());
		}
		finally {
			manager.commit();
		}
	}

	/**
	 * Returns and array of locales based on sorted by preferred labels given by the browser. This array always contains
	 * at least one element.
	 */
	public static Locale[] getBrowserLocales(UserContext context) {
		return getBrowserLocales(context.getRequest());
	}

	/**
	 * Returns and array of locales based on sorted by preferred labels given by the browser. This array always contains
	 * at least one element.
	 */
	@NotNull
	public static Locale[] getBrowserLocales(HttpServletRequest request) {
		Enumeration localesEnum = request.getLocales();
		if (localesEnum == null) return new Locale[] { Locale.ROOT }; // can be null in test environment
		@SuppressWarnings("unchecked")
		ArrayList<Locale> localList = Collections.list(localesEnum);
		if (localList.isEmpty()) return new Locale[] { Locale.ROOT };
		return localList.toArray(new Locale[localList.size()]);
	}

	/**
	 * Opens a page transaction for a large file based operation
	 * Also opens a transaction in ArticleManager
	 * Only works if GitVersioningFileProvider is active
	 */
	public static void openPageTransaction(UserContext context) {
		ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		articleManager.open();
		Environment.getInstance().getWikiConnector().openPageTransaction(context.getUserName());
	}

	/**
	 * Commits an open file transaction
	 * Also commits an ArticleManager transaction
	 * Only works if GitVersioningFileProvider is active
	 */
	public static void commitPageTransaction(UserContext context, String commitMsg) {
		Environment.getInstance().getWikiConnector().commitPageTransaction(context.getUserName(), commitMsg);
		ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		articleManager.commit();
	}

	/**
	 * Rollbacks an open file transaction
	 * Also rollbacks an ArticleManager transaction
	 * Only works if GitVersioningFileProvider is active
	 */
	public static void rollbackPageTransaction(UserContext context) {
		if (Environment.getInstance().getWikiConnector().hasRollbackPageProvider()) {
			ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
			articleManager.rollback();
		}
		Environment.getInstance().getWikiConnector().rollbackPageTransaction(context.getUserName());
	}
}
