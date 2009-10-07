package de.d3web.kernel.psMethods.diaFlux.flow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.d3web.kernel.dynamicObjects.XPSCaseObject;

public class FlowData extends XPSCaseObject {

	private final Map<INode, INodeData> nodeData;
	private final Map<IEdge, IEdgeData> edgeData;
	
	public FlowData(Flow flow, Map<INode, INodeData> nodeData, Map<IEdge, IEdgeData> edgeData) {
		super(flow);
		
		this.edgeData = Collections.unmodifiableMap(edgeData);
		this.nodeData = Collections.unmodifiableMap(nodeData);
	}

	public Map<INode, INodeData> getNodes() {
		return nodeData;
	}
	
	
	public INodeData getDataForNode(INode node) {
		if (!nodeData.containsKey(node))
			throw new IllegalArgumentException("Node '" + node + "' not found in flow '" + getSourceObject() + "'.");
		
		return nodeData.get(node); 
		
	}
	
	public IEdgeData getDataForEdge(IEdge edge) {
		if (!edgeData.containsKey(edge))
			throw new IllegalArgumentException("Edge '" + edge+ "' not found in flow '" + getSourceObject() + "'.");
			
		return edgeData.get(edge);
	}
	
	

}
