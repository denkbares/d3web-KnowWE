package de.d3web.kernel.psMethods.diaFlux.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;

/**
 * @author hatko
 *
 */
public class Flow implements Serializable, CaseObjectSource {
	
	private final List<IEdge> edges;
	private final List<INode> nodes;
	private final String name;
	
	
	public Flow(String name, List<INode> nodes, List<IEdge> edges) {
		
		if (nodes == null)
			throw new IllegalArgumentException("nodes is null");

		if (edges == null)
			throw new IllegalArgumentException("edges is null");
		
		if (name == null)
			throw new IllegalArgumentException("name is null");
		
		this.nodes = Collections.unmodifiableList(nodes);
		this.edges = Collections.unmodifiableList(edges);
		this.name = name;
		
		checkFlow();
	}

	/**
	 * Checks the consistency of nodes and edges
	 */
	private void checkFlow() {
		
		
	}
	
	@Override
	public XPSCaseObject createCaseObject() {
		
		Map<INode, INodeData> nodedata = new HashMap<INode, INodeData>(getNodes().size());
		
		for (INode nodeDecl : getNodes()) {
			nodedata.put(nodeDecl, (INodeData) nodeDecl.createCaseObject());
		}
		
		Map<IEdge, IEdgeData> edgedata = new HashMap<IEdge, IEdgeData>(getEdges().size());
		

		for (IEdge edge : getEdges()) {
			edgedata.put(edge, (IEdgeData) edge.createCaseObject());
		}
		
		return new FlowData(this, nodedata, edgedata);
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + edges.hashCode();
		result = prime * result + name.hashCode();
		result = prime * result + nodes.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Flow other = (Flow) obj;
		
		if (!edges.equals(other.edges))
			return false;
	
		if (!name.equals(other.name))
			return false;
		
		if (!nodes.equals(other.nodes))
			return false;
		
		return true;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public List<IEdge> getEdges() {
		return edges;
	}
	
	public List<INode> getNodes() {
		return nodes;
	}
	
	public List<INode> getStartNodes() {
		
		List<INode> result = new ArrayList<INode>(1);
		
		for (INode node : nodes) {
			if (node instanceof StartNode)
				result.add(node);
		}
		
		return result;
		
	}
	
	@Override
	public String toString() {
		return "Flow [" + getName() + ", " + nodes.size() + " nodes, " + edges.size() + " edges] "  + "@" + Integer.toHexString(hashCode());
	}

	

}
