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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ecyrd.jspwiki.WikiContext;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.we.user.AbstractUserContext;

/**
 * This class is a default implemantation of the UserActionContext interface.
 * 
 * The parameters of the request are accessible via getParameter(String
 * parametername). Be sure you know which parameters you have in your request.
 * 
 * Additionally it is possible to write content to your pages via
 * getWriter().write() or to send Data in a response via getOutputStream().
 * 
 * Please note, that if you use this class with a KnowWEAction everything you
 * write via getWriter().write() will be written to the KnowWE.jsp where it is
 * applicable for further processing (via JavaScript etc.).
 * 
 * @author Sebastian Furth
 * 
 */
public class ActionContext extends AbstractUserContext implements UserActionContext {

	public final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	public final String EXTENDED_POINT_ID = "Action";

	/**
	 * The name of the action
	 */
	private final String actionName;

	/**
	 * optional parameter for special servlets
	 */
	private final String path;

	/**
	 * all parameters of the request
	 */
	private final Map<String, String> parameters;

	/**
	 * the request itself
	 */
	private final HttpServletRequest request;

	/**
	 * the response itself
	 */
	private final HttpServletResponse response;

	/**
	 * the servlet context, necessary for KnowWEActions
	 */
	private final ServletContext servletContext;

	/**
	 * Default constructor.
	 *
	 * @param actionName Name of your action, equivalent to the ID specified in
	 *        your plugin.xml
	 * @param path optional parameter, only necessary for special servlets
	 * @param parameters all parameters of the request
	 * @param request the request itself
	 * @param response the response you can use for your purposes
	 * @param servletContext the servlet context
	 * @param map optional parameter for KnowWEActions
	 */
	public ActionContext(String actionName, String path, Map<String, String> parameters,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext servletContext, WikiContext wikiContext) {
		super(wikiContext);
		this.actionName = actionName;
		this.path = path;
		this.parameters = parameters;
		this.response = response;
		this.request = request;
		this.servletContext = servletContext;
	}

	public Action getAction() {
		PluginManager manager = PluginManager.getInstance();
		Extension[] extensions = manager.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_ID);
		for (Extension e : extensions) {
			if (e.getName().equals(actionName)) return ((Action) e.getSingleton());
		}
		Logger.getLogger(getClass().getName()).warning(
				"Action: \"" + actionName + "\" not found, check plugin.xml.");
		return null;
	}

	public String getActionName() {
		return this.actionName;
	}

	public String getPath() {
		return this.path;
	}

	public Map<String, String> getParameters() {
		return this.parameters;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public HttpServletRequest getRequest() {
		return this.request;
	}

	public HttpServletResponse getResponse() {
		return this.response;
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public Writer getWriter() throws IOException {
		if (response != null) return this.response.getWriter();
		return null;
	}

	public OutputStream getOutputStream() throws IOException {
		return this.response.getOutputStream();
	}

	public void setContentType(String mimetype) {
		this.response.setContentType(mimetype);
	}

	public void setContentLength(int length) {
		this.response.setContentLength(length);
	}

	public void sendRedirect(String location) throws IOException {
		this.response.sendRedirect(location);
	}

	public void setHeader(String name, String value) throws IOException {
		this.response.setHeader(name, value);
	}

	public void sendError(int sc, String msg) throws IOException {
		this.response.sendError(sc, msg);
	}

}
