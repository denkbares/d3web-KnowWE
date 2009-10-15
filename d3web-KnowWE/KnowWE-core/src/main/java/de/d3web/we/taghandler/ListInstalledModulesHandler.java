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

import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ListInstalledModulesHandler extends AbstractTagHandler{

	private String name = null;
	
	public ListInstalledModulesHandler() {
		super("listInstalledModules");
	}


	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values,
			String web) {
		
		List<KnowWEModule> modules = KnowWEEnvironment.getInstance().getModules();
		
		StringBuffer html = new StringBuffer();
		
		html.append("<div id=\"installed-modules-panel\" class=\"panel\"><h3>" + KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.ListInstalledModules.headline") + "</h3>");
		html.append("<div>");
		if(modules.size() == 0) {
			html.append("<b>(no modules installed)</b>");
		}
				
		for (KnowWEModule module : modules) {
			html.append("<div class='left'>");
			String className = module.getClass().getCanonicalName();
			String name = className.substring(module.getClass().getCanonicalName().lastIndexOf(".")+1);
			
			String link = "<a href=\"Wiki.jsp?page=ModuleInfo&module=" + name  + "\">" +
			name + "</a>";
			
			html.append("<b>"+link+"</b>"+ " ("+className+")");
			html.append("</div><br>");
		}
		
		html.append("</div></div>");
		
		return html.toString();
	}
	
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.ListInstalledModules.description");
	}

}
