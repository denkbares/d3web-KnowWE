package de.d3web.kernel.psMethods.diaFlux;

import java.util.Collections;
import java.util.Set;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.diaFlux.flow.IEdge;
import de.d3web.kernel.psMethods.diaFlux.flow.INode;

/**
 * Encapsulates a set of edges and nodes that all reference a 
 * certain NamedObject.
 * 
 * @author Reinhard Hatko
 * Created: 15.09.2009
 *
 */
public class NodeAndEdgeSet implements KnowledgeSlice {
	
	private final Set<IEdge> edges;
	private final Set<INode> nodes;
	

	/**
	 * @param edges
	 * @param nodes
	 */
	public NodeAndEdgeSet(Set<IEdge> edges, Set<INode> nodes) {
		this.edges = Collections.unmodifiableSet(edges);
		this.nodes = Collections.unmodifiableSet(nodes);
	}
	
	public Set<IEdge> getEdges() {
		return edges;
	}
	
	public Set<INode> getNodes() {
		return nodes;
	}
	
	
	

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getProblemsolverContext() {
		return FluxSolver.class;
	}

	@Override
	public boolean isUsed(XPSCase theCase) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
