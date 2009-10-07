package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.psMethods.diaFlux.actions.IAction;
import de.d3web.kernel.psMethods.diaFlux.actions.NoopAction;

/**
 * 
 * @author hatko
 *
 */
public class StartNode extends Node {

	private final String name;
	
	public StartNode(String name) {
		super(NoopAction.INSTANCE);
		
		this.name = name;
	}
	
	
	@Override
	protected boolean addIncomingEdge(IEdge edge) {
		throw new UnsupportedOperationException("can not add incoming edge to start node");
	}
	
	public String getName() {
		return name;
	}
	

}
