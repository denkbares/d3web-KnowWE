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

package de.d3web.we.jspwiki;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import de.d3web.we.action.Action;
import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.action.UserActionContext;

public class JSPActionDispatcher extends KnowWEActionDispatcher {

	private static final JSPActionDispatcher instance = new JSPActionDispatcher();

	private JSPActionDispatcher() {
	}

	public static JSPActionDispatcher getInstance() {
		return instance;
	}

	@Override
	public void performAction(UserActionContext context) throws IOException {
		String action = context.getParameter("action");
		context.getParameters().put("env", "JSPWiki");
		context.getParameters().put("KWikiWeb", "default_web");

		if (action != null) {
			executeAction(context);
		}
		else {
			context.getResponse().getWriter().write("no action found");
			throw new NullPointerException("Action is null!");
		}
	}

	private void executeAction(UserActionContext context) throws IOException {
		// Get an instance of the action
		Action actionInstance = context.getAction();

		// Error Handling
		if (actionInstance == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load action: \""
					+ context.getActionName() + "\"");
			context.getWriter().write("Unable to load action: \"" + context.getActionName() + "\"");
			Logger.getLogger(this.getClass().getName()).warning(
					"Unable to load action: \"" + context.getActionName() + "\"");
		}
		// Execute the action
		else if (actionInstance.isAdminAction()) {
			executeAdminAction(actionInstance, context);
		}
		else {
			actionInstance.execute(context);
		}
	}

	private void executeAdminAction(Action action, UserActionContext context) throws IOException {

		// Check if user is admin and execute the action
		if (context.userIsAdmin()) {
			action.execute(context);
		}

		// Tell the user that he has not the required privileges
		else {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You need to be admin to execute the action: \""
							+ context.getActionName() + "\"");
			context.getWriter().write("You need to be admin to execute the action: \""
					+ context.getActionName() + "\"");
			Logger.getLogger(this.getClass().getName()).warning(
					"Unauthorized user tried to execute action: \"" + context.getActionName()
							+ "\"");
		}
	}
}
