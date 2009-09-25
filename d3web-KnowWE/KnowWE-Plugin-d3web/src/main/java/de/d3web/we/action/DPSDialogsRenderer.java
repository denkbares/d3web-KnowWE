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

import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.dialog.Dialog;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class DPSDialogsRenderer implements KnowWEAction {

	private String iconURL;
	
	public DPSDialogsRenderer() {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String path = "KnowWEExtension/";
		iconURL = path + rb.getString("KWiki.config.path.images") + "";
	}

	public String perform(KnowWEParameterMap map){
		StringBuffer sb = new StringBuffer();
		Broker broker = D3webModule.getBroker(map);
		List<Dialog> history = broker.getDialogControl().getHistory();
		List<Dialog> instant = broker.getDialogControl().getInstantIndicatedDialogs();
		List<Dialog> indicated = broker.getDialogControl().getIndicatedDialogs();

		boolean painted = false;
		
		
		if (!instant.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div>");
			sb.append(getDialogLinkList(map, instant));
			sb.append("</div>");
			painted = true;
		}
		
		if (!indicated.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div>");
			sb.append(getDialogLinkList(map, indicated));
			sb.append("</div>");
			painted = true;
		}
		
		if (!history.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div>");
			sb.append(getDialogLinkList(map, history));
			sb.append("</div>");
			painted = true;
		}
	return sb.toString();
	}

	@SuppressWarnings("deprecation")
	private StringBuffer getDialogLinkList(KnowWEParameterMap map, List<Dialog> list) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(map.getRequest());
		StringBuffer sb = new StringBuffer();
		String user = map.getUser();
		String web = map.getWeb();
		sb.append("<ul>");
		for (Dialog each : list) {
			String link = "KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikisessionid="+each.getDialog().getNamespace()+"&KWikiUser="+user+"&KWikiWeb="+web;

			sb.append("<li>");
			sb.append("<div class='" + getColor(each) + "'>");
			sb.append("<a href=\""+link+"\" target='"+KnowWEAttributes.KNOWWE_DIALOG+"' ");
			sb.append(" title=\"");
			String comment = "";
			if(each.getComment() != null) {
				comment = "\n" + each.getComment();
			}
			String reason = rb.getString("KnowWE.user");
			if(each.getReason() != null) {
				reason = each.getReason().getNamespace();
			}
			sb.append(rb.getString("KnowWE.dialog.indicatedBy") + " " + reason + comment);
			sb.append("\">");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("windowIcon.gif");
			sb.append("\" border=0");
			sb.append("/> ");
			sb.append(each.getDialog().getNamespace().replaceAll("\\.\\.", ".. "));
			sb.append("</a>");
			if(each.isActive()) {
				sb.append(" ");
					sb.append("<a href='javascript:void(0)' onclick='kwiki_call(\"KnowWE.jsp?action=KWiki_noDelegate&KWikiNamespace="+ URLEncoder.encode(each.getDialog().getNamespace()/*, "UTF-8"*/)+"&KWikiUser="+user+"&KWikiWeb="+web+"\")'>");
				
				sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
				sb.append(iconURL);
				sb.append("cross.png");
				sb.append("\" border=0");
				sb.append(" title=\"");
				sb.append(rb.getString("KnowWE.dialog.cancel"));
				sb.append("\" />");
				sb.append("</a>");
			}
			sb.append("</div>");
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb;
	}

	private String getColor(Dialog each) {
		if(each.isActive()) {
			return "bg-green";
		} else if(each.isCancelled()) {
			return "bg-red";
		} else if(each.isFinished()) {
			return "bg-blue";
		}
		return "";
	}
}
