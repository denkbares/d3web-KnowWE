package de.d3web.kernel.psMethods.diaFlux.predicates;

import de.d3web.kernel.XPSCase;

/**
 * 
 * @author hatko
 *
 */
public class PredicateTrue implements IPredicate {

	@Override
	public boolean evaluate(XPSCase theCase) {
		return true;
	}

}
