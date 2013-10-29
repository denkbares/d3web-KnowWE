package de.knowwe.kdom.subtreehandler;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;

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
		super(false);
	}

}
