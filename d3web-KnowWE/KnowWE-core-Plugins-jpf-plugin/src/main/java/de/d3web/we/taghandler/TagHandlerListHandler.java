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

package de.d3web.we.taghandler;

import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Creates a list of all TagHandlers mentioned in the Hash-map
 * knowWEDefaultTagHandlers.
 * 
 * @author Max Diez
 * 
 */
public class TagHandlerListHandler extends AbstractHTMLTagHandler {

	public TagHandlerListHandler() {
		super("taghandlerlist");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		Collection<TagHandler> coll = KnowWEEnvironment.getInstance()
				.getDefaultTagHandlers().values();

		ResourceBundle rb = KnowWEEnvironment.getInstance()
				.getKwikiBundle(user);

		StringBuffer html = new StringBuffer();
		html.append("<div id='taghandlerlist-panel' class='panel'>" + "<h3>"
				+ rb.getString("KnowWE.TagHandlerList.header") + "</h3> ");
		html.append("<div class=\"sortable\">");
		html.append("<table class=\"wikitable\" border=\"1\">");
		html.append("<tbody>");

		// html.append("<tr class=\"odd\">");

		// html.append("<table width=100% border=1>");
		html.append("<TR><TH class=\"sort\">"
				+ rb.getString("KnowWE.TagHandlerList.table.name")
				+ "</TH><TH class=\"sort\">"
				+ rb.getString("KnowWE.TagHandlerList.table.example")
				+ "</TH><TH class=\"sort\">"
				+ rb.getString("KnowWE.TagHandlerList.table.description")
				+ "</TH>");
		for (TagHandler th : coll) {
			String name = "no name available";
			String example = "no example available";
			String description = "no description available";
			try {
				name = th.getTagName();
				example = th.getExampleString();
				description = th.getDescription(user);
			}
			catch (Exception e) {
				description = "Fehler";
				Logger.getLogger(this.getClass().getName()).warning(
						"Unable to get description for TagHandler: " + th.getClass() + " ("
								+ e.getMessage() + ")");
			}
			html.append("<TR><TD>" + name + "</TD><TD>" + example + "</TD><TD>"
					+ description + "</TD></TR> \n"); // \n only to avoid
			// hmtl-code being cut by
			// JspWiki (String.length
			// > 10000)
		}
		html.append("</tbody></table></div></div>");
		return html.toString();
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
				"KnowWE.TagHandlerList.description");
	}
}
