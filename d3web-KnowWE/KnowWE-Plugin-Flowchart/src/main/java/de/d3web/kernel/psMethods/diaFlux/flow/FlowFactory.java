package de.d3web.kernel.psMethods.diaFlux.flow;

import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.diaFlux.actions.IAction;
import de.d3web.kernel.psMethods.diaFlux.predicates.IPredicate;

/**
 * 
 * @author hatko
 *
 */
public class FlowFactory {
	
	
	private static final FlowFactory instance;
	
	static {
		instance = new FlowFactory();
	}
	
	
	public static FlowFactory getInstance() {
		return instance;
	}
	
	
	private FlowFactory() {
		
	}
	
	
	public Flow createFlowDeclaration(String name, List<INode> nodes, List<IEdge> edges) {
		return new Flow(name, nodes, edges);
		
	}
	
	public INode createNode(RuleAction action) {
		return new Node(action);
		
	}
	
	public IEdge createEdge(INode startNode, INode endNode, AbstractCondition condition) {
		return new Edge(startNode, endNode, condition);
	}
	
	
	
	

}
