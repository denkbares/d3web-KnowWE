package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.psMethods.diaFlux.PathEntry;

/**
 * 
 * @author Reinhard Hatko
 * Created: 10.09.2009
 *
 */
public interface INodeData {
	
	
	INode getNode();
	
	boolean isActive();

	void setActive(boolean b);
	
	boolean addSupport(PathEntry entry);
	
	boolean removeSupport(PathEntry entry);
	
	int getReferenceCounter();
	

}
