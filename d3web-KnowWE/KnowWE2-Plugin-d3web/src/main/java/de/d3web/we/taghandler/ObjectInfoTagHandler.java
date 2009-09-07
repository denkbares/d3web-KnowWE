package de.d3web.we.taghandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.search.Result;
import de.d3web.we.search.SearchEngine;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ObjectInfoTagHandler extends AbstractTagHandler {

	public ObjectInfoTagHandler() {
		super("ObjectInfo");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value,
			String web) {
		
		StringBuffer buffy = new StringBuffer();
		
		Map<String, String> urlParameterMap = user.getUrlParameterMap();
		String object = urlParameterMap.get("objectname");
		String type = urlParameterMap.get("objecttype");
		
		if (object == null) {
			buffy.append(doOverview());
		} else {
			buffy.append("<div id=\"objectinfo-panel\" class=\"panel\"><h3>Object information for <em>" + html_escape(object) + "</em></h3>");
			buffy.append(doObject(object, web, type));
			buffy.append("</div>");
		}
		
		
		return buffy.toString();
	}
	
	private String doOverview() {
		return "no object :(";
	}
	
	private String doObject(String obj, String web, String type) {
		Map<KnowWEArticle, Collection<Result>> results;
		SearchEngine se = KnowWEEnvironment.getInstance().getArticleManager(web).getSearchEngine();
		StringBuffer buffy = new StringBuffer("<ul>");
		
		results = se.search(obj);
		
		Iterator<KnowWEArticle> iter = results.keySet().iterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			String articleTitle = html_escape(article.getTitle());		
			
			buffy.append("<li>On <strong><a href=\"Wiki.jsp?page=" + articleTitle + "\">" + articleTitle + "</a></strong>: \n");
			buffy.append("<ul>\n");
			
			for (Result r : results.get(article)) {
				buffy.append("<li><strong>" + r.getSection().getObjectType().getClass().getName() + "</strong>:<br />" +
							 "<pre>" + html_escape(r.getSection().getOriginalText()) + "</pre>" + 
							  "</li>\n");
			}
		}
		
		buffy.append("</ul>");
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


}
