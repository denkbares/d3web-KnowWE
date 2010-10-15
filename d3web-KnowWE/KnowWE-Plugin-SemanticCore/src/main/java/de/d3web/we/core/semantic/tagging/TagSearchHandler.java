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

package de.d3web.we.core.semantic.tagging;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagSearchHandler extends AbstractHTMLTagHandler {

	private ResourceBundle rb;

	public TagSearchHandler() {
		super("tagsearch");

	}

	private String getForm(String baseurl) {
		// FIXME refactor TagSearch page somewhere else
		String form = "<form action=\""
				+ baseurl
				+ "Wiki.jsp"
				+ "?page=TagSearch\" "
				+ "class=\"wikiform\" "
				+ "id=\"searchform2\" name=\"searchform2\" accept-charset=\"UTF-8\">"
				+ "<p>"
				+ "<input type=\"hidden\" name=\"page\" id=\"page\" value=\"TagSearch\" />"
				+
						"<input type=\"text\" name=\"query\" id=\"tagquery\" value=\"\" size=\"32\" />"
				+ "<input type=\"submit\" name=\"ok\" id=\"ok\" value=\"Find!\" />"
				+ "<input type=\"submit\" name=\"go\" id=\"go\" value=\"Go!\" />"
				+ "<input type=\"hidden\" name=\"start\" id=\"start\" value=\"0\" />"
				+ "<input type=\"hidden\" name=\"maxitems\" id=\"maxitems\" value=\"20\" />"
				+

				"<span id=\"spin\" class=\"spin\" style=\"position:absolute;display:none;\"></span>"
				+ "</p>" + "</form>";
		return form;

	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		rb = KnowWEEnvironment.getInstance()
				.getKwikiBundle(user);
		String baseurl = KnowWEEnvironment.getInstance().getWikiConnector()
				.getBaseUrl();

		String querystring = null;
		Map<String, String> par = user.getUrlParameterMap();
		if (par != null) {
			String parameterName = "query";
			if (par.containsKey(parameterName)) {
				querystring = par.get(parameterName);
			}
		}
		StringBuffer html = new StringBuffer();
		html.append("<script type=\"text/javascript\" src=\"" + baseurl
				+ "KnowWEExtension/scripts/tagsearch.js\"></script>");
		html.append("<div class=\"panel\"><h3>"
				+ rb.getString("KnowWE.TagSearch.headline") + ": "
				+ (querystring == null ? "none" : querystring) + "</h3>");
		html.append(getForm(baseurl));
		html.append("<div id=\"searchResult2\" >");
		html.append(TaggingMangler.getInstance().getResultPanel(querystring));

		html.append("</div>");
		html.append("</div>");
		return html.toString();

	}

}
