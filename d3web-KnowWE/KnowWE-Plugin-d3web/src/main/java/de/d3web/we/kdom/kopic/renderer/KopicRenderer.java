/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.kopic.renderer;

import java.net.URLEncoder;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;


public class KopicRenderer extends KnowWEDomRenderer {
	
	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		String title = "Knowledge "
						+ generateLinkIcons(user.getUsername(), article.getTitle(), 
						KnowWEEnvironment.generateDefaultID(
								article.getTitle()), article.getWeb(), false, sec.getId());
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(article, sec, user, b);
		string.append(wrapCollapsebox(title, b.toString()));
	}

	private String wrapCollapsebox(String title, String render) {
		StringBuilder result = new StringBuilder();
		result.append("%%collapsebox-closed \n");
		result.append("! " +title + " \n");
		result.append(render);
		result.append("/%\n");
		return result.toString();
	}
	
	private String generateLinkIcons(String user, String topic, String id,
			String web, boolean error, String nodeID) {
		StringBuilder result = new StringBuilder(generateReportLink(topic, web, error));
		if (!error) {
			result.append(generateDialogLink(user, topic, id));
		}
		result.append(generateDownloadLink(topic, id, web, nodeID));
		if (!error) {
			result.append(generateJarLink(topic, id, web, nodeID));
		}
		return result.toString();
	}
	
	private String generateJarLink(String topic2, String id, String web2, String nodeID) {
		String icon = "<img src=KnowWEExtension/images/drive_disk.png title='Download jar file' /></img>";
		String result = "<a href='KnowWEDownload.jsp?KWiki_Topic="
			+ topic2 + "&web=" + web2 + "&nodeID=" + nodeID + "&filename=" + topic2
			+ "_kopic.jar' >" + icon + "</a>";
		return KnowWEUtils.maskHTML(result);
//		return KnowWEUtils.maskHTML("<a href='KnowWEExtension/KBrepository/" + web2 + "/"
//						+ topic2 + ".." + id + ".jar' >" + icon + "</a>");
	}

	private String generateDownloadLink(String topic2, String id, String web2,
			String nodeID) {
		
		String icon = "<img src=KnowWEExtension/images/disk.png title='Txt download' /></img>";
		String result = "<a href='KnowWEDownload.jsp?KWiki_Topic="
				+ topic2 + "&web=" + web2 + "&nodeID=" + nodeID + "&filename=" + topic2
				+ "_kopic.txt' >" + icon + "</a>";

		return KnowWEUtils.maskHTML(result);
	}

	public String generateDialogLink(String user, String topic, String actualID) {

		String prefix = KnowWEEnvironment.getInstance().getPathPrefix();
		return KnowWEEnvironment.HTML_ST
				+ "a target=kwiki-dialog href="
				+ prefix
				+ (prefix.length() != 0 ? "/" : "")
				+ "KnowWE.jsp?renderer=KWiki_dialog&action=RequestDialogRenderer&KWikisessionid="
				+ URLEncoder.encode(topic)
				+ ".."
				+ URLEncoder.encode(actualID)
				+ "&KWikiWeb=default_web&KWikiUser="
				+ user
				+ ""
				+ KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src=KnowWEExtension/images/run.gif title=Fall im d3web-Dialog starten /"
				+ KnowWEEnvironment.HTML_GT + KnowWEEnvironment.HTML_ST + "/a"
				+ KnowWEEnvironment.HTML_GT;
	}

	private String generateReportLink(String topicname, String web,
			boolean error) {
		String prefix = KnowWEEnvironment.getInstance().getPathPrefix();
		String pic = "statistics.gif";
		if (error) {
			pic = "statisticsError.gif";
		}
		return KnowWEEnvironment.HTML_ST + "a href=" + prefix
				+ (prefix.length() != 0 ? "/" : "")
				+ "KnowWE.jsp?action=getParseReport&topic="
				+ URLEncoder.encode(topicname) + "&KWiki_Topic="
				+ URLEncoder.encode(topicname) + "&web=" + web
				+ " target=_blank" + KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src='KnowWEExtension/images/" + pic
				+ "' title='Report'/" + KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST + "/a" + KnowWEEnvironment.HTML_GT;
	}

}
