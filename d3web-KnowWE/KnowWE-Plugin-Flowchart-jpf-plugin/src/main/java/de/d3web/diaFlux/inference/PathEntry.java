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

package de.d3web.diaFlux.inference;

import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.INodeData;
import de.d3web.diaFlux.flow.ISupport;

/**
 * 
 * @author Reinhard Hatko
 * Created: 10.09.2009
 *
 */
public class PathEntry {
	
	
	private final PathEntry path;
	private final PathEntry stack;
	private final INodeData nodeData;
	private final ISupport support;

	/**
	 * @param path
	 * @param stack
	 * @param nodeData 
	 * @param edge 
	 */
	public PathEntry(PathEntry path, PathEntry stack, INodeData nodeData, ISupport support) {
		this.path = path;
		this.stack = stack;
		this.nodeData = nodeData;
		this.support = support; 
	}
	
	public PathEntry getPath() {
		return path;
	}
	
	public PathEntry getStack() {
		return stack;
	}
	
//	public IEdge getEdge() {
//		return edge;
//	}
	
	/**
	 * @return the support
	 */
	public ISupport getSupport() {
		return support;
	}
	
	
	public INodeData getNodeData() {
		return nodeData;
	}
	
	public INode getNode() {
		return getNodeData().getNode();
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + nodeData + "]@" + Integer.toHexString(hashCode());
	}
	

}
