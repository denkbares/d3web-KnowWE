package de.d3web.we.core.knowledgeService;

import java.util.List;

import de.d3web.we.basic.Information;

public interface KnowledgeServiceSession {

	void inform(Information info);

	void processInit();
	
	void request(List<Information> infos);
	
	boolean isFinished();
		
	void clear();
	
	String getNamespace();
	
}
