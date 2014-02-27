/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.core.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.core.user.UserContext;

/**
 * UserActionContext interface which is a specialisied UserContext interface for Actions.
 * <p/>
 * The UserActionContext of the execute method provides almost everything you need for your Actions. If you want to have
 * some textual output just use context.getWriter().write(...). In case you are developing a KnowWEAction your output
 * will be written to KnowWE.jsp where it is applicable for further processing (via JavaScript etc.).
 * <p/>
 * Additionally you have the possibility to stream almost any kind of content via the OutputStream of the response.
 * Simply use context.getOutputStream().
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 4, 2011
 */
public interface UserActionContext extends UserContext {

	/**
	 * Returns the action which is currently processed. Please note that this is an optional parameter and thus can be
	 * null!
	 *
	 * @return the action the user triggerd (if available)
	 * @created Mar 4, 2011
	 */
	public Action getAction();

	/**
	 * Returns a special path concatenated to the action. Please note that this is an optional parameter and thus can be
	 * null!
	 *
	 * @return
	 * @created Mar 4, 2011
	 */
	public String getPath();

	/**
	 * Returns the http response.
	 *
	 * @return the user's http response
	 * @created 14.10.2010
	 */
	public HttpServletResponse getResponse();

	/**
	 * Returns the name of the action.
	 *
	 * @return the name of the action.
	 * @created Mar 4, 2011
	 */
	public String getActionName();

	/**
	 * Return the AuthenticationManager of the action.
	 *
	 * @return
	 * @created Feb 25, 2013
	 */
	public AuthenticationManager getManager();

	/**
	 * Returns the writer of the user's http response.
	 *
	 * @return the writer of the http response
	 * @throws IOException
	 * @created Mar 4, 2011
	 */
	public Writer getWriter() throws IOException;

	/**
	 * Returns the OutputStream of the http response.
	 *
	 * @return the outputstream of the http response.
	 * @throws IOException
	 * @created Mar 4, 2011
	 */
	public OutputStream getOutputStream() throws IOException;

	/**
	 * Allows to specify the content type of the http response.
	 *
	 * @param mimetype
	 * @created Mar 4, 2011
	 */
	public void setContentType(String mimetype);

	/**
	 * Allows to specify the length of the http response. The length is the number of bytes to be send.
	 *
	 * @param length
	 * @created Mar 4, 2011
	 */
	public void setContentLength(int length);

	/**
	 * Allows to set the redirect location of the http response.
	 *
	 * @param location
	 * @created Mar 4, 2011
	 */
	public void sendRedirect(String location) throws IOException;

	/**
	 * Allows to specify the header of the http response.
	 *
	 * @param name
	 * @param value
	 * @created Mar 4, 2011
	 */
	public void setHeader(String name, String value) throws IOException;

	/**
	 * Allows to send an http error as response.
	 *
	 * @param sc
	 * @param msg
	 * @created Mar 4, 2011
	 */
	public void sendError(int sc, String msg) throws IOException;

}
