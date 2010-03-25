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
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Renders the SearchMask for the KnowWEObjectTypeBrowser.
 * @see KnowWEObjectTypeBrowserRenderer.
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWEObjectTypeBrowserHandler extends AbstractTagHandler{
	
	public KnowWEObjectTypeBrowserHandler() {
		super("TypeBrowser");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWEObjectTypeBrowser.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		String typeName = null;
		Map<String, String> par = user.getUrlParameterMap();
		if (par != null) {
			String parameterName = "type";
			if (par.containsKey(parameterName)) {
				typeName= par.get(parameterName);
			}
		}
		
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance().getAllKnowWEObjectTypes();
		StringBuilder html = new StringBuilder();
		
		// Header
		html.append("<div id=\"KnowWEObjectTypeBrowser\" class=\"panel\"><h3>"
				+ KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWeObjectTypeBrowser.topic")
				+ "</h3>");
		html.append("<form method='post' action='' name='typebrowser'>");
		html.append("<fieldset>");
		
		// SelectList
		html.append("<select name=\"Auswahl\" size=\"6\">");
		
		// Entry for every Type
		String name = "";
		for (KnowWEObjectType type : types) {
			if (!type.getName().contains(".")) {
				name = type.getClass().getPackage().getName() + ".";
			}
			name += type.getName();
			
			String selected = "";
			if(typeName != null && name.endsWith((typeName))) {
				selected = "selected=\"1\"";
			}
			
			html.append("<option "+selected+" value=\"" + name + "\">" + type.getName() + "</option> \n" ); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		
		html.append("</select>");
		
		// Create a Search Button to start the Search for every Type
		html.append("<p><input type='button'"
			    + " value='" + KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWeObjectTypeBrowser.searchbutton") + "' name='' class='button' title=''/></p>");
	
		html.append("<div id=\"TypeSearchResult\"> </div>");
		
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("</div>");
		
		return html.toString();
	}
}
