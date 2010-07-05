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

/**
 * 
 */
package de.d3web.diaFlux.flow;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.SessionObject;


/**
 * @author Reinhard Hatko
 *
 */
class Edge implements IEdge {
	
	private final INode startNode;
	private final INode endNode;
	private final Condition condition;
	private final String id;
	
	
	public Edge(String id, INode startNode, INode endNode, Condition condition) {
		
		if (endNode == null)
			throw new IllegalArgumentException("endNode must not be null");
		
		if (condition == null)
			throw new IllegalArgumentException("condition must not be null");
		
		this.startNode = startNode;
		this.endNode = endNode;
		this.condition = condition;
		this.id = id;
		
		if (startNode != null)
			((Node) startNode).addOutgoingEdge(this);
		
				
	}
	
	

	@Override
	public Condition getCondition() {
		return condition;
	}

	@Override
	public INode getEndNode() {
		return endNode;
	}

	@Override
	public INode getStartNode() {
		return startNode;
	}
	
	@Override
	public String toString() {
		return "Edge [" + getStartNode() + " -> " + getEndNode() + "]@" +Integer.toHexString(hashCode()); 
	}


	@Override
	public String getID() {
		return id;
	}

}
