/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.utils;

import java.util.ResourceBundle;

import de.d3web.we.basic.D3webModule;

public class KnowWERenderUtils {

	public static StringBuffer getTopicLink(String web, String name, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		// String web = (String) BasicUtils.getModelAttribute(model,
		// KnowWEAttributes.WEB, String.class, true);
		String link = getLinkToTopic(name, web);
		if (asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");
		}
		sb.append("<a href='" + link + "'>");
		sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
		sb.append(iconURL);
		sb.append("table_go.png");
		sb.append("\" border=0");
		sb.append(" title=\"");
		sb.append(rb.getString("KnowWE.topic.show"));
		sb.append("\" />");
		if (withTitle) sb.append(rb.getString("KnowWE.topic"));
		sb.append("</a>");
		if (asButton) {
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}

	public static String getLinkToTopic(String name, String web) {
		String link = "Wiki.jsp?page=" + name;
		return link;
	}

	public static final String knowweUrlPrefix = "KnowWE.jsp";
}