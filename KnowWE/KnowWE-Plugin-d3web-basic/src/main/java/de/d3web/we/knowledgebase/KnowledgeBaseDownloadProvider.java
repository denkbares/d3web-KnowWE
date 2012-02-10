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

package de.d3web.we.knowledgebase;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import de.knowwe.core.KnowWEAttributes;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.d3web.action.DownloadKnowledgeBase;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

public class KnowledgeBaseDownloadProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		// and provide both download and refresh as tools
		Tool download = getDownloadTool(article, section, userContext);
		Tool qrCode = getQRCodeTool(article, section, userContext);
		Tool refresh = getRefreshTool(article, section, userContext);
		return new Tool[] {
				refresh, download, qrCode };
	}

	protected Tool getRefreshTool(KnowWEArticle article, Section<?> section, UserContext userContext) {
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

	protected Tool getDownloadTool(KnowWEArticle article, Section<?> section, UserContext userContext) {
		// tool to provide download capability
		String kbName = DefaultMarkupType.getContent(section).trim();
		if (kbName.isEmpty()) {
			kbName = "knowledgebase";
		}
		String jsAction = "window.location='action/DownloadKnowledgeBase" +
				"?" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
				"&" + KnowWEAttributes.WEB + "=" + article.getWeb() +
				"&" + DownloadKnowledgeBase.PARAM_FILENAME + "=" + kbName + ".d3web'";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/download16.gif",
				"Download",
				"Download the entire knowledge base into a single file for deployment.",
				jsAction);
	}

	protected Tool getQRCodeTool(KnowWEArticle article, Section<?> section, UserContext userContext) {
		// tool to provide download capability
		String kbName = DefaultMarkupType.getContent(section).trim();
		if (kbName.isEmpty()) {
			kbName = "knowledgebase";
		}
		String baseUrl = KnowWEEnvironment.getInstance().getWikiConnector().getBaseUrl();
		// try to replace hostname by ip address to allow access in local
		// networks
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			URL url = new URL(baseUrl);
			baseUrl = new URL(url.getProtocol(), ip, url.getPort(), url.getPath()).toExternalForm();
		}
		catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String kbURL = baseUrl + "action/DownloadKnowledgeBase" +
				"%3F" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
				"%26" + KnowWEAttributes.WEB + "=" + article.getWeb() +
				"%26" + DownloadKnowledgeBase.PARAM_FILENAME + "=" + kbName + ".d3web";

		try {
			kbURL = URLEncoder.encode(kbURL, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
		}

		String imageURL = "https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=" + kbURL;
		String id = section.getID();
		String jsAction = "var node=$E('.markupText', '" + id + "'); " +
				"var visible = (node.firstChild.nodeName == 'IMG'); " +
				"if (visible) node.firstChild.remove();" +
				"else " +
				"node.innerHTML='<img style=\\'float:left\\' " +
				"src=\\'" + imageURL + "\\'></img>'+node.innerHTML;";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/qrcode16.gif",
				"QR-Code",
				"Shows the QR-Code to download the knowledge base into mobile devices.",
				jsAction);
	}

}
