package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;

public class NoSuchObjectError extends KDOMError{
	
	private String name;
	
	public NoSuchObjectError(String name) {
		this.name = name;
	}

	@Override
	public String getVerbalization() {
		// TODO Auto-generated method stub
		return "Object not found: "+name;
	}

}
