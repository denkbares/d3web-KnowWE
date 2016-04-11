/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;

import de.knowwe.core.action.AbstractActionServlet;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.core.user.UserContextUtil;

/**
 * JSPWiki Version of the @link{AbstractActionServlet}.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 16, 2011
 */
public class JSPActionServlet extends AbstractActionServlet {

	private static final long serialVersionUID = -3039426486440276502L;

	@Override
	protected UserActionContext createActionContext(HttpServletRequest request, HttpServletResponse response) {
		WikiEngine wiki = WikiEngine.getInstance(getServletConfig());
		WikiContext wikiContext = wiki.createContext(request, WikiContext.VIEW);
		AuthenticationManager manager = new JSPAuthenticationManager(wikiContext);

		// create action context
		return new ActionContext(
				getActionName(request),
				getActionFollowUpPath(request),
				UserContextUtil.getParameters(request),
				request,
				response,
				getServletContext(),
				manager);
	}

}
