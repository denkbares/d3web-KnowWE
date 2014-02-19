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

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * 
 * @author Stefan Plehn
 * @created 28.06.2011
 */
public class LinkType extends AbstractType {

	// most important external protocols.
	public static final String[] EXTERNAL_PROTOCOLS = {
			"http:", "ftp:", "ftps:", "mailto:", "https:", "news:" };

	// public static final String REGEX = "(?<=(^|[^\\[]))\\[[^\\[{].*?\\]";
	public static final Pattern PATTERN = Pattern.compile(Patterns.JSPWIKI_LINK);
	public static final Pattern ATTRIBUTES_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*'([^']*)'");

	public LinkType() {
		this.setSectionFinder(new RegexSectionFinder(PATTERN));
	}

	/**
	 * 
	 * 
	 * @created 22.05.2013
	 * @param link
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
	 * @param link
	 * @return
	 */
	public static String getDisplayText(Section<LinkType> link) {
		Matcher matcher = PATTERN.matcher(link.getText());
		if (!matcher.matches()) return null; // shouldn't happen

		if (matcher.group(Patterns.LINK_GROUP_TEXT) != null) return matcher.group(
				Patterns.LINK_GROUP_TEXT).trim();
		else return getLink(link);
	}

	/**
	 * Checks, if a link is internal and points to an attachment.
	 * 
	 * @created 22.05.2013
	 * @param link
	 * @return
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

	public static boolean isInternal(Section<LinkType> link) {
		return !isExternal(link) && !isInterWiki(link);
	}

	public static boolean isExternal(Section<LinkType> link) {
		return isExternalForm(getLink(link));
	}

	public static boolean isFootnote(Section<LinkType> link) {
		return isFootnote(getLink(link));
	}

	public static boolean isInterWiki(Section<LinkType> link) {
		return isInterWikiForm(getLink(link));
	}

	public static boolean isExternalForm(String link) {
		if (link == null) return false;
		String lowerLink = link.trim().toLowerCase();
		for (String protocol : EXTERNAL_PROTOCOLS) {
			if (lowerLink.startsWith(protocol)) return true;
		}
		return false;
	}

	public static boolean isFootnote(String link) {
		if (link == null) return false;
		return link.trim().matches("\\d+");
	}

	public static boolean isInterWikiForm(String link) {
		if (link == null) return false;
		// if we want to support interwiki links, it would be best to check
		// jspwiki.properties for defined interwiki links
		return !isExternalForm(link) && link.contains(":");

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
