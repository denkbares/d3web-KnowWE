package de.d3web.we.core.broker;

import java.rmi.server.UID;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
		blackboard = new BlackboardImpl(environment);
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
			if(serviceSessions.get(id).equals(serviceSession)) {
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
		
		/* HOTFIX : reinit all d3webKnowledgeServiceSessions to update changed knowledgebases */
		java.util.Set<String> serviceIDs = serviceSessions.keySet();
		serviceSessions = new HashMap<String,KnowledgeServiceSession>();
		for (String string : serviceIDs) {
			removeServiceSession(string);
			KnowledgeService service = environment.getService(string);
			addServiceSession(service.getId(),service.createSession(broker));
		}
		
	}

	public String getId() {
		return id;
	}

	public DPSEnvironment getEnvironment() {
		return environment;
	}
	
}
