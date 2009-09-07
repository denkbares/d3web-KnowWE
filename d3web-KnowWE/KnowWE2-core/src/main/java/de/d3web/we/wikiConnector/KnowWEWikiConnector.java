package de.d3web.we.wikiConnector;

import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletContext;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.javaEnv.KnowWETopicLoader;

public interface KnowWEWikiConnector {
	
	public ServletContext getServletContext();
	
	public String getPagePath();	

	public KnowWETopicLoader getLoader();

	public KnowWEActionDispatcher getActionDispatcher();
	
	public boolean saveArticle(String name, String text, KnowWEParameterMap map);
	
	public LinkedList<String> getAttachments();
	
	public String getBaseUrl();
	
	public String getAttachmentPath(String JarName);
	
	public String getArticleSource(String name);
	
	public boolean doesPageExist(String Topic);
	
	public String createWikiPage(String topic, String newContent, String author);

	public String appendContentToPage(String topic, String pageContent);
	
	public Map<String,String> getAllArticles(String web);
}
