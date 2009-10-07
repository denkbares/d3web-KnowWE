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
package de.d3web.kernel.psMethods.diaFlux.flow;

import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.predicates.IPredicate;


/**
 * @author hatko
 *
 */
class Edge implements IEdge {
	
	
	private final INode startNode;
	private final INode endNode;
	private final AbstractCondition condition;
	
	
	public Edge(INode startNode, INode endNode, AbstractCondition condition) {
//		if (startNode == null)
//			throw new IllegalArgumentException("startNode must not be null");
		
		if (endNode == null)
			throw new IllegalArgumentException("endNode must not be null");
		
		if (condition == null)
			throw new IllegalArgumentException("condition must not be null");
		
		this.startNode = startNode;
		this.endNode = endNode;
		this.condition = condition;
		
	}
	
	
	@Override
	public XPSCaseObject createCaseObject() {
		return new EdgeData(this);
	}
	

	@Override
	public AbstractCondition getCondition() {
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

}
