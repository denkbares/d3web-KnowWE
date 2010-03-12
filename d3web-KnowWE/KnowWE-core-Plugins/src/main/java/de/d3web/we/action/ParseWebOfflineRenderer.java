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

package de.d3web.we.action;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.utils.KnowWEUtils;

public class ParseWebOfflineRenderer extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String webname = parameterMap.get(KnowWEAttributes.WEB);

		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(parameterMap.getRequest());
		
		Map<String, String> articles = KnowWEEnvironment.getInstance()
				.getWikiConnector().getAllArticles(webname);
		Set<String> articleNames = articles.keySet();
		StringBuffer reports = new StringBuffer();
		int problems = 0;
		for (String name : articleNames) {
			KnowWEDomParseReport object = KnowWEEnvironment.getInstance()
					.getArticleManager(webname).saveUpdatedArticle(new KnowWEArticle(articles.get(name),
							name, KnowWEEnvironment.getInstance().getRootType(),webname));
			
			if (object.hasErrors()) {
				reports.append("<p class=\"box error\">");
			} else {
				reports.append("<p class=\"box ok\">");
			}
			reports.append(rb.getString("webparser.info.parsing")
					+ createLink(name, webname)+ "<br />");
			if (object.hasErrors()) {
				problems++;
				reports.append("<br />\n");
			} 
		}
		
		String converted = KnowWEUtils.convertUmlaut(reports.toString());
		reports.delete(0, reports.length());
		reports.append(converted);
		
		reports.insert(0, "<a href=\"#\" id='js-parseWeb' class='clear-element'>" + rb.getString("KnowWE.buttons.close") + "</a><br />");
		
		return reports.toString();

	}

	private String createLink(String topicName, String webname) {

		return "<a href='Wiki.jsp?page=" + topicName + "' target='_blank'>"
				+ topicName + "</a>";
	}
	
	@Override
	public boolean isAdminAction() {
		return true;
		//return false;  //for local testing
	}

}
