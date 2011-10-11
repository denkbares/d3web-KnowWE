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
	 * @return user is an admin or not
	 */
	public boolean userIsAdmin();

	/**
	 * Returns whether the user is authenticated or not.
	 * 
	 * @created 01.02.2011
	 * @return boolean authentication state of the user
	 */
	public boolean userIsAsserted();

	/**
	 * Returns the name of the current user.
	 * 
	 * @created 14.10.2010
	 * @return the user name
	 */
	public String getUserName();

	/**
	 * Returns the topic of the article the user is currently visiting.
	 * 
	 * @deprecated improper naming, use {@link UserContext#getTitle()} instead.
	 * @created 14.10.2010
	 * @return the article's topic
	 */
	@Deprecated
	public String getTopic();

	/**
	 * Returns the title of the article the user is currently visiting.
	 * 
	 * @created 14.10.2010
	 * @return the article's title
	 */
	public String getTitle();

	/**
	 * Returns the web of the user's is currently visiting. It is the web the
	 * article belongs to.
	 * 
	 * @created 14.10.2010
	 * @return the article's web
	 */
	public String getWeb();

	/**
	 * Returns the parameter map of the http request with which the user is
	 * currently accessing the wiki server.
	 * 
	 * @created 14.10.2010
	 * @return the user's http request parameters
	 */
	public Map<String, String> getParameters();

	/**
	 * Returns the parameter of the http request with the specified key.
	 * 
	 * @created Mar 4, 2011
	 * @param key key for the parameter
	 * @return parameter of the user's http request if available
	 */
	public String getParameter(String key);

	/**
	 * Returns the parameter of the http request with the specified key. If this
	 * parameter is not available the default value should be returned.
	 * 
	 * @created Mar 4, 2011
	 * @param key key for the parameter
	 * @param defaultValue the default value for the parameter
	 * @return parameter of the user's http request if available, otherwise the
	 *         default value
	 */
	public String getParameter(String key, String defaultValue);

	/**
	 * Returns the http request with which the user is currently accessing the
	 * wiki server.
	 * 
	 * @created 14.10.2010
	 * @return the user's http request
	 */
	public HttpServletRequest getRequest();

	/**
	 * Returns the user's current http session
	 * 
	 * @created Mar 4, 2011
	 * @return the user's http session
	 */
	public HttpSession getSession();

	/**
	 * Returns the servlet context if available.
	 * 
	 * @created Mar 4, 2011
	 * @return the servlet context
	 */
	public ServletContext getServletContext();

}
