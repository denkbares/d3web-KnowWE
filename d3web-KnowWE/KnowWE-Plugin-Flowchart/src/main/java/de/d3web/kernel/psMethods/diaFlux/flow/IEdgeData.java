/**
 * 
 */
package de.d3web.kernel.psMethods.diaFlux.flow;

import java.io.Serializable;

/**
 * @author Reinhard Hatko
 * Created: 14.09.2009
 *
 */
public interface IEdgeData extends Serializable {
	
	
	IEdge getEdge();
	
	
	boolean getEvaluation();
	
	boolean hasFired();
	
	void fire();
	
	void unfire();
	

}
