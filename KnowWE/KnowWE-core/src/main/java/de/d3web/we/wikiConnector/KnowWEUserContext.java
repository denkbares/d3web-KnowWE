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

package de.d3web.we.wikiConnector;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Context of the current wiki user and its current wiki context, such as
 * current page, current web. You may also use this class to access the current
 * request details.
 *
 * @author Jochen Reutelshöfer, volker_belli
 * @created 14.10.2010
 */
public interface KnowWEUserContext {

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
	public boolean userIsAuthenticated();

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
	 * @created 14.10.2010
	 * @return the article's topic
	 */
	public String getTopic();

	/**
	 * Returns the web of the user's is currently visiting. It is the web the
	 * article belongs to.
	 *
	 * @created 14.10.2010
	 * @return the article's web
	 */
	public String getWeb();

	/**
	 * Returns the http request with which the user is currently accessing the
	 * wiki server.
	 *
	 * @created 14.10.2010
	 * @return the user's http request
	 */
	public HttpServletRequest getHttpRequest();

	/**
	 * Returns the parameter map of the http request with which the user is
	 * currently accessing the wiki server.
	 *
	 * @created 14.10.2010
	 * @return the user's http request parameters
	 */
	public Map<String, String> getUrlParameterMap();

}
