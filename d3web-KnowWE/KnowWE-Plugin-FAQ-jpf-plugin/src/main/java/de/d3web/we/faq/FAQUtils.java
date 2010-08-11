/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.faq;

import de.d3web.we.utils.KnowWEUtils;

/**
 * Class for collecting some useful global helper functions
 * 
 * @author M. Freiberg
 * @created 09.08.2010
 */
public class FAQUtils {

	/**
	 * replaces the link-markup within a FAQ answer text "-L- -/L-" with correct
	 * HTML linking markup
	 * 
	 * @created 03.08.2010
	 * @param a the String potentially containing the link
	 * @return the correctly HTML-marked-up answer string
	 */
	public static String resolveLinks(String a) {
		if (a.isEmpty() || a.equals(" ") || !a.contains("[")) {
			return a;
		}
		String complete = "";
		String link = "";
		String linktext = "";

		while (a.contains("[")) {

			complete = a.substring(a.indexOf("[") + 1, a.indexOf("]"));
			if (a.contains("|")) {
				link = a.substring(a.indexOf("[") + 1, a.indexOf("|"));
				linktext = a.substring(a.indexOf("|") + 1, a.indexOf("]"));
			}
			else {
				link = complete;
				linktext = complete;
			}

			if (a.contains(".pdf")) {
				a = a.replace(complete, "<b><a href=\"/KnowWE/attach/FAQ%20Entry/" + link + "\">"
						+ linktext + "</a></b>");
			}
			else {
				a = a.replace(complete, "<b><a href=\"" + link + "\">" + linktext + "</a></b>");
			}
			a = a.replaceFirst("\\[", "");
			a = a.replaceFirst("\\]", "");
		}
		return a;
	}

	/**
	 * Generates the String representation of the content of one FAQ entry
	 * 
	 * @created 09.08.2010
	 * @param question
	 * @param answer
	 * @param status
	 * @param major
	 * @return
	 */
	public static String renderFAQPluginInner(String question, String answer, String status, String major) {
		StringBuilder string = new StringBuilder();

		string.append("<div class='faq_question'> Q: ");
		string.append(question);
		string.append("</div>");
		string.append("<div class='faq_answer'> ");
		string.append(answer);
		string.append("<div class='faq_tags'> ");
		string.append(status);
		string.append(" ");
		string.append(major);
		string.append("</div>");
		string.append("</div>");

		return string.toString();
	}

	/**
	 * Generates the frame for rendering the FAQ plugin within an own panel into
	 * the wiki site
	 * 
	 * @created 13.07.2010
	 * @return
	 */
	public static String renderFAQPluginFrame(String pluginInner) {
		StringBuilder string = new StringBuilder();
		string.append(KnowWEUtils.maskHTML("<div class='panel'>"));
		string.append(KnowWEUtils.maskHTML("<h3>FAQ Plugin</h3>"));
		string.append(KnowWEUtils.maskHTML(pluginInner));
		string.append(KnowWEUtils.maskHTML("</div>"));
		return string.toString();
	}

	/**
	 * Generates the HTML representation for rendering the category (e.g., A, B,
	 * ... Z) of the FAQ entries
	 * 
	 * @created 09.08.2010
	 * @param cat
	 * @return
	 */
	public static String printCategory(FAQCats cat) {
		StringBuilder string = new StringBuilder();
		string.append("<div class=\"cat\">");

		if (cat.toString().equals("NUM")) {
			string.append("<a name=\"" + cat.toString() + "\">0...9</a>");
		}
		else {
			string.append("<a name=\"" + cat.toString() + "\">"
					+ cat.toString() + "</a>");
		}

		string.append("</div> <br />");
		return string.toString();
	}

	/**
	 * Generates the HTML displaying the anchor links to the FAQ categories in
	 * the top of the page
	 * 
	 * @created 10.08.2010
	 * @return
	 */
	public static String renderCategoriesAnchorLinks() {
		StringBuilder string = new StringBuilder();
		for (FAQCats cat : FAQCats.values()) {
			if (cat.toString().equals("NUM")) {
				string.append("<a class=\"cattop\" href=\"#" + cat.toString() + "\">0...9</a>");
			}
			else {
				string.append("<a class=\"cattop\" href=\"#" + cat.toString() + "\">"
						+ cat.toString() + "</a>");
			}
		}
		string.append("<p />");
		return string.toString();
	}
}
