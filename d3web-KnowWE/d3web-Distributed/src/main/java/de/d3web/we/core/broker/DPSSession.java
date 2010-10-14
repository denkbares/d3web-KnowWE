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
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;

public class DPSSession {

	private final DPSEnvironment environment;
	private final String id;
	private Map<String, D3webKnowledgeServiceSession> serviceSessions;

	public DPSSession(DPSEnvironment environment) {
		super();
		this.environment = environment;
		id = new UID().toString();
		serviceSessions = new HashMap<String, D3webKnowledgeServiceSession>();
	}

	public Collection<D3webKnowledgeServiceSession> getServiceSessions() {
		return serviceSessions.values();
	}

	public D3webKnowledgeServiceSession getServiceSession(String id) {
		return serviceSessions.get(id);
	}

	public String getServiceId(D3webKnowledgeServiceSession serviceSession) {
		for (String id : serviceSessions.keySet()) {
			if (serviceSessions.get(id).equals(serviceSession)) {
				return id;
			}
		}
		return null;
	}

	public void addServiceSession(String id, D3webKnowledgeServiceSession serviceSession) {
		serviceSessions.put(id, serviceSession);
	}

	public void removeServiceSession(String id) {
		serviceSessions.remove(id);
	}

	public void clear(Broker broker) {
		/*
		 * HOTFIX : reinit all d3webKnowledgeServiceSessions to update changed
		 * knowledgebases
		 */
		java.util.Set<String> serviceIDs = serviceSessions.keySet();
		serviceSessions = new HashMap<String, D3webKnowledgeServiceSession>();
		for (String string : serviceIDs) {
			removeServiceSession(string);
			D3webKnowledgeService service = environment.getService(string);
			addServiceSession(service.getId(), service.createSession(broker));
		}

	}

	public String getId() {
		return id;
	}
}
