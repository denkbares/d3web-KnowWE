package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webObjectRef<TermObject> extends ObjectRef<TermObject> {

	@Override
	public String getTermName(Section<? extends TermReference<TermObject>> s) {
		return KnowWEUtils.trimAndRemoveQuotes(s.getOriginalText());
	}

	@Override
	public abstract TermObject getObjectFallback(KnowWEArticle article,
			Section<? extends ObjectRef<TermObject>> s);
}
