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
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DescribeModuleTagHandler extends AbstractTagHandler {

	public DescribeModuleTagHandler() {
		super("describeModule");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values,
			String web) {

		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		
		String moduleName = null;
		Map<String, String> par = user.getUrlParameterMap();
		if (par != null) {
			String parameterName = "module";
			if (par.containsKey(parameterName)) {
				moduleName = par.get(parameterName);
			}
		}

		//TODO additionally try to read moduleName out from tag attributes (needs access to section id to allow access to sectionStore
		
		if (moduleName != null) {
			List<KnowWEModule> modules = KnowWEEnvironment.getInstance()
					.getModules();

			StringBuffer html = new StringBuffer();

			html.append("<div class=\"panel\"><h3>"
					+ rb.getString("KnowWE.DescribeModule.headline") + ": " + moduleName+"</h3>");

			for (KnowWEModule module : modules) {
				String className = module.getClass().getCanonicalName();
				String name = className.substring(module.getClass()
						.getCanonicalName().lastIndexOf(".") + 1);
				if (name.equals(moduleName)) {
					html.append("<i>Types:</i><br>");
					List<KnowWEObjectType> rootTypes = module.getRootTypes();
					for (KnowWEObjectType knowWEObjectType : rootTypes) {
						String subtypes = "";
						List<? extends KnowWEObjectType> allowedChildrenTypes = knowWEObjectType.getAllowedChildrenTypes();
						for (KnowWEObjectType knowWEObjectType2 : allowedChildrenTypes) {
							subtypes += knowWEObjectType2.getName() +",";
						}
						String link = "<a href=\"Wiki.jsp?page=TypeInfo&type=" + knowWEObjectType.getName()  + "\">" +
						knowWEObjectType.getName() + "</a>";
						
						html.append("<div class='left'>");
						html.append("<b>" + link + "</b>" + " (-->" + subtypes + ")");
						html.append("</div><br>");
					}
				
				}
			}

			html.append("</div>");

			return html.toString();
		} else {
			return rb.getString("KnowWE.DescribeModule.nomodule");
		}
	}

}
