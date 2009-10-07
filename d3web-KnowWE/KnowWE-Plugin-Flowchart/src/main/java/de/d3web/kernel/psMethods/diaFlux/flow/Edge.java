/**
 * 
 */
package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.predicates.IPredicate;


/**
 * @author hatko
 *
 */
class Edge implements IEdge {
	
	
	private final INode startNode;
	private final INode endNode;
	private final AbstractCondition condition;
	
	
	public Edge(INode startNode, INode endNode, AbstractCondition condition) {
//		if (startNode == null)
//			throw new IllegalArgumentException("startNode must not be null");
		
		if (endNode == null)
			throw new IllegalArgumentException("endNode must not be null");
		
		if (condition == null)
			throw new IllegalArgumentException("condition must not be null");
		
		this.startNode = startNode;
		this.endNode = endNode;
		this.condition = condition;
		
	}
	
	
	@Override
	public XPSCaseObject createCaseObject() {
		return new EdgeData(this);
	}
	

	@Override
	public AbstractCondition getCondition() {
		return condition;
	}

	@Override
	public INode getEndNode() {
		return endNode;
	}

	@Override
	public INode getStartNode() {
		return startNode;
	}
	
	@Override
	public String toString() {
		return "Edge [" + getStartNode() + " -> " + getEndNode() + "]@" +Integer.toHexString(hashCode()); 
	}

}
