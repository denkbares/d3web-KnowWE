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
