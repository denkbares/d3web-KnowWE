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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.broker.BrokerImpl;
import de.d3web.we.core.dialog.DistributedDialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.terminology.TerminologyServer;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public class DPSEnvironment {

	private URL environmentLocation;
	private TerminologyServer terminologyServer;
	private Map<String, de.d3web.we.core.knowledgeService.KnowledgeService> services;
	private Map<String, Broker> brokers;

	public DPSEnvironment(URL url) {
		super();
		setEnvironmentLocation(url);
		terminologyServer = new TerminologyServer();
		services = new HashMap<String, KnowledgeService>();
		brokers = new HashMap<String, Broker>();
		initialize();
	}

	public void setEnvironmentLocation(URL url) {
		environmentLocation = url;
	}

	private void clear() {
		terminologyServer = new TerminologyServer();
		services = new HashMap<String, KnowledgeService>();
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

	public void addService(KnowledgeService service) {
		KnowledgeService oldService = getService(service.getId());
		if (oldService != null) {
			removeService(oldService);
		}

		services.put(service.getId(), service);

		for (TerminologyType eachType : service.getTerminologies().keySet()) {
			LocalTerminologyAccess eachAccess = service.getTerminologies().get(eachType);
			terminologyServer.getStorage().register(service.getId(), eachType, eachAccess);
		}
	}

	public void removeService(KnowledgeService service) {
		Map<TerminologyType, LocalTerminologyAccess> map = service.getTerminologies();
		for (TerminologyType type : new ArrayList<TerminologyType>(map.keySet())) {
			terminologyServer.removeTerminology(service.getId(), type);
		}
		services.remove(service.getId());
	}

	public Broker createBroker(String userID) {
		Broker result = new BrokerImpl(this, userID, new DistributedDialogControl());
		for (KnowledgeService each : services.values()) {
			result.register(each);
		}
		return result;
	}

	public KnowledgeService getService(String id) {
		return services.get(id);
	}

	public Collection<KnowledgeService> getServices() {
		return services.values();
	}

	public Collection<String> getFriendlyServices(String serviceId) {
		return new ArrayList<String>();
	}

	public KnowledgeServiceSession createServiceSession(String id, Broker broker) {
		KnowledgeService service = services.get(id);
		if (service != null) {
			return service.createSession(broker);
		}
		return null;
	}

	public TerminologyServer getTerminologyServer() {
		return terminologyServer;
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
