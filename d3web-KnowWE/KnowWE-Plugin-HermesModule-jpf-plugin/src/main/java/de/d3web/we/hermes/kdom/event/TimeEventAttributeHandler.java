package de.d3web.we.hermes.kdom.event;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * A very simple abstract SubtreeHandler that only executes the crateAttribute()
 * hook if there is no error in its fahters subtree
 * 
 * @author Jochen
 * @created 11.10.2010
 * @param <T>
 */
public abstract class TimeEventAttributeHandler<T extends KnowWEObjectType> extends SubtreeHandler<T> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<T> s) {
		if (s.getFather().hasErrorInSubtree(article)) {
			return new ArrayList<KDOMReportMessage>(0);
		}
		else {
			createAttribute(article, s);
		}
		return null;
	}

	protected abstract Collection<KDOMReportMessage> createAttribute(KnowWEArticle article, Section<T> s);

	// @Override
	// public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
	// return super.needsToCreate(article, s)
	// || DashTreeUtils.isChangeInAncestorSubtree(article, s, 1);
	// }
	//
	// @Override
	// public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
	// return super.needsToDestroy(article, s)
	// || DashTreeUtils.isChangeInAncestorSubtree(article, s, 1);
	//
	// }
}
