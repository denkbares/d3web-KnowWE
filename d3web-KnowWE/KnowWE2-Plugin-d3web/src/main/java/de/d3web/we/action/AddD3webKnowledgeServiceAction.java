package de.d3web.we.action;

import java.net.URL;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public abstract class AddD3webKnowledgeServiceAction implements KnowWEAction {


//	public String perform(KnowWEParameterMap map) {
//		
//		String baseID = map.get(KnowWEAttributes.KNOWLEDGEBASE_ID);
//		
//		if(baseID == null) {
//			return "baseID is null";
//		}
//		
//		URL url = KnowWEUtils.getKbUrl(map, baseID);
//		addService(map, base, url);
//		return "done";
//	}

	protected void addService(KnowWEParameterMap map, KnowledgeBase base, URL url)
			throws Exception {
		if(url == null) return;
		DPSEnvironment env = D3webModule.getDPSE(map);
		if(env.getService(base.getId()) != null) {
			map.put(KnowWEAttributes.KNOWLEDGEBASE_ID, base.getId());
			new RemoveD3webKnowledgeServiceAction().perform(map);
		}
		
		String clusterID = map.get(KnowWEAttributes.CLUSTERID);
		KnowledgeService service = new D3webKnowledgeService(base, base.getId(), url);
		env.addService(service, clusterID, true);
		//KnowledgeBaseRepository.getInstance().addKnowledgeBase(base.getId(), base);
		
		for (Broker broker : env.getBrokers()) {
			broker.register(service);
		}
		
		//model.removeAttribute(KnowWEAttributes.KNOWLEDGEBASE, model.getWebApp());
	}

}
