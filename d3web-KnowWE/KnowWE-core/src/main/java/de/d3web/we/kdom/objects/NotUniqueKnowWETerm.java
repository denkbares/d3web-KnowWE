package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public interface NotUniqueKnowWETerm<TermObject> {

	public String getUniqueTermIdentifier(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s);

}
