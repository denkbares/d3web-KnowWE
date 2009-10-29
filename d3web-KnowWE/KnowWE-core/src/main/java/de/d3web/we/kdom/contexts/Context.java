package de.d3web.we.kdom.contexts;

import java.util.Map;

import de.d3web.we.kdom.Section;

public interface Context {
	
	
	public  Map<String, ? extends Object> getAttributes();
	
	public  boolean isValidForSection(Section s);
	
	public String getCID();

}
