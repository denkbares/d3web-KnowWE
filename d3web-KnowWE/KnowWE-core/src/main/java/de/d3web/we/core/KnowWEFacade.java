/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.core;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.fileupload.FileItem;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.upload.UploadManager;
import de.knowwe.plugin.Plugins;


/**
 * @author Jochen
 * 
 * This Facade offers Methods to perform user driven actions, coming by as a
 * (AJAX-) request through (for example) some instance of KnowWEActionDispatcher
 * (e.g. JspwikiActionDispatcher) Each method returns a String shown in the
 * user-view of the webapp. In the KnowWEParameterMap all
 * HTTP-Request-Attributes are stores to give the necessary informations to the
 * specific action.
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
	 */
	public String replaceKDOMNode(KnowWEParameterMap map) {
		return this.tryLoadingAction("ReplaceKDOMNodeAction").perform(map);
	}
	
	/**
	 * 
	 * AutoCompletion for editing in the KnowWE-Wiki. Returns a list of
	 * propositions for continuing the users writing. Result is further
	 * processed and inserted by several javascript functions.
	 * 
	 * @param parameterMap
	 * @return propositions for autocompletion
	 */
	public String autoCompletion(KnowWEParameterMap parameterMap) {
		return performAction("CodeCompletionRenderer", parameterMap);
	}

	public String performAction(String action, KnowWEParameterMap parameterMap) {
		if (action == null) {
			action = parameterMap.get("action");
			if (action == null) {
				throw new IllegalArgumentException("no action specified");
			}
		}
		
		KnowWEAction actionInstance  = tryLoadingAction(action);
		
		if (actionInstance.isAdminAction())
			return performAdminAction(actionInstance, parameterMap);
		else 
			return actionInstance.perform(parameterMap);
	}

	
	/**
	 * Checks the rights of the user prior to performing the action.
	 * 
	 */
	private String performAdminAction(KnowWEAction action,
			KnowWEParameterMap parameterMap) {
		
		ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");

		if (bundle.getString("knowwewiki.parseAllFunction").equals("true")) {

			if (parameterMap.getWikiContext().userIsAdmin()) {
				return action.perform(parameterMap);
			}

			return "<p class=\"info box\">"
					+ KnowWEEnvironment.getInstance().getKwikiBundle(parameterMap.getRequest()).getString("KnowWE.login.error.admin")
					+ "</p>";
		}
		
		
		return action.perform(parameterMap);
	}

	/**
	 * Trys to load the class with the specified name.
	 * if it is not fully qualified, the default knowwe action
	 * package is prefixed.
	 * If an action is found the created instance is cached in the
	 * action map for later use.
	 * 
	 * @return s an instance of the action, never null
	 * @throws various exceptions depending on the exact error 
	 * 
	 */
	public KnowWEAction tryLoadingAction(String actionName) {
		// check is action is fully qualified class name
		if (!actionName.contains(".")) {
			// if not, use d3web default package
			actionName = "de.d3web.we.action." + actionName;
		}
		List<KnowWEAction> knowWEActions = Plugins.getKnowWEAction();
		for (KnowWEAction action: knowWEActions) {
			if (action.getClass().getName().equals(actionName)) {
				return action;
			}
		}
		throw new IllegalArgumentException("No action "+actionName+" available");
	}

	public String performAction(KnowWEParameterMap parameterMap) {
		return this.performAction(null, parameterMap);
	}

	public String getNodeData(String web, String topic, String nodeID) {
		return KnowWEEnvironment.getInstance().getNodeData(web, topic, nodeID);
	}

	public String uploadFiles(Collection<FileItem> fileItems) {
		return UploadManager.getInstance().manageUpload(fileItems);
	}


	/**
	 * This returns a dump of the current Ontology
	 * @return
	 */
	public void writeOwl(OutputStream stream){
	    SemanticCore.getInstance().writeDump(stream);
	}
}
