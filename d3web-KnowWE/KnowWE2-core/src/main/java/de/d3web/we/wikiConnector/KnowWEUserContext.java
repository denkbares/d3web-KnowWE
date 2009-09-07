package de.d3web.we.wikiConnector;

import java.util.Map;

public interface KnowWEUserContext {
	
	public boolean userIsAdmin();
	
	public String getUsername();
	
	public Map<String,String> getUrlParameterMap();

}
