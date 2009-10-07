package de.d3web.kernel.psMethods.diaFlux.flow;

import java.io.Serializable;

import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;

/**
 * 
 * @author hatko
 *
 */
public interface IEdge extends Serializable, CaseObjectSource {
	
	/**
	 * 
	 * @return s the node this edge starts at
	 */
	INode getStartNode();
	
	
	/**
	 * 
	 * @return s the node this edge ends at
	 */
	INode getEndNode();


	/**
	 * 
	 * @return s the edges predicate
	 */
	AbstractCondition getCondition();
	
	

}
