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

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

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
	boolean userIsAdmin();

	/**
	 * Returns whether the user is authenticated or not.
	 *
	 * @created 01.02.2011
	 */
	boolean userIsAsserted();

	/**
	 * Returns the name of the current user.
	 *
	 * @created 14.10.2010
	 */
	String getUserName();

	/**
	 * Returns the preferred locale to render contents for.
	 */
	default Locale getLocale() {
		return KnowWEUtils.getBrowserLocales(getRequest())[0];
	}

	/**
	 * Returns the title of the article the user is currently visiting.
	 *
	 * @created 14.10.2010
	 */
	String getTitle();

	/**
	 * Returns the Article the user is currently visiting.
	 *
	 * @created 14.10.2010
	 */
	Article getArticle();

	/**
	 * Returns the web of the user's is currently visiting. It is the web the article belongs to.
	 *
	 * @created 14.10.2010
	 */
	String getWeb();

	/**
	 * Returns the parameter map of the http request with which the user is currently accessing the wiki server.
	 *
	 * @created 14.10.2010
	 */
	Map<String, String> getParameters();

	/**
	 * Returns the parameter of the http request with the specified key.
	 *
	 * @param key key for the parameter
	 * @created Mar 4, 2011
	 */
	String getParameter(String key);

	/**
	 * Returns the parameter of the http request with the specified key. If this parameter is not available the default
	 * value should be returned.
	 *
	 * @param key          key for the parameter
	 * @param defaultValue the default value for the parameter
	 * @created Mar 4, 2011
	 */
	String getParameter(String key, String defaultValue);

	/**
	 * Returns the http request with which the user is currently accessing the wiki server.
	 *
	 * @created 14.10.2010
	 */
	HttpServletRequest getRequest();

	/**
	 * Returns the user's current http session
	 *
	 * @created Mar 4, 2011
	 */
	HttpSession getSession();

	/**
	 * Returns the servlet context if available.
	 *
	 * @created Mar 4, 2011
	 */
	ServletContext getServletContext();

	/**
	 * Returns the ArticleManager belonging to this context
	 *
	 * @created 14.01.2014
	 */
	ArticleManager getArticleManager();

	/**
	 * Returns if asynchronous rendering should be executed
	 *
	 * @return true if asynchronous rendering shall be executed, false otherwise
	 */
	boolean allowAsynchronousRendering();

	/**
	 * Returns if a preview version is rendered when editing article
	 *
	 * @return true, if preview is rendered, false otherwise
	 */
	boolean isRenderingPreview();
}
