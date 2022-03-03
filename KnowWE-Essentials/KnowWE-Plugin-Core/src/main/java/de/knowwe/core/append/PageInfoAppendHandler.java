/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.append;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * This handler appends some basic information about the page and the current
 * user to the page.
 *
 * @author Reinhard Hatko
 * @created 18.10.2012
 */
public class PageInfoAppendHandler implements PageAppendHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PageInfoAppendHandler.class);

	@Override
	public void append(Article article, UserContext user, RenderResult html) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		if (article == null) return; // Can happen in preview mode
		final String title = article.getTitle();
		int version = connector.getVersion(title);
		long modDate = connector.getLastModifiedDate(title, -1).getTime();
		String userName = user.getUserName();
		String overallStatus = KnowWEUtils.getOverallStatus(user);

		html.appendHtml("<input type='hidden' id='knowWEInfoWeb' value='" + article.getWeb() + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoPageName' value=" + Strings.quote(title) + ">");
		html.appendHtml("<input type='hidden' id='knowWEInfoPageVersion' value='" + version + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoPageDate' value='" + modDate + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoUser' value=" + Strings.quote(userName) + ">");
		html.appendHtml("<input type='hidden' id='knowWEInfoLanguage' value='" + connector.getLocale(user.getRequest())
				.getLanguage() + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoStatus' value='" + overallStatus + "'>");
		html.appendHtml("<div style='display:none' comment='The following infos can be edited by the user any time, "
				+ "so it should only be used for superficial rendering/styling, not as a security check!'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoAdmin' value='" + KnowWEUtils.isAdmin(user) + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoCanWrite' value='" + KnowWEUtils.canWrite(article, user) + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoCanView' value='" + KnowWEUtils.canView(article, user) + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoCanCreatePages' value='" + KnowWEUtils.canCreatePages(user) + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoTemplate' value='" + connector.getTemplate() + "'>");
		html.appendHtml("</div>");
		if (KnowWEUtils.canView(article, user)) {
			html.appendHtml("<div type='hidden' style='display:none' id='knowWEInfoAttachments'>");
			try {
				for (WikiAttachment wikiAttachment : connector.getAttachments(article.getTitle())) {
					html.appendHtmlElement("span", wikiAttachment.getFileName(), "class", "knowWEInfoAttachmentName", "data-size", String.valueOf(wikiAttachment.getSize()));
				}
			}
			catch (IOException e) {
				LOGGER.error("Unable to retrieve attachments", e);
			}
			html.appendHtml("</div>");
		}
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
