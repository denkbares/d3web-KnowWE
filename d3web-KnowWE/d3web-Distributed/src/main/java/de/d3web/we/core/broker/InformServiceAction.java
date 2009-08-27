package de.d3web.we.core.broker;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class InformServiceAction implements ServiceAction {

	private final Information info;
	private final DPSSession session;
	
	public InformServiceAction(Information info, DPSSession session) {
		super();
		this.info = info;
		this.session = session;
	}
	
	public void run() {
		KnowledgeServiceSession serviceSession = session.getServiceSession(info.getNamespace());
		if(serviceSession != null) {
			serviceSession.inform(info);
		}
		if(info.getInformationType().equals(InformationType.AlignedUserInformation)) {
			session.getBlackboard().update(info);
		}
	}

}
