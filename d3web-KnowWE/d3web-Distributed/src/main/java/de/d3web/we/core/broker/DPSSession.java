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

package de.d3web.we.core.broker;

import java.rmi.server.UID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.blackboard.Blackboard;
import de.d3web.we.core.blackboard.BlackboardImpl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class DPSSession {

	private final DPSEnvironment environment;
	private final String id;
	private Blackboard blackboard;
	private Map<String, KnowledgeServiceSession> serviceSessions;

	public DPSSession(DPSEnvironment environment) {
		super();
		this.environment = environment;
		id = new UID().toString();
		blackboard = new BlackboardImpl();
		serviceSessions = new HashMap<String, KnowledgeServiceSession>();
	}

	public Blackboard getBlackboard() {
		return blackboard;
	}

	public Collection<KnowledgeServiceSession> getServiceSessions() {
		return serviceSessions.values();
	}

	public KnowledgeServiceSession getServiceSession(String id) {
		return serviceSessions.get(id);
	}

	public String getServiceId(KnowledgeServiceSession serviceSession) {
		for (String id : serviceSessions.keySet()) {
			if (serviceSessions.get(id).equals(serviceSession)) {
				return id;
			}
		}
		return null;
	}

	public void addServiceSession(String id, KnowledgeServiceSession serviceSession) {
		serviceSessions.put(id, serviceSession);
	}

	public void removeServiceSession(String id) {
		serviceSessions.remove(id);
		blackboard.removeInformation(id);
	}

	public void clear(Broker broker) {
		getBlackboard().clear(broker);

		/*
		 * HOTFIX : reinit all d3webKnowledgeServiceSessions to update changed
		 * knowledgebases
		 */
		java.util.Set<String> serviceIDs = serviceSessions.keySet();
		serviceSessions = new HashMap<String, KnowledgeServiceSession>();
		for (String string : serviceIDs) {
			removeServiceSession(string);
			KnowledgeService service = environment.getService(string);
			addServiceSession(service.getId(), service.createSession(broker));
		}

	}

	public String getId() {
		return id;
	}
}
