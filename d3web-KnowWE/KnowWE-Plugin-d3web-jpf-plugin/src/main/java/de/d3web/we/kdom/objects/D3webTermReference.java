package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webTermReference<TermObject> extends TermReference<TermObject> {

	@Override
	public String getTermName(Section<? extends KnowWETerm<TermObject>> s) {
		return KnowWEUtils.trimQuotes(s.getOriginalText());
	}

	@Override
	public abstract TermObject getTermObjectFallback(KnowWEArticle article,
			Section<? extends TermReference<TermObject>> s);
}
