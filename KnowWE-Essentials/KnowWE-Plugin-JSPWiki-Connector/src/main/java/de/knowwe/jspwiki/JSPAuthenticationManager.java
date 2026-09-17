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

import org.apache.wiki.api.core.Context;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.user.UserProfile;

import com.denkbares.strings.Strings;
import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.jspwiki.auth.AuthenticatedIdentities;

/**
 * Implementation of the @link{AuthenticationManager} interface. All methods are
 * delegated to the @link{WikiContext} of JSPWiki.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 16, 2011
 */
public class JSPAuthenticationManager implements AuthenticationManager {

	private final Context context;

	public JSPAuthenticationManager(Context context) {
		this.context = context;
	}

	@Override
	public boolean userIsAsserted() {
		return context.getWikiSession().isAuthenticated()
				|| context.getWikiSession().isAsserted();
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
				.getManager(org.apache.wiki.auth.AuthenticationManager.class)
				.login(context.getWikiSession(), request, username, password);
	}

	@Override
	public String getUserName() {
		return context.getWikiSession().getUserPrincipal().getName();
	}

	/**
	 * The user's mail address from their wiki profile, or, for a user the wiki knows only from their sign-in against
	 * an identity provider, the address that provider stated. Null where neither knows one.
	 */
	@Override
	public String getMailAddress() {
		UserManager manager = context.getEngine().getManager(UserManager.class);
		UserProfile userProfile = manager.getUserProfile(context.getWikiSession());
		String email = userProfile == null ? null : userProfile.getEmail();
		if (Strings.isBlank(email)) {
			AuthenticatedIdentities.Identity identity = AuthenticatedIdentities.of(context.getEngine(),
					context.getWikiSession().getUserPrincipal().getName());
			if (identity != null) {
				email = identity.email();
			}
		}
		return email;
	}
}
