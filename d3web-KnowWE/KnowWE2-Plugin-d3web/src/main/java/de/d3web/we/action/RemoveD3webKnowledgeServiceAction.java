package de.d3web.we.action;


import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class RemoveD3webKnowledgeServiceAction implements KnowWEAction {


	public String perform(KnowWEParameterMap map) {
		String baseID = map.get(KnowWEAttributes.KNOWLEDGEBASE_ID);
		if(baseID == null) return "no kbid to remove knowledge service";
		DPSEnvironment env = D3webModule.getDPSE(map);
		KnowledgeService service = env.getService(baseID);
		if(service == null) return "no service found for id: "+baseID;
		env.removeService(service);
		for (Broker broker : env.getBrokers()) {
			broker.signoff(service);
		}
		//KnowledgeBaseRepository.getInstance().removeKnowledgeBase(baseID);
		return "done";
	}

}
