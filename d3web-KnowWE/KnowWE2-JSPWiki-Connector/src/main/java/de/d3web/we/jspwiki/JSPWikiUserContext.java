package de.d3web.we.jspwiki;

import java.security.Principal;
import java.util.Map;

import com.ecyrd.jspwiki.WikiContext;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class JSPWikiUserContext implements KnowWEUserContext{
	
	private WikiContext context;
	private Map<String,String> urlParameter;
	 
	public JSPWikiUserContext (WikiContext context, Map<String,String> urlParameter) {
		this.context = context;
		this.urlParameter = urlParameter;
	}
	
	public JSPWikiUserContext (WikiContext context) {
		this.context = context;
	}
	
	
	
	public void setUrlParameter(Map<String, String> urlParameter) {
		this.urlParameter = urlParameter;
	}

	public String getUsername() {
		return context.getWikiSession().getUserPrincipal().getName();
	}

	@Override
	public boolean userIsAdmin() {

		// returns true if User is in Admin-Group
		Principal[] princ = context.getWikiSession().getRoles();
		
		for (Principal p : princ) {
			if (p.getName().equals("Admin")) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Map<String, String> getUrlParameterMap() {
		return urlParameter;
	}
	
}
