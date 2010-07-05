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

package de.d3web.diaFlux.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.inference.PathEntry;

public class FlowData extends SessionObject {

	private final Map<INode, INodeData> nodeData;
	
	
	public FlowData(Flow flow, Map<INode, INodeData> nodeData) {
		super(flow);
		
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
	

}
