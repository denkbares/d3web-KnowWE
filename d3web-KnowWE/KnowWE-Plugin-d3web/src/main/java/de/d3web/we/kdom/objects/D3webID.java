package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

public abstract class D3webID extends ObjectRef {

	@Override
	public String getID(Section<? extends ObjectRef> s) {
		String content = s.getOriginalText();
		
		String trimmed = content.trim();
		
		if(trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length()-1).trim();
		}
		
		return trimmed;
	}

	

}
