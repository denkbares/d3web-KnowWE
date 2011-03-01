package de.d3web.we.kdom.subtreehandler;

import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * 
 * A general subtreeHandler that is always executed if initialized for a type
 * (global scope)
 * 
 * @author Jochen
 * @created 17.12.2010
 */
public abstract class GeneralSubtreeHandler<T extends Type> extends SubtreeHandler<T> {

	public GeneralSubtreeHandler() {
		super(true);
	}

}
