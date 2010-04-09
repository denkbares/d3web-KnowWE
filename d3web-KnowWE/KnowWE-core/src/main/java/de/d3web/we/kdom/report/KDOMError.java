package de.d3web.we.kdom.report;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class KDOMError extends KDOMReportMessage {
	
	@Override
	public abstract String getVerbalization(KnowWEUserContext usercontext);

}
