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
package de.d3web.we.action;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.user.UserContextUtil;

/**
 * ActionServlet is a Servlet for ajax-based interview or any other user
 * interfaces.
 *
 * ActionServlet provides an extensible set of actions that can be used to
 * provide required functionality. These actions can be used from client side to
 * proceed e.g. within the interview or to deliver required information.
 * <p>
 * New actions can be added easily by providing a class implementing the
 * {@link Action} interface and adds it to the package specified by the
 * this.COMMAND_PACKAGE constant (or a sub-package). The command name is the
 * name of the class, or if it is in a sub-package, the class name preceded by
 * the missing sub-package names (e.g. command name for class
 * "cc.d3web.use.servlet.cmd.ajax.Restart" will be "ajax.Restart").
 * <p>
 * There are two methods of calling a action:
 * <ol>
 *
 * <li>A action is called by using a POST or GET for the following URL:
 *
 * <pre>
 * &quot;http://&lt;host&gt;:&lt;port&gt;[/&lt;servlet-locator&gt;]/&lt;command-name&gt;[/&lt;path-suffix&gt;]&quot;
 * </pre>
 *
 * Depending on the action, it may utilize information given by query parameters
 * or the path suffix to produce the desired outcome.</li>
 *
 * <li>In addition you can call a series of actions using an xml structure as
 * the body of a post request (or the query string). The url of this method is:
 *
 * <pre>
 * &quot;http://&lt;host&gt;:&lt;port&gt;[/&lt;servlet-locator&gt;]/command&quot;
 * </pre>
 *
 * The provided xml must have the following structure:
 *
 * <pre>
 * 	&lt;commands&gt;
 * 		&lt;command name="ajax.Restart" path="ui.zip/index.html"&gt;
 * 			&lt;param name="foo"&gt;bla&lt;/param&gt;
 * 		&lt;/command&gt;
 * 	&lt;/commands&gt;
 * </pre>
 *
 * The action tag can be repeated multiple times. The example above is almost
 * identical to the following url:
 *
 * <pre>
 * &quot;http://&lt;host&gt;:&lt;port&gt;[/&lt;servlet-locator&gt;]/ajax.Restart/ui.zip/index.html?foo=bla&quot;
 * </pre>
 *
 * </li>
 * </ol>
 *
 * @author Volker Belli (refactored by Sebastian Furth)
 *
 */
public class ActionServlet extends HttpServlet {

	private static final long serialVersionUID = 9190931066151487381L;

	public ActionServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Logger.getLogger("ActionServlet").info(
				"GET: " + request.getRequestURI() + "?" + request.getQueryString());
		if (request.getPathInfo() == null || request.getPathInfo().length() <= 1) {
			// no path provided (or only "/" as path)
			Logger.getLogger("ActionServlet").info(
					"no path provided (or only \"/\" as path): " + request.getRequestURI());
			response.getWriter().write(
					"<b>ActionServlet:</b> No path provided (or only \"/\" as path): "
							+ request.getRequestURI());
		}
		else if (request.getPathInfo().equals("/command")) {
			// xml base command list
			doXmlActions(request, response, new StringReader(request.getQueryString()));
		}
		else {
			// or simple path command
			doPathAction(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Logger.getLogger("ActionServlet").info("POST: " + request.getRequestURI());
		if (request.getPathInfo().equals("/command")) {
			doXmlActions(request, response, request.getReader());
		}
		else {
			doPathAction(request, response);
		}
	}

	private void doPathAction(HttpServletRequest request, HttpServletResponse response) throws IOException {

		WikiEngine wiki = WikiEngine.getInstance(getServletConfig());

		// create action context
	    ActionContext context = new ActionContext(
				getActionName(request),
				getActionFollowUpPath(request),
				UserContextUtil.getParameters(request),
				request,
				response,
				getServletContext(),
				wiki.createContext(request, WikiContext.VIEW)
			);
		try {
			// get action and execute it
			Action cmd = context.getAction();
			cmd.execute(context);
		}
		catch (RuntimeException e) {
			Logger.getLogger("ActionServlet").log(Level.SEVERE, "unexpected internal error", e);
			throw e;
		}
	}

	private void doXmlActions(HttpServletRequest request, HttpServletResponse response, Reader xmlReader) throws IOException {
		throw new IllegalStateException("not implemented yet");
	}

	@Override
	public synchronized void init() {
	}

	@Override
	public synchronized void destroy() {
	}

	public String getActionName(HttpServletRequest request) {
		String path = request.getPathInfo();
		int cmdEndPos = path.indexOf('/', 1);
		if (cmdEndPos == -1) cmdEndPos = path.indexOf('?', 1);
		if (cmdEndPos == -1) cmdEndPos = path.length();
		return path.substring(1, cmdEndPos);
	}

	public static String getActionFollowUpPath(HttpServletRequest request) {
		String path = request.getPathInfo();

		int pathStartPos = path == null ? -1 : path.indexOf('/', 1);
		if (pathStartPos == -1) return null;

		int pathEndPos = path.indexOf('?', pathStartPos + 1);
		if (pathEndPos == -1) pathEndPos = path.length();

		return path.substring(pathStartPos + 1, pathEndPos);
	}

}
