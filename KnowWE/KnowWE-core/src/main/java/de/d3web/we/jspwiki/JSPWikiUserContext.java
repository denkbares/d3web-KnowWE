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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.ecyrd.jspwiki.WikiContext;

import de.d3web.we.user.AbstractUserContext;

public class JSPWikiUserContext extends AbstractUserContext {

	private final Map<String, String> urlParameter;

	public JSPWikiUserContext(WikiContext context,
			Map<String, String> urlParameter) {
		super(context);
		this.urlParameter = urlParameter;
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
		return this.context.getHttpRequest().getSession();
	}

	@Override
	public ServletContext getServletContext() {
		return this.context.getHttpRequest().getSession().getServletContext();
	}

}
