/*
 * Copyright (C) ${year} denkbares GmbH, Germany
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

package de.d3web.we.kdom.kopic;

import de.d3web.we.action.DownloadKnowledgeBase;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowledgeBaseDownloadProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		// and provide both download and refresh as tools
		Tool download = getDownloadTool(article, section, userContext);
		Tool refresh = getRefreshTool(article, section, userContext);
		return new Tool[] {
				refresh, download };
	}

	protected Tool getRefreshTool(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		// tool to execute a full-parse onto the knowledge base
		// may be removed in later releases (after moneypenny)
		String jsAction = "var url = window.location.href;" +
				"url = url.replace(/&parse=full/g, '');" +
				"if (url.indexOf('?') == -1) {url += '?';}" +
				"url += '&parse=full';" +
				"window.location = url;";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/refresh16.png",
				"Refresh",
				"Performs a fresh rebuild of the knowledge base from the wiki content.",
				jsAction);
	}

	protected Tool getDownloadTool(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		// tool to provide download capability
		String kbName = DefaultMarkupType.getContent(section).trim();
		String jsAction = "window.location='action/DownloadKnowledgeBase" +
				"?" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
				"&" + KnowWEAttributes.WEB + "=" + article.getWeb() +
				"&" + DownloadKnowledgeBase.PARAM_FILENAME + "=" + kbName + ".d3web'";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/download16.gif",
				"Download",
				"Download the whole knowledge base into a single file for deployment.",
				jsAction);
	}

}
