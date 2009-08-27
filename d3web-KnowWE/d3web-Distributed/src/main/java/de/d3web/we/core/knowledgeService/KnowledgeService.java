package de.d3web.we.core.knowledgeService;

import java.util.Map;

import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public interface KnowledgeService {

	public String getId();
	
	public KnowledgeServiceSession createSession(Broker broker);
	
	public Map<TerminologyType, LocalTerminologyAccess> getTerminologies();

}
