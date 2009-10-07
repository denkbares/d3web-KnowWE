package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.psMethods.diaFlux.actions.IAction;
import de.d3web.kernel.psMethods.diaFlux.actions.NoopAction;

/**
 * 
 * @author hatko
 *
 */
public class EndNode extends Node {

	private final String name;
	
	public EndNode(String name) {
		super(NoopAction.INSTANCE);
		
		this.name = name;
	}
	
	
	@Override
	protected boolean addOutgoingEdge(IEdge edge) {
		throw new UnsupportedOperationException("can not add outgoing edge to end node");
	}
	
	public String getName() {
		return name;
	}
	
	
	

}
