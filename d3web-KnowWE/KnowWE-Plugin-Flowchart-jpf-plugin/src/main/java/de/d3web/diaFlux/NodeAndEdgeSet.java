/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.diaFlux;

import java.util.Collections;
import java.util.Set;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.inference.FluxSolver;

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
	public boolean isUsed(Session theCase) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
