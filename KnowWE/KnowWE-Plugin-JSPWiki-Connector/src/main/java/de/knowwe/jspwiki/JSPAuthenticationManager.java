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

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.wiki.WikiContext;
import org.apache.wiki.auth.WikiSecurityException;

import de.knowwe.core.user.AuthenticationManager;

/**
 * Implementation of the @link{AuthenticationManager} interface. All methods are
 * delegated to the @link{WikiContext} of JSPWiki.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 16, 2011
 */
public class JSPAuthenticationManager implements AuthenticationManager {

	private final WikiContext context;

	public JSPAuthenticationManager(WikiContext context) {
		this.context = context;
	}

	@Override
	public boolean userIsAsserted() {
		if (context.getWikiSession().isAuthenticated()
				|| context.getWikiSession().isAsserted()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean userIsAuthenticated() {
		return context.getWikiSession().isAuthenticated();
	}

	@Override
	public boolean userIsAdmin() {
		Principal[] princ = context.getWikiSession().getRoles();
		for (Principal p : princ) {
			if (p.getName().equalsIgnoreCase("Admin")) {
				return true;
			}
		}
		return context.hasAdminPermissions();
	}

	public boolean login(HttpServletRequest request, String username, String password) throws WikiSecurityException {
		return context.getEngine()
				.getAuthenticationManager()
				.login(context.getWikiSession(), request, username, password);
	}

}
