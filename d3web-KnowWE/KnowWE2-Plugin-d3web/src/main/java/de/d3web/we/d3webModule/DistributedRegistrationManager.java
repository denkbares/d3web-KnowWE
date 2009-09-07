package de.d3web.we.d3webModule;

import java.net.URL;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;

public class DistributedRegistrationManager {

	private static DistributedRegistrationManager instance = null;

	public static DistributedRegistrationManager getInstance() {
		if (instance == null) {
			instance = new DistributedRegistrationManager();

		}

		return instance;
	}

	public void registerKnowledgeBase(KnowledgeBaseManagement kbm, String topic, String webname) {
		

		KnowledgeBase base = kbm.getKnowledgeBase();

		base.setId(topic + ".."
				+ KnowWEEnvironment.generateDefaultID(topic));
		URL url = D3webModule.getKbUrl(webname, base.getId());
		DPSEnvironment env = D3webModule.getDPSE(webname);
		KnowledgeService service = new D3webKnowledgeService(base,
				base.getId(), url);
		env.addService(service, null, true);
		// KnowledgeBaseRepository.getInstance().addKnowledgeBase(
		// base.getId(), base);

		for (Broker broker : env.getBrokers()) {
			broker.register(service);
		}
	}

}
