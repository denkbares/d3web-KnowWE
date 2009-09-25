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

package de.d3web.we.jspwiki;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.WikiContext;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class JSPWikiUserContext implements KnowWEUserContext{
	
	private WikiContext context;
	private Map<String,String> urlParameter;
	 
	public JSPWikiUserContext (WikiContext context, Map<String,String> urlParameter) {
		this.context = context;
		this.urlParameter = urlParameter;
	}
	
	public JSPWikiUserContext (WikiContext context) {
		this.context = context;
	}
	
	public HttpServletRequest getHttpRequest() {
		return context.getHttpRequest();
	}
	
	public void setUrlParameter(Map<String, String> urlParameter) {
		this.urlParameter = urlParameter;
	}

	public String getUsername() {
		return context.getWikiSession().getUserPrincipal().getName();
	}

	@Override
	public boolean userIsAdmin() {

		// returns true if User is in Admin-Group
		Principal[] princ = context.getWikiSession().getRoles();
		
		for (Principal p : princ) {
			if (p.getName().equals("Admin")) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Map<String, String> getUrlParameterMap() {
		return urlParameter;
	}
	
//	public static JSPWikiUserContext getUserByName( String name ) {
//		
//		Map<String, String> urlParam = null; //TODO: get URLParam & wikiContext by name
//		WikiContext wikiContext = null;
//		
//		return new JSPWikiUserContext (wikiContext, urlParam);
//	}
	
}
