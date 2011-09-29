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
import java.util.WeakHashMap;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;

public class WikiEnvironment {

	private Map<String, KnowledgeBase> knowledgeBases;
	private Map<String, SessionBroker> brokers;

	/**
	 * A store for information about a certain session, e.g. for
	 * visualization.
	 */
	private final Map<Session, Map<String, Object>> sessionInfoStore = new WeakHashMap<Session, Map<String, Object>>();

	public WikiEnvironment() {
		knowledgeBases = new HashMap<String, KnowledgeBase>();
		brokers = new HashMap<String, SessionBroker>();
	}

	private void clear() {
		knowledgeBases = new HashMap<String, KnowledgeBase>();
		brokers = new HashMap<String, SessionBroker>();
	}

	synchronized public void reInitialize() {
		clear();
	}

	public void addKnowledgeBase(KnowledgeBase base) {
		KnowledgeBase oldService = getKnowledgeBase(base.getId());
		if (oldService != null) {
			removeKnowledgeBase(oldService);
		}
		knowledgeBases.put(base.getId(), base);

	}

	public void removeKnowledgeBase(KnowledgeBase service) {
		knowledgeBases.remove(service.getId());
	}

	private SessionBroker createBroker(String userID) {
		SessionBroker result = new SessionBroker(this);
		for (KnowledgeBase each : knowledgeBases.values()) {
			Session session = this.createSession(
					each.getId());
			result.addSession(each.getId(), session);
		}
		return result;
	}

	/**
	 * Returns the SessionInfostore for a session. This simply is a Map for
	 * key/value-pairs.
	 * 
	 * @created 25.02.2011
	 * @param session
	 * @return
	 */
	public Map<String, Object> getSessionInfoStore(Session session) {
		Map<String, Object> store = this.sessionInfoStore.get(session);
		if (store == null) {
			store = new HashMap<String, Object>();
			this.sessionInfoStore.put(session, store);
		}
		return store;

	}

	public KnowledgeBase getKnowledgeBase(String id) {
		return knowledgeBases.get(id);
	}

	public Collection<KnowledgeBase> getKnowledgeBases() {
		return knowledgeBases.values();
	}

	public Session createSession(String id) {
		KnowledgeBase service = knowledgeBases.get(id);
		if (service != null) {
			return SessionFactory.createSession(service);
		}
		return null;
	}

	public SessionBroker getBroker(String userID) {
		SessionBroker result = brokers.get(userID);
		if (result == null) {
			SessionBroker broker = createBroker(userID);
			brokers.put(userID, broker);
			result = broker;
		}
		return result;
	}

	public Collection<SessionBroker> getBrokers() {
		return brokers.values();
	}
}
