/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import de.d3web.we.user.UserContext;

/**
 * UserActionContext interface which is a specialisied UserContext interface for
 * Actions.
 *
 * The UserActionContext of the execute method provides almost everything you
 * need for your Actions. If you want to have some textual output just use
 * context.getWriter().write(...). In case you are developing a KnowWEAction
 * your output will be written to KnowWE.jsp where it is applicable for further
 * processing (via JavaScript etc.).
 * 
 * Additionally you have the possibility to stream almost any kind of content
 * via the OutputStream of the response. Simply use context.getOutputStream().
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 4, 2011
 */
public interface UserActionContext extends UserContext {

	/**
	 * Returns the action which is currently processed. Please note that this is
	 * an optional parameter and thus can be null!
	 *
	 * @created Mar 4, 2011
	 * @return the action the user triggerd (if available)
	 */
	public Action getAction();

	/**
	 * Returns a special path concatenated to the action. Please note that this
	 * is an optional parameter and thus can be null!
	 *
	 * @created Mar 4, 2011
	 * @return
	 */
	public String getPath();

	/**
	 * Returns the http response.
	 *
	 * @created 14.10.2010
	 * @return the user's http response
	 */
	public HttpServletResponse getResponse();

	/**
	 * Returns the name of the action.
	 *
	 * @created Mar 4, 2011
	 * @return the name of the action.
	 */
	public String getActionName();

	/**
	 * Returns the writer of the user's http response.
	 *
	 * @created Mar 4, 2011
	 * @return the writer of the http response
	 * @throws IOException
	 */
	public Writer getWriter() throws IOException;

	/**
	 * Returns the OutputStream of the http response.
	 *
	 * @created Mar 4, 2011
	 * @return the outputstream of the http response.
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException;

	/**
	 * Allows to specify the content type of the http response.
	 *
	 * @created Mar 4, 2011
	 * @param mimetype
	 */
	public void setContentType(String mimetype);

	/**
	 * Allows to specify the length of the http response
	 *
	 * @created Mar 4, 2011
	 * @param length
	 */
	public void setContentLength(int length);

	/**
	 * Allows to set the redirect location of the http response.
	 *
	 * @created Mar 4, 2011
	 * @param location
	 */
	public void sendRedirect(String location) throws IOException;

	/**
	 * Allows to specify the header of the http response.
	 *
	 * @created Mar 4, 2011
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value) throws IOException;

	/**
	 * Allows to send an http error as response.
	 *
	 * @created Mar 4, 2011
	 * @param sc
	 * @param msg
	 */
	public void sendError(int sc, String msg) throws IOException;

}
