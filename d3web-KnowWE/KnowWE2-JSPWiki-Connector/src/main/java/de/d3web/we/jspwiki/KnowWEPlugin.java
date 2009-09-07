package de.d3web.we.jspwiki;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.filters.BasicPageFilter;
import com.ecyrd.jspwiki.filters.FilterException;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;

public class KnowWEPlugin extends BasicPageFilter implements WikiPlugin {

	private String topicName = "";

	private void initKnowWEEnvironmentIfNeeded(WikiEngine wEngine) {
		if (!KnowWEEnvironment.isInitialized()) {
			KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wEngine));

		}
	}

	public String execute(WikiContext context, Map params)
			throws PluginException {
		String result = "tag not found or error";

		try {
			JSPWikiUserContext userContext = new JSPWikiUserContext(context, parseRequestVariables(context));
			
			String topic = context.getPage().getName();
			if (context.getCommand().getRequestContext().equals(
					WikiContext.VIEW)) {
				initKnowWEEnvironmentIfNeeded(context.getEngine());
				result = KnowWEEnvironment.getInstance().renderTags(params, topic, userContext,
						KnowWEEnvironment.DEFAULT_WEB);
			}
		} catch (Throwable t) {
			System.out.println("****Exception EXECUTE***");
			System.out.println("****Exception EXECUTE***");
			System.out.println("****Exception EXECUTE***");
			t.printStackTrace();
		}

		return result;
		// return "KnowWEplugin";
	}

	@Override
	public void postSave(WikiContext wikiContext, String content)
			throws FilterException {
		// setWikiContextAndEngine(wikiContext);

		// get the Page-name
		String topic = wikiContext.getPage().getName();
		String user = wikiContext.getWikiSession().getUserPrincipal().getName();

		initKnowWEEnvironmentIfNeeded(wikiContext.getEngine());

		// process this article in KnowWE
		KnowWEEnvironment.getInstance().processAndUpdateArticle(user, content, topic,
				"default_web");
	}

	@Override
	public String postTranslate(WikiContext wikiContext, String htmlContent)
			throws FilterException {

		try {

			htmlContent = KnowWEEnvironment.unmaskHTML(htmlContent);

			return htmlContent;
		} catch (Exception e) {
			System.out.println("****Exception in POST TRANSLATE***");
			System.out.println("****Exception in POST TRANSLATE***");
			System.out.println("****Exception in POST TRANSLATE***");
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String preTranslate(WikiContext wikiContext, String content)
			throws FilterException {
		// setWikiContextAndEngine(wikiContext);

		if (!wikiContext.getCommand().getRequestContext().equals(
				WikiContext.VIEW)) {
			return content;
		}
		String pagedata = "";
		WikiEngine engine = wikiContext.getEngine();
		if (engine != null) {
			pagedata = engine.getPureText(wikiContext.getPage().getName(),
					wikiContext.getPage().getVersion());
			if (!content.equals(pagedata))
				return content;
		}
		try {
			
			/* creating KnowWEUserContext with username and requestParamteters */
			JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext, parseRequestVariables(wikiContext));
			
			topicName = wikiContext.getPage().getName();
			
			initKnowWEEnvironmentIfNeeded(engine);

			String newContent = "articleContent";

			KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(
					KnowWEEnvironment.DEFAULT_WEB, topicName);
			if (article != null) {
				String originalText = article.getSection().getOriginalText();
				if (!originalText.equals(content)) {
					article = new KnowWEArticle(content, topicName,
							KnowWEEnvironment.getInstance().getRootTypes(),KnowWEEnvironment.DEFAULT_WEB);
				}
			} else {
				article = new KnowWEArticle(content, topicName,
						KnowWEEnvironment.getInstance().getRootTypes(),KnowWEEnvironment.DEFAULT_WEB);
				if (pagedata.endsWith(content)) {
					// INITIALISATION PHASE: when page is first requested
					// after
					// server-start article is stored in manager
					KnowWEEnvironment.getInstance().getArticleManager(
							"default_web").saveUpdatedArticle(article);
				}
			}
			newContent = article.getRenderer().render(article.getSection(),
					userContext, "default_web", topicName);

			return newContent;
		} catch (Exception e) {
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses the request variables (GET and POST) using a wiki context object.
	 * @param context WikiContext to be used
	 * @return A Map containing all request variables
	 */
	private Map<String, String> parseRequestVariables(WikiContext context) {
		HttpServletRequest req = context.getHttpRequest();
		
		Map<String, String> parameter = new HashMap<String, String>();
		
		if (req == null)
			return parameter;
		
		Enumeration p = req.getParameterNames();
		
		while (p.hasMoreElements()) {
			String param = (String) p.nextElement();
			try {
				parameter.put(param, URLDecoder.decode((String) req.getParameter(param), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				parameter.put(param, (String) req.getParameter(param));
			} 
		}
		
		return parameter;				
	}

}
