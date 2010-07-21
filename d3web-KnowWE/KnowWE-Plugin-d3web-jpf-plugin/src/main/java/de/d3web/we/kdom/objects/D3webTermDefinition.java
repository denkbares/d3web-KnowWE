package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webTermDefinition<TermObject> extends TermDefinition<TermObject> {

	public D3webTermDefinition(String key) {
		super(key);
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<TermObject>> s) {
		return KnowWEUtils.trimQuotes(s.getOriginalText());
	}

}
