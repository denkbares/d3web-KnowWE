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

package de.d3web.we.taghandler;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AdminPanelHandler extends AbstractTagHandler {
	
	public AdminPanelHandler() {
		super("adminpanel");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.AdminPanel.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		String header = rb.getString("KnowWE.AdminPanel.header");
		String overview = rb.getString("KnowWE.AdminPanel.overview");
		String reset = rb.getString("KnowWE.AdminPanel.reset");
		String parse = rb.getString("KnowWE.AdminPanel.parseall");
		
		String html = 
			"<div id=\"admin-panel\" class=\"panel\"><h3>" + header + "</h3>"
		    + "<ul>"
		    + "<li><a href=\"#summarizer\" onclick='doSumAll();'>"+overview+"</a><p id=\"sumAll\"></p></li>"
		    + "<li><a href=\"#reInit\" onclick='doReInit();'>"+reset+"</a><p id=\"reInit\"></p></li>"
		    + "<li><a href=\"#parseWeb\" onclick='doParseWeb();'>"+parse+"</a><p id=\"parseWeb\"></p></li>"
		    + "</ul></div>";
//				"<hr/></li> <li> <a href=\"#summarizer\" onclick='doSumAll();'><big>"+overview+"</big></a><div id=\"sumAll\"></div></li> "
//			+ "<li> <a href=\"#reInit\" onclick='doReInit();'><big>"+reset+"</big></a><div id=\"reInit\"></div>"
//			+ "</li> <li> <a href=\"#parseWeb\" onclick='doParseWeb();'><big>"+parse+"</big></a><div id=\"parseWeb\"></div><hr/>";
		return html;
	}

}
