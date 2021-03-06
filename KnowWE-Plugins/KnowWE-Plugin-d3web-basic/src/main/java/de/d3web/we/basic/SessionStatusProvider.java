/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.d3web.we.basic;

import java.util.Collection;

import de.d3web.core.session.Session;
import de.knowwe.core.user.UserContext;
import de.knowwe.plugin.StatusProvider;

/**
 * Provides a status for the sessions of the user.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.08.2014
 */
public class SessionStatusProvider implements StatusProvider {

	@Override
	public int getStatus(UserContext context) {
		Collection<Session> sessions = SessionProvider.getSessions(context);
		int hash = 0;
		for (Session session : sessions) {
			hash += session.getId().hashCode();
			hash += session.getLastChangeDate().getTime();
		}
		return hash;
	}
}
