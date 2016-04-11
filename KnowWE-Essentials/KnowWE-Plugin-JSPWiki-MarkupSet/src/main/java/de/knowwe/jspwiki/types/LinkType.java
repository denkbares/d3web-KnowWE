/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.jspwiki.types;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.RenderPreviewAction;
import de.knowwe.core.action.RenderPreviewAction.Mode;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.DefaultMessageRenderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Patterns;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * 
 * @author Stefan Plehn
 * @created 28.06.2011
 */
public class LinkType extends AbstractType {

	private static class LinkRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			Section<LinkType> linkSection = Sections.cast(section, LinkType.class);
			Section<?> target = getReferencedSection(linkSection);
			String linkMarkup = section.getText();
			if (target == null) {
				// special case: if anchor link,
				// but only anchor does not exist, render anchor as error
				String link = getLink(linkSection);
				int index = link.indexOf('#');
				if (index > 0) {
					String articleName = link.substring(0, index);
					String headerName = link.substring(index + 1);
					if (user.getArticleManager().getArticle(articleName) != null
							&& !Strings.isBlank(headerName)) {
						result.append("&#91;");
						if (linkMarkup.contains("|")) {
							result.append(getDisplayText(linkSection)).append(" &#124; ");
						}
						result.append("[").append(articleName).append("]").appendJSPWikiMarkup("#");
						Message msg = Messages.error(
								"No section '" + headerName + "' in article '" + articleName + "'");
						DefaultMessageRenderer errorRenderer = DefaultMessageRenderer.ERROR_RENDERER;
						errorRenderer.preRenderMessage(msg, user, null, result);
						result.append(headerName);
						errorRenderer.postRenderMessage(msg, user, null, result);
						result.append("&#93;");
						return;
					}
				}
				// render as plain wiki markup only
				result.append(linkMarkup);
			}
			else {
				// render also as plain wiki markup,
				// but decorate with tooltipster-based ajax preview
				String previewSrc = "action/RenderPreviewAction"
						+ "?" + Attributes.TITLE + "=" +
						Strings.encodeURL(target.getTitle())
						+ "&amp;" + Attributes.WEB + "=" + target.getWeb()
						+ "&amp;" + RenderPreviewAction.ATTR_MODE + "=" + Mode.plain.name()
						+ "&amp;" + Attributes.SECTION_ID + "=" + target.getID();
				if (target.get() instanceof RootType) {
					// render all plain wiki-pages links normally
					result.append(linkMarkup);
				}
				else {
					// render anchor links (to headers, definitions, footnotes, ...) with preview
					result.appendHtml("<span class='tooltipster ajax'")
							.appendHtml(" data-tooltip-src='" + previewSrc + "'>")
							.append(linkMarkup)
							.appendHtml("</span>");
				}
			}
		}
	}

	// most important external protocols.
	public static final String[] EXTERNAL_PROTOCOLS = {
			"http:", "ftp:", "ftps:", "mailto:", "https:", "news:" };

	// public static final String REGEX = "(?<=(^|[^\\[]))\\[[^\\[{].*?\\]";
	public static final Pattern PATTERN = Pattern.compile(Patterns.JSPWIKI_LINK);
	public static final Pattern ATTRIBUTES_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*'([^']*)'");

	public LinkType() {
		setSectionFinder(new RegexSectionFinder(PATTERN));
		setRenderer(new LinkRenderer());
	}

	/**
	 * Returns the link text of the specified link type.
	 * 
	 * @created 22.05.2013
	 * @param link the link type to get the link text part from
	 * @return s the target of the link. This is an article or attachment link
	 *         for internal links, an URL for for external links.
	 */
	public static String getLink(Section<LinkType> link) {
		Matcher matcher = PATTERN.matcher(link.getText());
		if (!matcher.matches()) return null; // shouldn't happen

		if (matcher.group(Patterns.LINK_GROUP_LINK) != null) return matcher.group(
				Patterns.LINK_GROUP_LINK).trim();
		else return matcher.group(Patterns.LINK_GROUP_TEXT).trim();
	}

	/**
	 * Returns the optional displayed text of the link. If none is specified, it
	 * returns the link text.
	 * 
	 * @created 22.05.2013
	 * @param link the link type section to get the display text from
	 * @return the display text of this link
	 */
	public static String getDisplayText(Section<LinkType> link) {
		Matcher matcher = PATTERN.matcher(link.getText());
		if (!matcher.matches()) return null; // shouldn't happen

		if (matcher.group(Patterns.LINK_GROUP_TEXT) != null) return matcher.group(
				Patterns.LINK_GROUP_TEXT).trim();
		else return getLink(link);
	}

	/**
	 * Checks, if the specified link is internal and points to an attachment.
	 * 
	 * @created 22.05.2013
	 * @param link the link type section to check
	 * @return if the link references an attachment
	 */
	public static boolean isAttachment(Section<LinkType> link) {
		Matcher matcher = PATTERN.matcher(link.getText());
		if (!matcher.matches()) return false; // shouldn't happen

		if (!isInternal(link)) return false;

		String path = getLink(link);

		// qualified attachment
		if (path.contains("/")) return true;

		// not qualified, check for attachment to current page
		try {
			List<WikiAttachment> list = Environment.getInstance().getWikiConnector().getAttachments(
					link.getTitle());

			for (WikiAttachment wikiAttachment : list) {
				if (wikiAttachment.getFileName().equalsIgnoreCase(path)) return true;
			}
			return false;

		}
		catch (IOException e) {
			return false;
		}

	}

	/**
	 * Checks, if the specified link type is an inner-wiki link, referencing
	 * an other page (and/or header) in this wiki.
	 *
	 * @created 22.05.2013
	 * @param link the link type section to check
	 * @return if the link references this wiki
	 */
	public static boolean isInternal(Section<LinkType> link) {
		return isInternal(getLink(link));
	}

	/**
	 * Checks, if the specified link type is a link to a global url.
	 *
	 * @created 22.05.2013
	 * @param link the link type section to check
	 * @return if the link references a global url
	 */
	public static boolean isExternal(Section<LinkType> link) {
		return isExternal(getLink(link));
	}

	/**
	 * Checks, if the specified link type points to a footnote.
	 *
	 * @created 22.05.2013
	 * @param link the link type section to check
	 * @return if the link references a footnote
	 */
	public static boolean isFootnote(Section<LinkType> link) {
		return isFootnote(getLink(link));
	}

	/**
	 * Checks, if the specified link type is an inter-wiki link, referencing
	 * an other wiki (and not a global url).
	 *
	 * @created 22.05.2013
	 * @param link the link section to check
	 * @return if the link references another this wiki
	 */
	public static boolean isInterWiki(Section<LinkType> link) {
		return isInterWiki(getLink(link));
	}

	/**
	 * Returns the section that is referenced by this link or null if the
	 * section does not exist or if it is not an internal link. The returned
	 * section (if there is any) is either the root section, a header section, a
	 * (inline/normal) definition section or a footnote section.
	 * 
	 * @created 20.02.2014
	 * @param section the link to get the referenced (target) section for
	 * @return the referenced sections
	 */
	public static Section<?> getReferencedSection(Section<LinkType> section) {
		return getReferencedSection(section.getArticle(), getLink(section));
	}

	/**
	 * Returns the section that is referenced by this link or null if the
	 * section does not exist or if it is not an internal link. The returned
	 * section (if there is any) is either the root section, a header section, a
	 * (inline/normal) definition section or a footnote section.
	 * 
	 * @created 20.02.2014
	 * @param sourceArticle the article the link is located on
	 * @param link the link to get the referenced (target) section for
	 * @return the referenced sections
	 */
	public static Section<?> getReferencedSection(Article sourceArticle, String link) {
		if (isFootnote(link)) {
			// check for footnote section
			for (Section<FootnoteType> footnote : Sections.successors(sourceArticle,
					FootnoteType.class)) {
				String id = footnote.get().getFootnoteID(footnote);
				// if footnote matches, return if the footnote
				// is included in the export (if not, footnote will go wrong)
				if (link.equals(id)) return footnote;
			}
		}

		if (isInternal(link)) {
			// split link to article and header
			Article article;
			String headerName;
			int index = link.indexOf("#");
			if (index == 0) {
				article = sourceArticle;
				headerName = link.substring(1);
			}
			else if (link.contains("#")) {
				String articleName = link.substring(0, index);
				headerName = link.substring(index + 1);
				article = sourceArticle.getArticleManager().getArticle(articleName);
			}
			else {
				article = sourceArticle.getArticleManager().getArticle(link);
				headerName = null;
			}

			// if article not found, stop searching
			if (article == null) return null;
			// or article is the thing we are looking for (no header)
			if (headerName == null) return article.getRootSection();

			// search article for header or definition
			// otherwise search for the header sections
			for (Section<HeaderType> header : Sections.successors(article, HeaderType.class)) {
				String text = header.get().getHeaderText(header);
				if (text.equalsIgnoreCase(headerName)) {
					return header;
				}
			}
			// and for the definition sections
			for (Section<DefinitionType> def : Sections.successors(article, DefinitionType.class)) {
				String text = def.get().getHeadText(def);
				if (text.equalsIgnoreCase(headerName)) {
					return def;
				}
			}
		}

		return null;
	}

	/**
	 * Checks, if the specified link is a link to a global url.
	 *
	 * @created 22.05.2013
	 * @param link the link to check
	 * @return if the link references a global url
	 */
	public static boolean isExternal(String link) {
		if (link == null) return false;
		String lowerLink = link.trim().toLowerCase();
		for (String protocol : EXTERNAL_PROTOCOLS) {
			if (lowerLink.startsWith(protocol)) return true;
		}
		return false;
	}

	/**
	 * Checks, if the specified link points to a footnote.
	 *
	 * @created 22.05.2013
	 * @param link the link to check
	 * @return if the link references a footnote
	 */
	public static boolean isFootnote(String link) {
		return link != null && link.trim().matches("\\d+");
	}

	/**
	 * Checks, if the specified link type is an inter-wiki link, referencing
	 * an other wiki (and not a global url).
	 *
	 * @created 22.05.2013
	 * @param link the link to check
	 * @return if the link references another this wiki
	 */
	public static boolean isInterWiki(String link) {
		if (link == null) return false;
		// if we want to support interwiki links, it would be best to check
		// jspwiki.properties for defined interwiki links
		return !isExternal(link) && link.contains(":");
	}

	/**
	 * Checks, if the specified link is an inner-wiki link, referencing
	 * an other page (and/or header) in this wiki.
	 *
	 * @created 22.05.2013
	 * @param link the link to check
	 * @return if the link references this wiki
	 */
	public static boolean isInternal(String link) {
		return !isExternal(link) && !isInterWiki(link);
	}

	public static Map<String, String> getAttributes(Section<LinkType> link) {
		Matcher matcher = PATTERN.matcher(link.getText());
		if (!matcher.matches()) return null; // shouldn't happen

		if (matcher.group(Patterns.LINK_GROUP_ATTRIBUTES) == null) return Collections.emptyMap();
		Map<String, String> result = new HashMap<String, String>();

		String attributes = matcher.group(Patterns.LINK_GROUP_ATTRIBUTES);
		Matcher attMatcher = ATTRIBUTES_PATTERN.matcher(attributes);

		while (attMatcher.find()) {
			String name = attMatcher.group(1);
			String value = attMatcher.group(2);
			result.put(name, value);

		}

		return result;

	}

}
