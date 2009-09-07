package de.d3web.we.action;

import java.util.Map;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWEUserContextImpl implements KnowWEUserContext {

	
	private String user;
	private Map<String,String> params;
	
	public KnowWEUserContextImpl(String user, Map<String,String> parameters) {
		this.user = user;
		this.params = parameters;
	}
	
	@Override
	public String getUsername() {
		return user;
	}

	@Override
	public boolean userIsAdmin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> getUrlParameterMap() {
		return params;
	}
	

}
