package de.d3web.we.kdom.contexts;

import java.util.HashMap;

/**
 * 
 * 
 * @author Fabian Haupt
 * 
 */
public abstract class Context {
	protected HashMap<String, String> attributes;

	public abstract String getCID();
	
	public Context() {
		attributes = new HashMap<String, String>();
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	

}
