package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webObjectDef<TermObject> extends ObjectDef<TermObject> {

	public D3webObjectDef(String key) {
		super(key);
	}

	@Override
	public String getTermName(Section<? extends TermReference<TermObject>> s) {
		return KnowWEUtils.trimAndRemoveQuotes(s.getOriginalText());
	}

}
