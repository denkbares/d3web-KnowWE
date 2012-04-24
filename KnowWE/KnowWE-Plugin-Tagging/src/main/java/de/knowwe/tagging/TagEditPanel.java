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

import de.knowwe.core.Attributes;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class TagEditPanel extends AbstractHTMLTagHandler {

	public TagEditPanel() {
		super("tageditpanel");
	}

	@Override
	public String renderHTML(String topic, UserContext user,
			Map<String, String> values, String web) {
		return render(user);
	}

	public static String render(UserContext user) {
		TaggingMangler tm = TaggingMangler.getInstance();
		List<String> tags = tm.getPageTags(user.getTitle());
		String output = "<p>";
		output += "Tags (<span id=\"tagpanedit\" style='text-decoration:underline;'>edit</span>):";
		output += "<span id=\"tagspan\">";
		if (tags != null) {
			for (String cur : tags) {
				// output += cur + " ";
				output += " <a href =\"Wiki.jsp?page=TagSearch&query=" + cur
						+ "&ok=Find!&start=0&maxitems=20\" >" + cur + "</a>";

			}
		}

		if (output.trim().length() == 0) {
			output += "none";
		}
		output += "</span>";
		output += "<script type=\"text/javascript\" src=\"KnowWEExtension/scripts/silveripe.0.2.js\"></script>";
		output += "<script type=\"text/javascript\">";
		output += "var myIPE=new SilverIPE('tagpanedit','tagspan','KnowWE.jsp',{parameterName:'tagtag',highlightColor: '#ffff77',"
				+ "additionalParameters:{tagaction:\"set\",action:\"TagHandlingAction\","
				+ Attributes.TOPIC + ":\"" + user.getTitle() + "\"} });";
		output += "</script>";
		output += "</p>";
		return KnowWEUtils.maskHTML(output);
	}

}
