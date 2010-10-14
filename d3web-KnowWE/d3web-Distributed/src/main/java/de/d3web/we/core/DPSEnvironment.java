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

package de.d3web.we.core;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.broker.BrokerImpl;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;

public class DPSEnvironment {

	private final URL environmentLocation;
	private Map<String, D3webKnowledgeService> services;
	private Map<String, Broker> brokers;

	public DPSEnvironment(URL url) {
		super();
		environmentLocation = url;
		services = new HashMap<String, D3webKnowledgeService>();
		brokers = new HashMap<String, Broker>();
		initialize();
	}

	private void clear() {
		services = new HashMap<String, D3webKnowledgeService>();
		brokers = new HashMap<String, Broker>();
	}

	synchronized public void reInitialize() {
		clear();
		initialize();
	}

	public void initialize() {
		File dir = null;
		if (dir == null || !dir.isDirectory()) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: initialization failed: " + environmentLocation);
			return;
		}
	}

	public void addService(D3webKnowledgeService service) {
		D3webKnowledgeService oldService = getService(service.getId());
		if (oldService != null) {
			removeService(oldService);
		}

		services.put(service.getId(), service);

	}

	public void removeService(D3webKnowledgeService service) {
		services.remove(service.getId());
	}

	public Broker createBroker(String userID) {
		Broker result = new BrokerImpl(this, userID);
		for (D3webKnowledgeService each : services.values()) {
			result.register(each);
		}
		return result;
	}

	public D3webKnowledgeService getService(String id) {
		return services.get(id);
	}

	public Collection<D3webKnowledgeService> getServices() {
		return services.values();
	}

	public D3webKnowledgeServiceSession createServiceSession(String id, Broker broker) {
		D3webKnowledgeService service = services.get(id);
		if (service != null) {
			return service.createSession(broker);
		}
		return null;
	}

	public Broker getBroker(String userID) {
		Broker result = brokers.get(userID);
		if (result == null) {
			Broker broker = createBroker(userID);
			brokers.put(userID, broker);
			result = broker;
		}
		return result;
	}

	public Collection<Broker> getBrokers() {
		return brokers.values();
	}
}
