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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.search.Result;
import de.d3web.we.kdom.search.SearchEngine;
import de.d3web.we.kdom.search.SearchOption;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ObjectInfoTagHandler extends AbstractTagHandler {
	
	private String topic;
	private KnowWEUserContext user;
	
	private ResourceBundle rb;
	
	public ObjectInfoTagHandler() {
		super("ObjectInfo");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values,
			String web) {
		
		StringBuffer buffy = new StringBuffer();
		
		rb = D3webModule.getKwikiBundle_d3web(user);
		
		Map<String, String> urlParameterMap = user.getUrlParameterMap();
		String object = urlParameterMap.get("objectname");
		String type = urlParameterMap.get("objecttype");
		boolean fuzzy = false;
		
		if (urlParameterMap.get("fuzzy") != null)
			fuzzy = urlParameterMap.get("fuzzy").equals("on");
		
		String sortOrder;
		
		if (urlParameterMap.get("order") != null)
			sortOrder = urlParameterMap.get("order");
		else
			sortOrder = "default";
		
		this.topic = topic;
		this.user = user;
		
		if (object == null) {
			buffy.append(doOverview());
		} else {
			buffy.append("<div id=\"objectinfo-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.ObjectInfoTagHandler.info_for") + " <em>" + html_escape(object) + "</em></h3>");
			buffy.append(rb.getString("KnowWE.ObjectInfoTagHandler.newPage")+" <a href='Edit.jsp?page="+object+"'>"+object+"</a>");
			buffy.append(doObject(object, web, type, fuzzy, sortOrder));
			buffy.append("</div>");
		}
		
		
		return buffy.toString();
	}
	
	private String doOverview() {
		StringBuffer buffy = new StringBuffer();
		
		buffy.append("<div id=\"objectinfo-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.ObjectInfoTagHandler.look_up") + "</em></h3>");

		
		
		buffy.append("<form action=\"\" method=\"get\">");
		buffy.append("<input type=\"hidden\" name=\"page\" value=\"" + urlencode(topic) + "\" />");
		buffy.append("<input type=\"text\" name=\"objectname\" /> ");
		buffy.append("<input type=\"submit\" value=\"&rarr;\" />");
		buffy.append("</form>");
		
		buffy.append("</div>");
	
		return buffy.toString();
	}
	
	private String doObject(String obj, String web, String type, boolean fuzzy, String order) {
		Map<KnowWEArticle, Collection<Result>> results;
		SearchEngine se = new SearchEngine(KnowWEEnvironment.getInstance().getArticleManager(web));
		
		if (fuzzy)
			se.setOption(SearchOption.FUZZY);
		
		StringBuffer buffy = new StringBuffer("");
		
		results = se.search(obj);

		
		buffy.append("<div style=\"width: 15em; float: right;\"><h4>" + rb.getString("KnowWE.ObjectInfoTagHandler.settings") + "</h4>");
		buffy.append("<form action=\"\" method=\"get\">");
		buffy.append("<input type=\"hidden\" name=\"page\" value=\"" + urlencode(topic) + "\" />");
		buffy.append("<input type=\"hidden\" name=\"objectname\" value=\"" + urlencode(obj) + "\" />");
		buffy.append("<input type=\"hidden\" name=\"fuzzy\" value=\"" + (fuzzy ? "off" : "on") + "\" />");
		buffy.append("<input type=\"submit\" value=\"" + rb.getString("KnowWE.ObjectInfoTagHandler.fuzzy." + (fuzzy ? "disable" : "enable")) + "\" />");
		buffy.append("</form>");
		
		buffy.append("<form action=\"\" method=\"get\">");
		buffy.append("<input type=\"hidden\" name=\"page\" value=\"" + urlencode(topic) + "\" />");
		buffy.append("<input type=\"hidden\" name=\"objectname\" value=\"" + urlencode(obj) + "\" />");
		buffy.append("<input type=\"hidden\" name=\"fuzzy\" value=\"" + (fuzzy ? "on" : "off") + "\" />");
		buffy.append("<input type=\"hidden\" name=\"order\" value=\"" + (order.equals("default") ? "type" : "default") + "\" />");
		buffy.append("<input type=\"submit\" value=\"" + rb.getString("KnowWE.ObjectInfoTagHandler.order." + (order.equals("default") ? "type" : "occurance")) + "\" />");
		buffy.append("</form>");
		
		buffy.append("</div>");
		
		buffy.append("<h4>" + rb.getString("KnowWE.ObjectInfoTagHandler.overview") + "</h4>");
		buffy.append("<p>" + rb.getString("KnowWE.ObjectInfoTagHandler.found_msg").replace("{0}", "<strong>" + results.size() + "</strong>"));

		Iterator<KnowWEArticle> iter = results.keySet().iterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			String articleTitle = html_escape(article.getTitle());
			
			buffy.append(" <a href=\"#" + articleTitle + "-box\">" + articleTitle + "</a> (" + results.get(article).size() + ")" + (iter.hasNext() ? ", " : "")); 
		}
		
		buffy.append("</p>");

		buffy.append("<br style=\"clear: both;\" />");
		
		buffy.append("<div>");
		
		iter = results.keySet().iterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			String articleTitle = html_escape(article.getTitle());
			
			buffy.append("<div id=\"" + articleTitle + "-box\" class=\"panel\"><h3>" + rb.getString("KnowWE.ObjectInfoTagHandler.on") + " <em>" + articleTitle + "</em></h3>");
			buffy.append("<p>" + rb.getString("KnowWE.ObjectInfoTagHandler.goto") + " <strong><a href=\"Wiki.jsp?page=" + articleTitle + "\">" + articleTitle + "</a></strong></p>\n");
			buffy.append("<table><tr><th>" + rb.getString("KnowWE.ObjectInfoTagHandler.class") + "</th><th stlye=\"width: 50%;\">" + rb.getString("KnowWE.ObjectInfoTagHandler.preview") + "</th></tr>\n");
			
			List<Result> items = new LinkedList<Result>(results.get(article));
			
			if (order.equals("type")) {
				Collections.sort(items);
			}
			
			for (Result r : items) {
				StringBuilder b = new StringBuilder();
				r.getSection().getObjectType().getRenderer().render(article, r.getSection(), user, b);
				
				if (fuzzy) {
					buffy.append("<tr><td><strong>" + r.getSection().getObjectType().getName() + "</strong></td>" +
								 "<td><pre>" + b.toString() + "</pre></td></tr>\n");
				} else {
					buffy.append("<tr>\n"
							+ "<td><strong>" + r.getSection().getObjectType().getName() + "</strong></td>\n"		
							+ "<td><code class=\"box\">\n"
							+ "<span style=\"color: #333;\">&hellip;" + html_escape(r.getAdditionalContext(-30).replaceAll("\n", " ")) + "</span>" 
							+ b.toString() 
							+ "<span style=\"color: #333;\">" + html_escape(r.getAdditionalContext(30).replaceAll("\n", " ")) + "&hellip;</span>" 
							+ "</code></td></tr>\n");
				}
			}
			
			buffy.append("</table></div>\n");
		}
		
		buffy.append("</div>");
		return buffy.toString();
	}
	
	/**
	 * Escapes the given string for safely using user-input in web sites.
	 * @param text Text to escape
	 * @return Sanitized text
	 */
	private String html_escape(String text) {
		if (text == null)
			return null;

		return text.replaceAll("&", "&amp;").
					replaceAll("\"", "&quot;").
					replaceAll("<", "&lt;").
					replaceAll(">", "&gt;");
	}
	
	private String urlencode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(text);
		}
	}


}
