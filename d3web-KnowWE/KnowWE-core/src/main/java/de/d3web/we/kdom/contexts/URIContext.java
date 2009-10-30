package de.d3web.we.kdom.contexts;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import de.d3web.we.kdom.Section;

public class URIContext implements Context {

	protected Map<String, URI> attributes;

	public URIContext() {
		attributes = new HashMap<String, URI>();
	}

	@Override
	public HashMap<String, URI> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValidForSection(Section s) {
		return true;
	}

}
