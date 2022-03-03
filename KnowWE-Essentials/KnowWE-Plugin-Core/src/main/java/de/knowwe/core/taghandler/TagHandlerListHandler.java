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

package de.knowwe.core.taghandler;

import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

/**
 * Creates a list of all TagHandlers mentioned in the Hash-map
 * knowWEDefaultTagHandlers.
 * 
 * @author Max Diez
 * 
 */
public class TagHandlerListHandler extends AbstractHTMLTagHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagHandlerListHandler.class);

	public TagHandlerListHandler() {
		super("taghandlerlist");
	}

	@Override
	public void renderHTML(String web, String topic,
			UserContext user, Map<String, String> values, RenderResult result) {
		Collection<TagHandler> coll = Environment.getInstance()
				.getDefaultTagHandlers().values();

		ResourceBundle rb = Messages.getMessageBundle(user);

		StringBuilder html = new StringBuilder();
		html.append("<div id='taghandlerlist-panel' class='panel'>" + "<h3>")
				.append(rb.getString("KnowWE.TagHandlerList.header"))
				.append("</h3> ");
		html.append("<div class=\"sortable\">");
		html.append("<table class=\"wikitable\" border=\"1\">");
		html.append("<tbody>");

		// html.append("<tr class=\"odd\">");

		// html.append("<table width=100% border=1>");
		html.append("<TR><TH class=\"sort\">")
				.append(rb.getString("KnowWE.TagHandlerList.table.name"))
				.append("</TH><TH class=\"sort\">")
				.append(rb.getString("KnowWE.TagHandlerList.table.example"))
				.append("</TH><TH class=\"sort\">")
				.append(rb.getString("KnowWE.TagHandlerList.table.description"))
				.append("</TH>");
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
				LOGGER.warn("Unable to get description for TagHandler " + th.getClass(), e);
			}
			html.append("<TR><TD>")
					.append(name)
					.append("</TD><TD>")
					.append(example)
					.append("</TD><TD>")
					.append(description)
					.append("</TD></TR> \n"); // \n only to avoid
			// hmtl-code being cut by
			// JspWiki (String.length
			// > 10000)
		}
		html.append("</tbody></table></div></div>");
		result.appendHtml(html.toString());
	}

	@Override
	public String getDescription(UserContext user) {
		return Messages.getMessageBundle(user).getString(
				"KnowWE.TagHandlerList.description");
	}
}
