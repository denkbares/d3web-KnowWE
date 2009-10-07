package de.d3web.kernel.psMethods.diaFlux;

import java.io.Serializable;

import de.d3web.kernel.psMethods.diaFlux.flow.IEdge;
import de.d3web.kernel.psMethods.diaFlux.flow.INodeData;

/**
 * 
 * @author Reinhard Hatko
 * Created: 10.09.2009
 *
 */
public class PathEntry implements Serializable {
	
	
	private final PathEntry path;
	private final PathEntry stack;
	private final INodeData nodeData;
	private final IEdge edge;

	/**
	 * @param path
	 * @param stack
	 * @param nodeData 
	 * @param edge 
	 */
	public PathEntry(PathEntry path, PathEntry stack, INodeData nodeData, IEdge edge) {
		this.path = path;
		this.stack = stack;
		this.nodeData = nodeData;
		this.edge = edge;
	}
	
	
	public PathEntry getPath() {
		return path;
	}
	
	
	public PathEntry getStack() {
		return stack;
	}
	
	public IEdge getEdge() {
		return edge;
	}
	
	public INodeData getNodeData() {
		return nodeData;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[" + nodeData + "]" + Integer.toHexString(hashCode());
	}
	
	
	

}
