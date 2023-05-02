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
 * UserActionContext interface which is a specialized UserContext interface for Actions.
 * <p/>
 * The UserActionContext of the execute method provides almost everything you need for your Actions. If you want to have
 * some textual output just use context.getWriter().write(...). In case you are developing a KnowWEAction your output
 * will be written to KnowWE.jsp where it is applicable for further processing (via JavaScript etc.).
 * <p/>
 * Additionally, you have the possibility to stream almost any kind of content via the OutputStream of the response.
 * Simply use context.getOutputStream().
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 04.03.2011
 */
public interface UserActionContext extends UserContext {

	/**
	 * Returns the action which is currently processed.
	 * Can be null.
	 *
	 * @return the action the user triggered (if available)
	 */
	Action getAction();

	/**
	 * Returns a special path concatenated to the action.
	 * Can be null.
	 *
	 * @return special path
	 */
	String getPath();

	/**
	 * Returns the HTTP response object.
	 *
	 * @return the current HTTP response object
	 * @created 14.10.2010
	 */
	HttpServletResponse getResponse();

	/**
	 * Returns the name of the action.
	 *
	 * @return the name of the action
	 */
	String getActionName();

	/**
	 * Returns the AuthenticationManager of the action.
	 *
	 * @return AuthenticationManager
	 * @created 25.02.2013
	 */
	AuthenticationManager getManager();

	/**
	 * Returns the writer of the user's http response.
	 *
	 * @return the writer of the http response
	 * @throws IOException io exception
	 * @created Mar 4, 2011
	 */
	Writer getWriter() throws IOException;

	/**
	 * Returns the OutputStream of the HTTP response.
	 *
	 * @return the OutputStream of the HTTP response
	 * @throws IOException io exception
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Sets 'Content-Type' in the response.
	 *
	 * @param mimetype MIME type to send
	 */
	void setContentType(String mimetype);

	/**
	 * Allows to specify the length of the HTTP response. The length is the number of bytes to be sent.
	 *
	 * @param length content length to set
	 */
	void setContentLength(int length);

	/**
	 * Redirect the user to the specified location.
	 *
	 * @param location redirects the user to the given location
	 */
	void sendRedirect(String location) throws IOException;

	/**
	 * Sends a header <tt>x-redirect-header</tt> to get the possibility to navigate to another page after an AJAX call.
	 * To use it in your success function, use KNOWWE.core.util.reload(jqXHR) with the jQuery XHR request.
	 *
	 * @param location navigates the user to the given location
	 */
	void sendNavigate(String location);

	/**
	 * Send an HTTP header with the response.
	 *
	 * @param name Header name
	 * @param value value to set the header to
	 */
	void setHeader(String name, String value) throws IOException;

	/**
	 * Sends an HTTP error as response.
	 * Constants starting with SC_ in {@link javax.servlet.http.HttpServletResponse} are available to set HTTP error codes.
	 *
	 * @param sc HTTP status code
	 * @param msg error message to display. If there's an error message configured in the servlet, this message is
	 *            proposed as message
	 */
	void sendError(int sc, String msg) throws IOException;
}