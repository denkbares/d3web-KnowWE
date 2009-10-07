package de.d3web.kernel.psMethods.diaFlux.predicates;

import java.io.Serializable;

import de.d3web.kernel.XPSCase;

/**
 * 
 * @author hatko
 *
 */
public interface IPredicate extends Serializable {
	
	
	boolean evaluate(XPSCase theCase);
	

}
