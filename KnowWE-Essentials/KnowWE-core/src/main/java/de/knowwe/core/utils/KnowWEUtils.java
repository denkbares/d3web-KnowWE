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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
import de.knowwe.core.AttachmentManager;
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

	private static void mask(final StringBuilder buffer, final String toReplace) {
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

	private static void unmask(final StringBuilder buffer, final String toReplace) {
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
	public static String maskJSPWikiMarkup(final String string) {
		final StringBuilder temp = new StringBuilder(string);
		maskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Masks JSPWiki links (like [ArticleName]), but only if the link is invalid. A link is invalid, if there is no
	 * article with the given name and the links is also no to an external site or an attachment. This method can be
	 * used to gracefully handle rendering of copy&pasted texts containing [ and ].
	 */
	public static String maskInvalidJSPWikiLinks(final ArticleManager articleManager, final String text) {
		final Pattern linkFinder = Pattern.compile("\\[(?:[^]|]+\\|)?([^]]+)]");
		final Matcher matcher = linkFinder.matcher(text);
		final List<Pair<Integer, Integer>> escapeIndices = new ArrayList<>();
		while (matcher.find()) {
			final String link = matcher.group(1);
			if (articleManager.getArticle(link.replaceAll("#.*$", "")) == null
					&& !link.startsWith("http")
					&& !link.startsWith("file")
					&& !link.startsWith("attach")) {
				escapeIndices.add(new Pair<>(matcher.start(), matcher.end()));
			}
		}
		final StringBuilder builder = new StringBuilder(text);
		int shift = 0;
		for (final Pair<Integer, Integer> indices : escapeIndices) {
			final int start = indices.getA();
			final int end = indices.getB();
			builder.insert(indices.getA() + shift++, "~");
			// also escape opening brackets after the first one, but before the closing one
			// if they are not escaped, JSPWiki will also make links fore these accidental link subsection
			final char[] chars = new char[end - start - 1];
			builder.getChars(start + shift + 1, end + shift, chars, 0);
			for (int i = 0; i < chars.length; i++) {
				final char aChar = chars[i];
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
	public static String unmaskJSPWikiMarkup(final String string) {
		final StringBuilder temp = new StringBuilder(string);
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
	public static void maskJSPWikiMarkup(final StringBuilder builder) {
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
		mask(builder, "''");
	}

	/**
	 * Unmasks [, ], ----, {{{, }}} and %% so that tests of error messages run properly.
	 *
	 * @param builder the builder to destructively unmask its contents
	 * @created 28.09.2012
	 */
	public static void unmaskJSPWikiMarkup(final StringBuilder builder) {
		unmask(builder, "[");
		unmask(builder, "]");
		unmask(builder, "----");
		unmask(builder, "{{{");
		unmask(builder, "}}}");
		unmask(builder, "{{");
		unmask(builder, "}}");
		unmask(builder, "%%");
		unmask(builder, "\\");
		unmask(builder, "''");
	}

	public static void appendToFile(final String path, final String entry) {

		try {
			final OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(path, true),
					StandardCharsets.UTF_8.newEncoder()
			);
			final BufferedWriter out = new BufferedWriter(writer);
			out.write(entry);
			out.close();
		}
		catch (final Exception e) {
			Log.warning("Unable to append to File", e);
		}
	}

	public static String convertUmlaut(final String text) {
		if (text == null) {
			return null;
		}
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
	public static String getAnchor(final Section<?> section) {
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
	public static void renderAnchor(final Section<?> section, final RenderResult result) {
		final String anchorName = KnowWEUtils.getAnchor(section);
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
	public static WikiAttachment getAttachment(final String title, final String fileName) throws IOException {
		WikiAttachment actualAttachment = Environment.getInstance()
				.getWikiConnector().getAttachment(title + "/" + fileName);
		if (actualAttachment != null) {
			return actualAttachment;
		}
		final Collection<WikiAttachment> attachments = Environment.getInstance()
				.getWikiConnector()
				.getAttachments();
		for (final WikiAttachment attachment : attachments) {
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
	public static Collection<WikiAttachment> getAttachments(final String title, final String regex) throws IOException {
		final Collection<WikiAttachment> result = new LinkedList<>();
		final Collection<WikiAttachment> attachments = Environment.getInstance()
				.getWikiConnector().getAttachments();
		Pattern pattern;
		try {
			pattern = Pattern.compile(regex);
		}
		catch (final PatternSyntaxException e) {
			pattern = Pattern.compile(Pattern.quote(regex));
		}
		for (final WikiAttachment attachment : attachments) {
			// we return the attachment if:
			// the regex argument directly equals the path
			if (!regex.equals(attachment.getPath())) {
				// or the pattern matches the path
				if (!pattern.matcher(attachment.getPath()).matches()) {
					// or we have the correct title and
					if (!attachment.getParentName().equals(title)) {
						continue;
					}
					// the regex either directly equals the file name
					if (!regex.equals(attachment.getFileName())) {
						// or the pattern matches the filename
						if (!pattern.matcher(attachment.getFileName()).matches()) {
							continue;
						}
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
	 * Returns if the user has the rights to create new pages
	 *
	 * @param context the user context
	 * @return true if the user has the rights to create new articles
	 * @created 29.11.2013
	 */
	public static boolean canCreatePages(final UserContext context) {
		return Environment.getInstance().getWikiConnector().userCanCreateArticles(context.getRequest());
	}

	/**
	 * Returns if the user has the read access rights to the specified article.
	 *
	 * @param article the article to check the access rights for
	 * @param context the user context
	 * @return true if the user has the read access rights to the article
	 * @created 29.11.2013
	 */
	public static boolean canView(final Article article, final UserContext context) {
		final WikiConnector connector = Environment.getInstance().getWikiConnector();
		// try nine times with catching unexpected exception from AuthorizationManager
		for (int i = 0; i < 9; i++) {
			try {
				return connector.userCanViewArticle(article.getTitle(), context.getRequest());
			}
			catch (final ConcurrentModificationException e) {
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
	private static boolean canView(final Set<Article> articles, final UserContext context) {
		for (final Article article : articles) {
			if (!canView(article, context)) {
				return false;
			}
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
	public static boolean canView(final Section<?> section, final UserContext context) {
		return section != null && canView(section.getArticle(), context);
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
	public static boolean canView(final Collection<Section<?>> sections, final UserContext context) {
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
	public static void assertCanView(final Section<?> section, final UserContext context) throws NotAuthorizedException {
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
	public static boolean canWrite(final String articleTitle, final UserContext user) {
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
	public static boolean canWrite(final Article article, final UserContext user) {
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
	private static boolean canWrite(final Set<Article> articles, final UserContext user) {
		for (final Article article : articles) {
			if (!canWrite(article, user)) {
				return false;
			}
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
	public static boolean canWrite(final Section<?> section, final UserContext user) {
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
	public static void assertCanWrite(final Section<?> section, final UserContext context) throws NotAuthorizedException {
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
	public static boolean isInGroup(final String groupName, final UserContext user) {
		return Environment.getInstance()
				.getWikiConnector()
				.userIsMemberOfGroup(groupName, user.getRequest());
	}

	/**
	 * Checks whether the given article is compiled via an %%Attachment markup using the @compile: true flag.
	 *
	 * @param article the article to check
	 * @return true if the given article is an attachment article, false otherwise
	 */
	public static boolean isAttachmentArticle(Article article) {
		ArticleManager articleManager = article.getArticleManager();
		if (!(articleManager instanceof DefaultArticleManager)) return false;
		AttachmentManager attachmentManager = ((DefaultArticleManager) articleManager).getAttachmentManager();
		return attachmentManager.isAttachmentArticle(article);
	}

	/**
	 * Checks whether the given user is a wiki admin.
	 *
	 * @param user the user to check
	 * @return true, if the user is an admin, false else
	 */
	public static boolean isAdmin(final UserContext user) {
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
	public static boolean canWrite(final Collection<Section<?>> sections, final UserContext user) {
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
	public static Identifier getTermIdentifier(final Section<?> section) {
		if (section.get() instanceof Term) {
			final Section<Term> termSection = Sections.cast(section, Term.class);
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
	public static String getTermName(final Section<?> termSection) {
		if (termSection.get() instanceof Term) {
			final Section<Term> simpleSection = Sections.cast(termSection, Term.class);
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
		final String baseUrl = StringUtils.stripEnd(Environment.getInstance().getWikiConnector().getBaseUrl(), "/");
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
	public static String getLinkHTMLToArticle(final Article article) {
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
	public static String getLinkHTMLToArticle(final String title) {
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
	public static String getLinkHTMLToArticle(final String title, final String linkText) {
		return "<a href='" + getURLLink(title) + "' >" + Strings.encodeHtml(linkText) + "</a>";
	}

	/**
	 * Creates a url link that can be placed e.g. into a &lt;a href="..."&gt; tag to reference the specified article.
	 *
	 * @param title the article title to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(final String title) {
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
	public static String getURLLink(final String title, final int version) {
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
	public static String getURLLink(final Article article) {
		return getURLLink(article.getTitle());
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified attachment of the specified article.
	 *
	 * @param article    the article to create the link for
	 * @param attachment the attachment to create the link for
	 * @return the created link
	 */
	public static String getURLLink(final Article article, final String attachment) {
		return "attach/" + article.getTitle() + "/" + attachment;
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified attachment of the specified article.
	 *
	 * @param attachment the attachment to create the link for
	 * @return the created link
	 */
	public static String getURLLink(final WikiAttachment attachment) {
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
	public static String getLinkHTMLToSection(final Section<?> section) {
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
	public static String getURLLink(final Section<?> section) {
		String title = section.getTitle();
		final ArticleManager articleManager = section.getArticleManager();
		if (articleManager instanceof DefaultArticleManager) {
			final Set<Section<AttachmentType>> compilingAttachmentSections = ((DefaultArticleManager) articleManager).getAttachmentManager()
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
	public static String getDiffURLLink(final String title, final int version1, final int version2) {
		return "Diff.jsp?page=" + title + "&r1=" + version1 + "&r2=" + version2;
	}

	public static String getURLLinkToObjectInfoPage(final Identifier identifier) {
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
	public static String getCookie(final String name, final UserContext context) {
		final HttpServletRequest request = context.getRequest();
		if (request == null) {
			return null;
		}
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
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
	public static void cleanupSectionCookies(final UserContext context, final Pattern cookieNamePattern, final int sectionIdGroup) {
		if (!(context instanceof UserActionContext)) {
			return;
		}
		final HttpServletRequest request = context.getRequest();
		if (request == null) {
			return;
		}
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				final String name = cookie.getName();
				final Matcher matcher = cookieNamePattern.matcher(name);
				if (!matcher.find()) {
					continue;
				}
				final String sectionId = matcher.group(sectionIdGroup);
				try {
					final Section<?> section = Sections.get(sectionId);
					if (section != null) {
						continue;
					}
				}
				catch (final NumberFormatException ignore) {
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
	public static String getCookie(final String name, final String defaultValue, final UserContext context) {
		String cookie = getCookie(name, context);
		if (cookie == null) {
			cookie = defaultValue;
		}
		return cookie;
	}

	/**
	 * Returns the version of the article at the given date or the version saved closest before the given date. If there
	 * is no such version or article, -1 is returned. In JSPWiki -1 represents the latest version of an article.
	 */
	public static int getArticleVersionAtDate(final String title, final Date date) throws IOException {
		final WikiPageInfo articleVersionInfoAtDate = getArticleVersionInfoAtDate(title, date);
		return articleVersionInfoAtDate == null ? -1 : articleVersionInfoAtDate.getVersion();
	}

	/**
	 * Returns an info object representing the version of the article that was saved at the given date or the version
	 * saved closest and before the given date. If there is no such version or the article does not exist, null is
	 * returned.
	 *
	 * @return an info object of the article for the given date
	 */
	public static WikiPageInfo getArticleVersionInfoAtDate(final String title, final Date date) throws IOException {
		return getObjectInfoAtDate(Environment.getInstance().getWikiConnector().getArticleHistory(title), date);
	}

	/**
	 * Returns an info object representing the version of the attachment that was saved at the given date or the version
	 * saved closest and before the given date. If there is no such version or the attachment does not exist, null is
	 * returned.
	 *
	 * @return an info object of the attachment for the given date
	 */
	public static WikiAttachmentInfo getAttachmentVersionInfoAtDate(final String title, final Date date) throws IOException {
		return getObjectInfoAtDate(Environment.getInstance().getWikiConnector().getAttachmentHistory(title), date);
	}

	/**
	 * Returns the version of the attachment at the given date or the version saved closest before the given date. If
	 * there is no such version or article, -1 is returned. In JSPWiki -1 represents the latest version of an
	 * attachment.
	 */
	public static int getAttachmentVersionAtDate(final String title, final Date date) throws IOException {
		final WikiAttachmentInfo attachmentVersionInfoAtDate = getAttachmentVersionInfoAtDate(title, date);
		return attachmentVersionInfoAtDate == null ? -1 : attachmentVersionInfoAtDate.getVersion();
	}

	private static <T extends WikiObjectInfo> T getObjectInfoAtDate(final List<T> objectHistory, final Date date) {
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
	public static String getWikiLink(final Section<?> section) {
		String link = section.getTitle();
		if (link != null && link.contains("/")) { // happens for compiled attachments, we link to the parent article
			link = link.replaceAll("/.*", "");
		}
		// append section id if the section is not the root section
		if (section.getParent() != null) {
			link += "#" + section.getID();
		}
		return link;
	}

	public static String cleanWikiPageName(String name) {
		return name.replace("+", "_").replaceAll("[\\\\/:]+", "-");
	}

	public static String readFile(final String fileName) {
		try {
			return Strings.readFile(fileName);
		}
		catch (final IOException e) {
			Log.severe("Unable to read File", e);
			return "";
		}
	}

	public static String readStream(final InputStream inputStream) {
		return Strings.readStream(inputStream);
	}

	public static void writeFile(final String path, final String content) {
		try {
			Strings.writeFile(path, content);
		}
		catch (final Exception e) {
			Log.severe("Unable to write file", e);
		}
	}

	/**
	 * @return the {@link TerminologyManager} for the given (master) article.
	 * @deprecated use the method {@link KnowWEUtils#getTerminologyManagers(Section)}
	 */
	@Deprecated
	public static TerminologyManager getTerminologyManager(final Article article) {
		if (article == null) {
			//noinspection ConstantConditions
			return Compilers.getCompiler(KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB),
					DefaultGlobalCompiler.class).getTerminologyManager();
		}
		final Section<PackageCompileType> compileSection = Sections.successor(article.getRootSection(), PackageCompileType.class);
		// to emulate old behavior (not return null) we return an empty
		// TerminologyManager
		if (compileSection == null) {
			return new TerminologyManager();
		}
		final Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(compileSection);
		if (terminologyManagers.isEmpty()) {
			return null;
		}
		return terminologyManagers.iterator().next();
	}

	/**
	 * Returns the TerminologyManagers of the {@link TermCompiler}s compiling the give compile Section.
	 *
	 * @param section the section to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given section
	 * @created 15.11.2013
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(final Section<?> section) {
		final Collection<TermCompiler> compilers = Compilers.getCompilers(section, TermCompiler.class);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager}.
	 *
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given manager
	 * @created 15.11.2013
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(final ArticleManager manager) {
		final Collection<TermCompiler> compilers = Compilers.getCompilers(manager, TermCompiler.class);
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
	public static Collection<TerminologyManager> getTerminologyManagers(final ArticleManager manager, final Class<? extends TermCompiler> compilerClass) {
		final Collection<? extends TermCompiler> compilers = Compilers.getCompilers(manager,
				compilerClass);
		return KnowWEUtils.getTerminologyManagers(compilers);
	}

	private static Collection<TerminologyManager> getTerminologyManagers(final Collection<? extends TermCompiler> compilers) {
		return compilers.stream()
				.map(TermCompiler::getTerminologyManager)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the article with the given web and title from the {@link DefaultArticleManager}.
	 *
	 * @created 07.01.2014
	 */
	public static Article getArticle(final String web, final String title) {
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
	@NotNull
	public static ArticleManager getArticleManager(final String web) {
		return Environment.getInstance().getArticleManager(web);
	}

	/**
	 * Returns the {@link PackageManager} of the given {@link ArticleManager}.
	 *
	 * @param manager the ArticleManager we want the {@link PackageManager} from
	 * @created 07.01.2014
	 */
	@NotNull
	public static PackageManager getPackageManager(final ArticleManager manager) {
		return Objects.requireNonNull(Compilers.getCompiler(manager, PackageRegistrationCompiler.class))
				.getPackageManager();
	}

	/**
	 * Returns the {@link PackageManager} for the given {@link Section}.
	 *
	 * @param section the {@link Section} we want the {@link PackageManager} for
	 * @created 07.01.2014
	 */
	public static PackageManager getPackageManager(final Section<?> section) {
		final ArticleManager manager = Environment.getInstance().getArticleManager(section.getWeb());
		return getPackageManager(manager);
	}

	/**
	 * Returns the default {@link PackageManager} of the given web.
	 *
	 * @param web the web we want the {@link PackageManager} from
	 * @created 07.01.2014
	 */
	public static PackageManager getPackageManager(final String web) {
		return getPackageManager(getArticleManager(web));
	}

	public static void storeObject(final Section<?> s, final String key, final Object o) {
		KnowWEUtils.storeObject(null, s, key, o);
	}

	public static void storeObject(final Compiler compiler, final Section<?> s, final String key, final Object o) {
		s.storeObject(compiler, key, o);
	}

	public static Object getStoredObject(final Section<?> section, final String key) {
		return KnowWEUtils.getStoredObject(null, section, key);
	}

	public static Object getStoredObject(final Compiler compiler, final Section<?> section, final String key) {
		return section.getObject(compiler, key);
	}

	/**
	 * Returns an object of the given class from section store using the fully qualified class name as key
	 *
	 * @param clazz the class of the returned object
	 * @return the object stored for the given clazz in the given section
	 */
	public static <T> T getStoredObject(final Section<?> section, final Class<T> clazz) {
		//noinspection unchecked
		return (T) getStoredObject(section, clazz.getName());
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
	public static Date getLastModified(final Article article) {
		final WikiConnector connector = Environment.getInstance().getWikiConnector();
		final String title = article.getTitle();
		final int version = connector.getVersion(title);
		return connector.getLastModifiedDate(title, version);
	}

	/**
	 * Returns the latest @{@link WikiPageInfo instance from the given article's history.
	 *
	 * @param article Required
	 * @return null if no history exists
	 */
	public static WikiPageInfo getLatestArticleHistory(final Article article) {
		final WikiConnector connector = Environment.getInstance().getWikiConnector();
		final String title = article.getTitle();

		try {
			final List<WikiPageInfo> articleHistory = connector.getArticleHistory(title);
			return (articleHistory != null && !articleHistory.isEmpty()) ? articleHistory.get(0) : null;
		}
		catch (final IOException e) {
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
	public static int getLatestVersion(final Article article) {
		final WikiConnector connector = Environment.getInstance().getWikiConnector();
		final String title = article.getTitle();
		return connector.getVersion(title);
	}

	/**
	 * Returns a unique hash or integer for the status of the wiki for a given user. The hash should stay the same as
	 * long as the status does not change, but change if the status changes.
	 *
	 * @param context context of the user
	 * @return current wiki status for a user
	 */
	public static String getOverallStatus(final UserContext context) {
		int overAllStatus = 0;
		for (final StatusProvider statusProvider : Plugins.getStatusProviders()) {
			overAllStatus += statusProvider.getStatus(context);
		}
		return Integer.toHexString(overAllStatus);
	}

	/**
	 * Renames and article
	 */
	public static void renameArticle(final String oldArticleTitle, final String newArticleTitle) {
		final ArticleManager manager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		final Article oldArticle = manager.getArticle(oldArticleTitle);
		manager.open();
		try {
			manager.deleteArticle(oldArticleTitle);
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
	public static Locale[] getBrowserLocales(final UserContext context) {
		return getBrowserLocales(context.getRequest());
	}

	/**
	 * Returns and array of locales based on sorted by preferred labels given by the browser. This array always contains
	 * at least one element.
	 */
	@NotNull
	public static Locale[] getBrowserLocales(final HttpServletRequest request) {
		final Enumeration<Locale> localesEnum = request.getLocales();
		if (localesEnum == null) {
			return new Locale[] { Locale.ROOT }; // can be null in test environment
		}
		final ArrayList<Locale> localList = Collections.list(localesEnum);
		if (localList.isEmpty()) {
			return new Locale[] { Locale.ROOT };
		}
		return localList.toArray(new Locale[0]);
	}

	/**
	 * Opens a page transaction for a large file based operation
	 * Also opens a transaction in ArticleManager
	 * Only works if GitVersioningFileProvider is active
	 */
	public static void openPageTransaction(final UserContext context) {
		final ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		articleManager.open();
		Environment.getInstance().getWikiConnector().openPageTransaction(context.getUserName());
	}

	/**
	 * Commits an open file transaction
	 * Also commits an ArticleManager transaction
	 * Only works if GitVersioningFileProvider is active
	 * Awaits termination of compile manager, although ArticleManager.commit() seems to do the same,
	 * it seems to be necessary for some actions, to get a correct section, which was just changed, in a following
	 * transaction
	 */
	public static void commitPageTransaction(final UserContext context, final String commitMsg) {
		Environment.getInstance().getWikiConnector().commitPageTransaction(context.getUserName(), commitMsg);
		final ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		articleManager.commit();
		try {
			articleManager.getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			Log.severe("awaitTermination was interrupted", e);
		}
	}

	/**
	 * Rollbacks an open file transaction
	 * Also rollbacks an ArticleManager transaction
	 * Only works if GitVersioningFileProvider is active
	 */
	public static boolean rollbackPageTransaction(final UserContext context) {
		boolean rolledBack = false;
		if (Environment.getInstance().getWikiConnector().hasRollbackPageProvider()) {
			final ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
			articleManager.rollback();
			rolledBack = true;
		}
		Environment.getInstance().getWikiConnector().rollbackPageTransaction(context.getUserName());
		return rolledBack;
	}

	/**
	 * Re-compiles the given articles without changing it. The articles are parsed and compiled again. The method waits
	 * until the compilation is completed.
	 *
	 * @param articles the articles to recompile
	 */
	public static void reCompileArticles(Article... articles) throws InterruptedException {
		if (articles.length == 0) return;
		ArticleManager articleManager = articles[0].getArticleManager();
		if (articleManager == null) return;
		articleManager.open();
		try {
			for (Article article : articles) {
				articleManager.registerArticle(article.getTitle(), article.getText());
			}
		}
		finally {
			articleManager.commit();
		}
		articleManager.getCompilerManager().awaitTermination();
	}
}
