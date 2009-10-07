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

package de.d3web.kernel.psMethods.diaFlux.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.actions.IAction;

/**
 * 
 * @author hatko
 *
 */
class Node implements INode, CaseObjectSource {

	
	private final List<IEdge> incoming;
	private final List<IEdge> outgoing;
	private final RuleAction action;
	
	public Node(RuleAction action) {
		
		if (action == null)
			throw new IllegalArgumentException("object must not be null.");
		
		this.action = action;
		this.incoming = new ArrayList<IEdge>();
		this.outgoing = new ArrayList<IEdge>();
		 
	}
	
	
	protected boolean addIncomingEdge(IEdge edge) {
		if (edge == null)
			throw new IllegalArgumentException("edge must not be null");
		
		if (edge.getEndNode() != this)
			throw new IllegalArgumentException("edge '" + edge + "' does not end at: " + this.toString());
		
		return incoming.add(edge);
		
	}
	
	protected boolean addOutgoingEdge(IEdge edge) {
		if (edge == null)
			throw new IllegalArgumentException("edge must not be null");
		
		if (edge.getStartNode() != this)
			throw new IllegalArgumentException("edge '" + edge + "' does not start at: " + this.toString());
		
		return outgoing.add(edge);
		
	}
	
	@Override
	public final List<IEdge> getIncomingEdges() {
		return Collections.unmodifiableList(incoming);
	}

	
	@Override
	public final List<IEdge> getOutgoingEdges() {
		return Collections.unmodifiableList(outgoing);
	}

	@Override
	public RuleAction getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((incoming == null) ? 0 : incoming.hashCode());
		result = prime * result
				+ ((outgoing == null) ? 0 : outgoing.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		if (!(obj instanceof Node))
			return false;
		
		Node other = (Node) obj;
		if (!action.equals(other.action))
			return false;
		
		if (!incoming.equals(other.incoming))
			return false;
		
		if (!outgoing.equals(other.outgoing))
			return false;
		
		return true;
	}


	@Override
	public XPSCaseObject createCaseObject() {
		return new NodeData(this);
	}
	
	
}
