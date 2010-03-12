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
package de.d3web.we.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.d3web.plugin.Extension;
import de.d3web.plugin.JPFPluginManager;
import de.d3web.plugin.PluginManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ActionContext {

	public final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	public final String EXTENDED_POINT_ID = "Action";
	
	private final String actionName;
	private final String path;
	private final Properties parameters;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ServletContext servletContext;
	private final KnowWEParameterMap map;

	public ActionContext(String actionName, String path, Properties parameters, 
			HttpServletRequest request, HttpServletResponse response,
			  ServletContext servletContext, KnowWEParameterMap map) {
		this.actionName = actionName;
		this.path = path;
		this.parameters = parameters;
		this.response = response;
		this.request = request;
		this.servletContext = servletContext;
		this.map = map;
	}
	
	public Action getAction() {
		PluginManager manager = JPFPluginManager.getInstance();
		Extension[] extensions = manager.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_ID);
		for (Extension e : extensions) {
			if (e.getName().equals(actionName))
				return ((Action) e.getSingleton());
		}
		Logger.getLogger(this.getClass()).warn("Action: \"" + actionName + "\" not found, check plugin.xml.");
		return null;
	}
	
	public String getActionName() {
		return this.actionName;
	}
	
	public String getPath() {
		return this.path;
	}

	public Properties getParameters() {
		return this.parameters;
	}
	
	public String getParameter(String key) {
		return this.parameters.getProperty(key);
	}
	
	public String getParameter(String key, String defaultValue) {
		return this.parameters.getProperty(key, defaultValue);
	}
	
	public String getWeb() {
		return this.getParameter(KnowWEAttributes.WEB);
	}
	
	public String getTopic() {
		return this.getParameter(KnowWEAttributes.TOPIC);
	}
	
	public String getUser() {
		return this.getParameter(KnowWEAttributes.USER);
	}
	
	public KnowWEUserContext getWikiContext() {
		if (map != null)
			return this.map.getWikiContext();
		Logger.getLogger(this.getClass()).info("No WikiContext found. getWikiContext() works only for KnowWEActions not for Servlets. Returned null.");
		return null;
	}
	
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	
	public HttpServletRequest getRequest() {
		return this.request;
	}
		
	public HttpSession getSession() {
		return this.request.getSession();
	}
			
	public Writer getWriter() throws IOException {
		return this.response.getWriter();
	}
	
	public OutputStream getOutputStream() throws IOException {
		return this.response.getOutputStream();
	}
		
	public KnowWEParameterMap getKnowWEParameterMap() {
		return this.map;
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
