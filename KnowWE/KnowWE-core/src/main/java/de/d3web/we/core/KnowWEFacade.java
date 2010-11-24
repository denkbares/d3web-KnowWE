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

package de.d3web.we.core;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import de.d3web.we.action.Action;
import de.d3web.we.action.ActionContext;
import de.knowwe.plugin.Plugins;

/**
 * @author Jochen
 * 
 *         This Facade offers Methods to perform user driven actions, coming by
 *         as a (AJAX-) request through (for example) some instance of
 *         KnowWEActionDispatcher (e.g. JspwikiActionDispatcher) Each method
 *         returns a String shown in the user-view of the webapp. In the
 *         KnowWEParameterMap all HTTP-Request-Attributes are stores to give the
 *         necessary informations to the specific action.
 * 
 * 
 */
public class KnowWEFacade {

	/**
	 * Singleton instance
	 */
	private static KnowWEFacade instance;

	/**
	 * lazy singleton instance factory
	 * 
	 * @return
	 */
	public static synchronized KnowWEFacade getInstance() {
		if (instance == null) {
			instance = new KnowWEFacade();
		}

		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private KnowWEFacade() {
	}

	/**
	 * Used for Tests.
	 * 
	 * @param map
	 * @return
	 * @throws IOException
	 */
	public void replaceKDOMNode(KnowWEParameterMap map) throws IOException {
		performAction("ReplaceKDOMNodeAction", map);
	}

	/**
	 * 
	 * AutoCompletion for editing in the KnowWE-Wiki. Returns a list of
	 * propositions for continuing the users writing. Result is further
	 * processed and inserted by several javascript functions.
	 * 
	 * @param parameterMap
	 * @return propositions for autocompletion
	 * @throws IOException
	 * @throws IOException
	 */
	public void autoCompletion(KnowWEParameterMap parameterMap) throws IOException {
		performAction("CodeCompletionRenderer", parameterMap);
	}

	public void performAction(String action, KnowWEParameterMap parameterMap) throws IOException {
		if (action == null) {
			action = parameterMap.get("action");
			if (action == null) {
				throw new IllegalArgumentException("no action specified");
			}
		}

		// Create ActionContext
		ActionContext context = new ActionContext(action, "",
				getActionContextProperties(parameterMap),
				parameterMap.getRequest(),
				parameterMap.getResponse(),
				parameterMap.getContext(),
				parameterMap);

		// Get the Action
		Action actionInstance = context.getAction();

		if (actionInstance == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load action: \""
					+ action + "\"");
			context.getWriter().write("Unable to load action: \"" + action + "\"");
			Logger.getLogger(this.getClass().getName()).warning(
					"Unable to load action: \"" + action + "\"");
		}
		else if (actionInstance.isAdminAction()) {
			performAdminAction(actionInstance, context);
		}
		else {
			actionInstance.execute(context);
		}
	}

	public Properties getActionContextProperties(KnowWEParameterMap map) {
		Properties parameters = new Properties();
		for (String key : map.keySet()) {
			String value = map.get(key);
			parameters.put(key, value);
		}
		return parameters;
	}

	/**
	 * Checks the rights of the user prior to performing the action.
	 * 
	 * @throws IOException
	 * 
	 */
	private void performAdminAction(Action action,
			ActionContext context) throws IOException {

		ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");

		if (bundle.getString("knowwewiki.parseAllFunction").equals("true")) {

			if (context.getKnowWEParameterMap().getWikiContext().userIsAdmin()) {
				action.execute(context);
			}

			context.getWriter().write(
					"<p class=\"info box\">"
							+ KnowWEEnvironment.getInstance().getKwikiBundle(
									context.getKnowWEParameterMap().getWikiContext()).getString(
									"KnowWE.login.error.admin")
							+ "</p>");
		}
		else {
			action.execute(context);
		}

	}

	/**
	 * Trys to load the class with the specified name. if it is not fully
	 * qualified, the default knowwe action package is prefixed. If an action is
	 * found the created instance is cached in the action map for later use.
	 * 
	 * @return s an instance of the action, never null
	 * @throws various exceptions depending on the exact error
	 * 
	 */
	public Action tryLoadingAction(String actionName) {
		String originalName = actionName;
		// check if action is fully qualified class name
		if (!actionName.contains(".")) {
			// if not, use d3web default package
			actionName = "de.d3web.we.action." + actionName;
		}
		List<Action> actions = Plugins.getKnowWEAction();
		for (Action action : actions) {
			if (action.getClass().getName().equals(actionName)) {
				return action;
			}
		}
		// if no action is found, search in other packages then the default
		// package
		for (Action action : actions) {
			if (action.getClass().getName().endsWith(originalName)) {
				return action;
			}
		}
		throw new IllegalArgumentException("No action " + actionName + " available");
	}

	public void performAction(KnowWEParameterMap parameterMap) throws IOException {
		performAction(null, parameterMap);
	}

	public String getNodeData(String web, String topic, String nodeID) {
		return KnowWEEnvironment.getInstance().getNodeData(web, topic, nodeID);
	}

}
