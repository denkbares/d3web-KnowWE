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

package de.knowwe.jspwiki;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wiki.WikiContext;

import de.knowwe.core.Attributes;
import de.knowwe.core.user.AbstractUserContext;

public class JSPWikiUserContext extends AbstractUserContext {

	private final Map<String, String> urlParameter;
	private final WikiContext context;

	public JSPWikiUserContext(WikiContext context,
			Map<String, String> urlParameter) {
		super(new JSPAuthenticationManager(context));
		this.context = context;
		this.urlParameter = urlParameter;
		addDefaultAttributes();
	}

	private void addDefaultAttributes() {
		// Add user
		if (!urlParameter.containsKey(Attributes.USER)) {
			urlParameter.put(Attributes.USER,
					context.getWikiSession().getUserPrincipal().getName());
		}
		// Add topic
		if (!urlParameter.containsKey(Attributes.TOPIC)) {
			urlParameter.put(Attributes.TOPIC, context.getPage().getName());
		}
		// Add web
		if (!urlParameter.containsKey(Attributes.WEB)) {
			urlParameter.put(Attributes.WEB, "default_web");
		}
	}

	@Override
	public HttpServletRequest getRequest() {
		return context.getHttpRequest();
	}

	@Override
	public Map<String, String> getParameters() {
		return urlParameter;
	}

	@Override
	public HttpSession getSession() {
		if (this.context.getHttpRequest() == null) return null;
		return this.context.getHttpRequest().getSession();
	}

	@Override
	public ServletContext getServletContext() {
		if (this.context.getHttpRequest() == null) return null;
		return this.context.getHttpRequest().getSession().getServletContext();
	}

}
