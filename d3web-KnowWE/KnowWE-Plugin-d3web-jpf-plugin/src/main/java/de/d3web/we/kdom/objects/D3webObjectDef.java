package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

public abstract class D3webObjectDef<T> extends ObjectDef<T> {

	public D3webObjectDef(String key) {
		super(key);
	}

	@Override
	public String getTermName(Section<? extends TermReference<T>> s) {
		String content = s.getOriginalText();

		String trimmed = content.trim();

		if(trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length()-1).trim();
		}

		return trimmed;
	}





}
