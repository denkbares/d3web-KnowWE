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

package de.knowwe.tagging;

import java.util.List;
import java.util.Map;

import com.denkbares.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;

public class TagEditPanel extends AbstractHTMLTagHandler {

	public TagEditPanel() {
		super("tageditpanel");
	}

	@Override
	public void renderHTML(String web, String topic,
			UserContext user, Map<String, String> values, RenderResult result) {
		render(user, result);
	}

	public static void render(UserContext user, RenderResult result) {
		TaggingMangler tm = TaggingMangler.getInstance();
		List<String> tags = tm.getPageTags(user.getTitle());
		RenderResult output = new RenderResult(result);
		output.appendHtml("<p>");
		output.appendHtml("Tags (<span id=\"tagpanedit\" style='text-decoration:underline;'>edit</span>):");
		output.appendHtml("<span id=\"tagspan\">");
		if (tags != null) {
			for (String cur : tags) {
				// output += cur + " ";
				output.appendHtml(" <a href =\"Wiki.jsp?page=TagSearch&query="
						+ Strings.encodeHtml(cur)
						+ "&ok=Find!&start=0&maxitems=20\" >");
				output.append(cur);
				output.appendHtml("</a>");

			}
		}

		if (output.toStringRaw().trim().isEmpty()) {
			output.append("none");
		}
		output.appendHtml("</span>");
		output.appendHtml("<script type=\"text/javascript\" src=\"KnowWEExtension/scripts/silveripe.0.2.js\"></script>");
		output.appendHtml("<script type=\"text/javascript\">");
		output.appendHtml("var myIPE=new SilverIPE('tagpanedit','tagspan','KnowWE.jsp',{parameterName:'tagtag',highlightColor: '#ffff77',"
				+ "additionalParameters:{tagaction:\"set\",action:\"TagHandlingAction\","
				+ Attributes.TOPIC + ":\"" + user.getTitle() + "\"} });");
		output.appendHtml("</script>");
		output.appendHtml("</p>");
	}

}
