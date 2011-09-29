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

package de.d3web.we.basic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;

public class SessionBroker {

	private final WikiEnvironment environment;
	private Map<String, Session> sessions;

	public SessionBroker(WikiEnvironment environment) {
		super();
		this.environment = environment;
		sessions = new HashMap<String, Session>();
	}

	public Collection<Session> getSessions() {
		return sessions.values();
	}

	public Session getSession(String id) {
		return sessions.get(id);
	}

	public void addSession(String id, Session session) {

		sessions.put(id, session);
	}

	public void removeSession(String id) {
		sessions.remove(id);
	}

	public void clear() {
		/*
		 * HOTFIX : reinit all d3webKnowledgeServiceSessions to update changed
		 * knowledgebases
		 */
		java.util.Set<String> serviceIDs = sessions.keySet();
		sessions = new HashMap<String, Session>();
		for (String string : serviceIDs) {
			removeSession(string);
			KnowledgeBase service = environment.getKnowledgeBase(string);
			addSession(service.getId(), SessionFactory.createSession(service));
		}

	}
}
