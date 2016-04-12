/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.d3web.event;

import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;
import de.knowwe.core.event.Event;
import de.knowwe.core.user.UserContext;

/**
 * This event should be fired each time a fact is set by a user.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class FindingSetEvent extends Event {

	private final UserContext user;
	private final Fact fact;
	private final Session session;

	public FindingSetEvent(Fact fact, Session session, UserContext user) {
		this.user = user;
		this.fact = fact;
		this.session = session;
	}

	public UserContext getUserContext() {
		return user;
	}

	public Fact getFact() {
		return fact;
	}

	public Session getSession() {
		return session;
	}

}
