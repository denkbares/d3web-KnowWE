package de.d3web.we.knowRep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KnowledgeRepresentationManager {
	
	private Map<String, KnowledgeRepresentationHandler> handlers = new HashMap<String, KnowledgeRepresentationHandler>();
	
	public void registerHandler(String key, KnowledgeRepresentationHandler handler) {
		handlers.put(key, handler);
		
	}
	
	
	public void initArticle(String name) {
		for(KnowledgeRepresentationHandler handler : handlers.values()) {
			handler.initArticle(name);
		}
	}
	
	public KnowledgeRepresentationHandler getHandler(String key) {
		return handlers.get(key);
	}
	
	public Collection<KnowledgeRepresentationHandler> getHandlers() {
		return handlers.values();
	}

}
