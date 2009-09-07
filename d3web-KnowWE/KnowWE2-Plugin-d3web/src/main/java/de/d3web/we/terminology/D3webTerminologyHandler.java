package de.d3web.we.terminology;

import java.util.HashMap;
import java.util.Map;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

public class D3webTerminologyHandler implements KnowledgeRepresentationHandler {

	private Map<String, KnowledgeBaseManagement> kbms = new HashMap<String, KnowledgeBaseManagement>();

	// public D3webTerminologyHandler

	public KnowledgeBaseManagement getKBM(String name) {
		return kbms.get(name);
	}

	@Override
	public void initArticle(String name) {
		DPSEnvironment env = D3webModule.getDPSE("default_web");
		String id = name + ".." + KnowWEEnvironment.generateDefaultID(name);
		KnowledgeService service = env.getService(id);
		if (service != null) {
			env.removeService(service);
			for (Broker broker : env.getBrokers()) {
				broker.signoff(service);
			}
		}
		kbms.put(name, KnowledgeBaseManagement.createInstance());

	}

	@Override
	public void finishedArticle(String name) {
			KnowledgeBaseManagement kbm = this.getKBM(name);
			if(kbm.getKnowledgeBase().getAllKnowledgeSlices().size() > 0 || kbm.getKnowledgeBase().getQuestions().size() > 1 || kbm.getKnowledgeBase().getDiagnoses().size() > 1 ) {
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, name, "default_web");
			}
	}
	

	

}
