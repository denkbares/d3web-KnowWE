/**
 * 
 */
package de.d3web.kernel.psMethods.diaFlux.flow;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.domainModel.RuleAction;

/**
 * @author hatko
 *
 */
public interface INode extends Serializable, CaseObjectSource {
	
	
	/**
	 * 
	 * @return s a list of this node's incoming edges. 
	 */
	List<IEdge> getIncomingEdges();
	
	/**
	 * 
	 * @return s a list of this node's outgoing edges.
	 */
	List<IEdge> getOutgoingEdges();
	
	
	/**
	 * 
	 * @return s the action this node is doing when reached
	 */
	RuleAction getAction();
	
	
	
	
	
	

}
