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

package de.d3web.we.wikiConnector;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.javaEnv.KnowWETopicLoader;

public interface KnowWEWikiConnector {
	
	public String getRealPath();
	
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
	
	public boolean userCanEditPage( String articlename );
	
	public boolean userCanEditPage( String articlename, HttpServletRequest r );
	
	public boolean isPageLocked( String articlename );
	
	public boolean setPageLocked( String articlename, String user );
	
	public void undoPageLocked( String articlename );
	
	public boolean isPageLockedCurrentUser( String articlename, String user );
	
	public Locale getLocale();
	
	public Locale getLocale( HttpServletRequest request );

}
