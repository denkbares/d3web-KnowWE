/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.jspwiki;

import java.security.Principal;

import com.ecyrd.jspwiki.WikiContext;

import de.d3web.we.user.AuthenticationManager;

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
	public boolean userIsAdmin() {
		Principal[] princ = context.getWikiSession().getRoles();
		for (Principal p : princ) {
			if (p.getName().equals("Admin")) {
				return true;
			}
		}
		return false;
	}

}
