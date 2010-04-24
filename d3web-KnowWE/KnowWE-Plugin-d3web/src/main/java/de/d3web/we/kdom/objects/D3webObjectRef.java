package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

public abstract class D3webObjectRef<T> extends ObjectRef<T> {



	@Override
	public boolean objectExisting(Section<? extends ObjectRef<T>> s) {
		return getObject(s) != null;
	}

	@Override
	public String getID(Section s) {
		String content = s.getOriginalText();

		String trimmed = content.trim();

		if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length() - 1).trim();
		}

		return trimmed;
	}
}
