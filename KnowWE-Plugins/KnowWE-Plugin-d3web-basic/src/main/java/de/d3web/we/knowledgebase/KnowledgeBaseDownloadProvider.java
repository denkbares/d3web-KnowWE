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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.d3web.action.KnowledgeBaseDownloadAction;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

public class KnowledgeBaseDownloadProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// and provide both download as tools
		Tool download = getDownloadTool(section, userContext);
		if (download == null) {
			return new Tool[0];
		}
		Tool qrCode = getQRCodeTool(section, userContext);
		return new Tool[] { download, qrCode };
	}

	protected Tool getDownloadTool(Section<?> section, UserContext userContext) {
		Section<PackageCompileType> compileSection = Sections.successor(section,
				PackageCompileType.class);
		if (compileSection == null) {
			return null;
		}
		// check if knowledge base is empty
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(userContext, compileSection);
		if (D3webUtils.isEmpty(kb)) {
			return null;
		}

		// tool to provide download capability
		String kbName = DefaultMarkupType.getContent(section).trim();
		if (kbName.isEmpty()) {
			kbName = "knowledgebase";
		}
		String jsAction = "window.location='action/KnowledgeBaseDownloadAction" +
				"?" + Attributes.SECTION_ID + "=" + compileSection.getID() +
				"&amp;" + Attributes.WEB + "=" + compileSection.getWeb() +
				"&amp;" + KnowledgeBaseDownloadAction.PARAM_FILENAME + "=" + kbName + ".d3web'";
		return new DefaultTool(
				Icon.DOWNLOAD,
				"Download as File",
				"Download the entire knowledge base into a single file for deployment.",
				jsAction,
				Tool.CATEGORY_DOWNLOAD);
	}

	protected Tool getQRCodeTool(Section<?> section, UserContext userContext) {
		Section<PackageCompileType> compileSection = Sections.successor(section,
				PackageCompileType.class);
		if (compileSection == null) {
			return null;
		}
		// tool to provide download capability
		String kbName = DefaultMarkupType.getContent(section).trim();
		if (kbName.isEmpty()) {
			kbName = "knowledgebase";
		}
		String baseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
		// try to replace hostname by ip address to allow access in local
		// networks
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			URL url = new URL(baseUrl);
			baseUrl = new URL(url.getProtocol(), ip, url.getPort(), url.getPath()).toExternalForm();
		}
		catch (UnknownHostException | MalformedURLException e1) {
			// TODO Auto-generated catch block
			Log.severe("Exception creating url for QR code. ");
		}
		String kbURL = baseUrl + "action/KnowledgeBaseDownloadAction" +
				"?" + Attributes.SECTION_ID + "=" + compileSection.getID() +
				"&amp;" + Attributes.WEB + "=" + compileSection.getWeb() +
				"&amp;" + KnowledgeBaseDownloadAction.PARAM_FILENAME + "=" + kbName + ".d3web";
		kbURL = Strings.encodeURL(kbURL);

		String imageURL = "https://chart.googleapis.com/chart?cht=qr&amp;chs=200x200&amp;chl="
				+ kbURL;
		String id = section.getID();
		String jsAction = "var node=jq$('#" + id + "'); " +
				"var qrCode = jq$('#" + id + "_qr_code'); " +
				"if (qrCode.exists()) qrCode.remove();" +
				"else node.prepend('<img id=\\'" + id + "_qr_code\\' style=\\'float:left\\' " +
				"src=\\'" + imageURL + "\\' />');";
		return new DefaultTool(
				Icon.QRCODE,
				"Download via QR-Code",
				"Shows the QR-Code to download the knowledge base into mobile devices.",
				jsAction,
				Tool.ActionType.ONCLICK,
				Tool.CATEGORY_DOWNLOAD);
	}

}
