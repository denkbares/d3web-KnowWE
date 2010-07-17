package de.d3web.we.kdom.report;

import de.d3web.we.kdom.AbstractKnowWEObjectType;

/**
 * Abstract class for a message denoting a serious error in the
 * parsing/compilation process
 * 
 * Will be rendered by the ErrorRenderer specified by getErrorRenderer() of the
 * KnowWEObjectType
 * 
 * @see @link {@link AbstractKnowWEObjectType}
 * 
 * 
 * @author Jochen
 *
 */
public abstract class KDOMError extends KDOMReportMessage {

	@Override
	public abstract String getVerbalization();

}
