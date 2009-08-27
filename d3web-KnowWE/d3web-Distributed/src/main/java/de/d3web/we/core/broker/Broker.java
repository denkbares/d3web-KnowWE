package de.d3web.we.core.broker;

import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.core.dialog.DialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public interface Broker {
	
	void update(Information info);

	void processInit();
	
	Information request(Information requestInfo, KnowledgeServiceSession serviceSession);
	
	void register(KnowledgeService service);
	
	void signoff(KnowledgeService service);
	
	DPSSession getSession();

	void clearDPSSession();

	void delegate(List<Information> infos, String targetNamespace, boolean temporary, boolean instantly, String comment, KnowledgeServiceSession kss);
	
	void activate(KnowledgeServiceSession kss, KnowledgeServiceSession reason, boolean userIndicated, boolean instantly, String comment);
	
	void finished(KnowledgeServiceSession kss);
	
	DialogControl getDialogControl();
	
}
