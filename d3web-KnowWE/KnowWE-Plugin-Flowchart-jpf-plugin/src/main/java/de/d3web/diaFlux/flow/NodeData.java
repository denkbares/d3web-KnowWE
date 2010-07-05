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
import java.util.List;

import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.inference.PathEntry;

public class NodeData extends SessionObject implements INodeData {

	
	private final List<PathEntry> support;
	private int refCounter;
	
	public NodeData(INode node) {
		super(node);
		
		support = new ArrayList<PathEntry>(2);
		refCounter = 0;
	}

	@Override
	public INode getNode() {
		return (INode) getSourceObject();
	}

	@Override
	public boolean isActive() {
		return !support.isEmpty();
	}
	
	
	@Override
	public boolean addSupport(PathEntry entry) {
		return support.add(entry);
	}
	
	@Override
	public boolean removeSupport(PathEntry entry) {
		return support.remove(entry);
	}
	
	@Override
	public int decReferenceCounter() {
		refCounter = refCounter - 1;
		return refCounter;
	}
	
	@Override
	public int incReferenceCounter() {
		refCounter = refCounter + 1;
		return refCounter;
	}
	
	@Override
	public int getReferenceCounter() {
		return refCounter;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getNode() + ", active=" + isActive() + ", refs=" + getReferenceCounter()+ "]" + Integer.toHexString(hashCode());
	}

}
