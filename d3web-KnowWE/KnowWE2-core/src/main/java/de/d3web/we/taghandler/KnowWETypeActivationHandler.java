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
 * Renders the Mask for the KnowWEObjectTypeActivatior.
 * See also KnowWEObjectTypeActivatorRenderer.
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWETypeActivationHandler extends AbstractTagHandler{
	
	/**
	 * Constructor.
	 */
	public KnowWETypeActivationHandler() {
		super("KnowWEObjectTypeActivator");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWEObjectTypeActivator.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance().getAllKnowWEObjectTypes();
		StringBuilder html = new StringBuilder();
		
		html.append("<div id=\"KnowWEObjectTypeActivator\" class=\"panel\"><h3>"
				+ KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWeObjectTypeActivator.topic")
				+ "</h3>");
		html.append("<form method='post' action='' name='typeactivator'>");
		html.append("<fieldset>");
		
		// Create SelectList
		html.append("<select name=\"Auswahl\" size=\"6\">");
		
		// Iterate over Types to create Checkboxes
		String spancolor;
		String name = "";
		for (KnowWEObjectType type : types) {
			
			// get the current Activation Status and set the color
			spancolor = "green";
			if (!type.getActivationStatus()){
				spancolor = "red";
			}
			
			// get the name of the current type
			if (!type.getName().contains(".")) {
				name = type.getClass().getPackage().getName() + ".";
			}
			name += type.getName();
			
			// insert type with spancolor
			html.append("<option value=\"" + name + "\"style=\"color:" + spancolor + "\">"
						+ type.getName()
						+ "</option> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		html.append("</select>");
		
		// button for changing
		html.append("<p><input type='button' class='button' "
					+ "value='" + KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.KnowWEObjectTypeActivator.changebutton") + "'"
					+ "onclick='switchTypeActivation(\"" + types.size() + "\");'/></p>");
		
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("</div>");
		
		return html.toString();
	}
}
