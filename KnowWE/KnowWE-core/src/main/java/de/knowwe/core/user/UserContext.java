/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.user;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;

/**
 * UserContext which represents the users interaction with the server.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 4, 2011
 */
public interface UserContext {

	/**
	 * Returns whether the user has administration rights.
	 * 
	 * @created 14.10.2010
	 */
	public boolean userIsAdmin();

	/**
	 * Returns whether the user is authenticated or not.
	 * 
	 * @created 01.02.2011
	 */
	public boolean userIsAsserted();

	/**
	 * Returns the name of the current user.
	 * 
	 * @created 14.10.2010
	 */
	public String getUserName();

	/**
	 * Returns the title of the article the user is currently visiting.
	 * 
	 * @created 14.10.2010 s
	 */
	public String getTitle();

	/**
	 * Returns the Article the user is currently visiting.
	 * 
	 * @created 14.10.2010 s
	 */
	public Article getArticle();

	/**
	 * Returns the web of the user's is currently visiting. It is the web the
	 * article belongs to.
	 * 
	 * @created 14.10.2010
	 */
	public String getWeb();

	/**
	 * Returns the parameter map of the http request with which the user is
	 * currently accessing the wiki server.
	 * 
	 * @created 14.10.2010
	 */
	public Map<String, String> getParameters();

	/**
	 * Returns the parameter of the http request with the specified key.
	 * 
	 * @created Mar 4, 2011
	 * @param key key for the parameter
	 */
	public String getParameter(String key);

	/**
	 * Returns the parameter of the http request with the specified key. If this
	 * parameter is not available the default value should be returned.
	 * 
	 * @created Mar 4, 2011
	 * @param key key for the parameter
	 * @param defaultValue the default value for the parameter
	 */
	public String getParameter(String key, String defaultValue);

	/**
	 * Returns the http request with which the user is currently accessing the
	 * wiki server.
	 * 
	 * @created 14.10.2010
	 */
	public HttpServletRequest getRequest();

	/**
	 * Returns the user's current http session
	 * 
	 * @created Mar 4, 2011
	 */
	public HttpSession getSession();

	/**
	 * Returns the servlet context if available.
	 * 
	 * @created Mar 4, 2011
	 */
	public ServletContext getServletContext();

	/**
	 * Returns the ArticleManager belonging to this context
	 * 
	 * @created 14.01.2014
	 */
	public ArticleManager getArticleManager();

}
