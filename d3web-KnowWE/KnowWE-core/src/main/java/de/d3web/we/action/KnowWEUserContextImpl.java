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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWEUserContextImpl implements KnowWEUserContext {

	private String user;
	private Map<String, String> params;
	private String page;

	public KnowWEUserContextImpl(String user, Map<String, String> parameters) {
		this.user = user;
		this.params = parameters;
	}

	@Override
	public String getUsername() {
		return user;
	}

	public String getPage() {
		return page;
	}

	@Override
	public boolean userIsAdmin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> getUrlParameterMap() {
		return params;
	}

	@Override
	public HttpServletRequest getHttpRequest() {
		// TODO Auto-generated method stub
		return null;
	}

}
