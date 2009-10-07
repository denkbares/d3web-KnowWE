package de.d3web.kernel.psMethods.diaFlux.predicates;

import java.io.Serializable;

import de.d3web.kernel.XPSCase;

/**
 * 
 * @author hatko
 *
 */
public class PredicateNot implements IPredicate {
	
	private final IPredicate predicate;


	public PredicateNot(IPredicate predicate) {
		this.predicate = predicate;
	}
	
	
	@Override
	public boolean evaluate(XPSCase theCase) {
		return !predicate.evaluate(theCase);
	}
	
	
	

	

}
