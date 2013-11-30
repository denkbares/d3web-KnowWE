/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.include;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.ToolSet;

/**
 * 
 * @author jochenreutelshofer
 * @created 11.04.2013
 */
public class IncludeDefaultMarkupRenderer extends DefaultMarkupRenderer {

	@Override
	public void renderDefaultMarkupStyled(String title,
			String content,
			String sectionID,
			String cssClassName,
			ToolSet tools,
			UserContext user,
			RenderResult string) {

		String cssClass = "defaultMarkupFrame";
		if (cssClassName != null) cssClass += " " + cssClassName;
		string.appendHtml("<div id=\"" + sectionID + "\" class='" + cssClass
				+ "'>\n");

		appendHeader(title, sectionID, tools, user, string);

		// render pre-formatted box
		string.appendHtml("<div id=\"box_" + sectionID
				+ "\" class='defaultMarkup'>");
		string.appendHtml("<div id=\"content_" + sectionID
				+ "\" class='markupText'>");

		string.append(content);

		// and close the box(es)
		string.appendHtml("</div>"); // class=markupText
		string.appendHtml("</div>"); // class=defaultMarkup
		string.appendHtml("</div>");

	}

}
