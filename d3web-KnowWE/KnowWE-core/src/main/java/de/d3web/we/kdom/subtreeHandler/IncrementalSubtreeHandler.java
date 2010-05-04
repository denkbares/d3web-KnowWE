package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;

public abstract class IncrementalSubtreeHandler<T extends KnowWEObjectType> implements SubtreeHandler<T> {

	@Override
	
	public final KDOMReportMessage reviseSubtree(KnowWEArticle article, Section<T> s) {
		return generateKnowledge(article, s);
	}

	public abstract KDOMReportMessage generateKnowledge(KnowWEArticle article, Section<T> s);
	
	public abstract void deleteKnowledge(KnowWEArticle article, Section<T> s);

}
