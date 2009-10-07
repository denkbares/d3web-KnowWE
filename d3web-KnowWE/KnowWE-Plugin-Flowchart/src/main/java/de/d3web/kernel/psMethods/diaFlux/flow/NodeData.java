package de.d3web.kernel.psMethods.diaFlux.flow;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.PathEntry;

public class NodeData extends XPSCaseObject implements INodeData {

	
	private boolean isActive;
	private final List<PathEntry> support;
	
	public NodeData(INode node) {
		super(node);
		
		support = new ArrayList<PathEntry>(2);
		isActive = false;
	}

	@Override
	public INode getNode() {
		return (INode) getSourceObject();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}
	
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
	@Override
	public boolean addSupport(PathEntry entry) {
		return support.add(entry);
	}
	
	@Override
	public boolean removeSupport(PathEntry entry) {
		return support.remove(entry);
	}
	
	@Override
	public int getReferenceCounter() {
		return support.size();
	}

}
