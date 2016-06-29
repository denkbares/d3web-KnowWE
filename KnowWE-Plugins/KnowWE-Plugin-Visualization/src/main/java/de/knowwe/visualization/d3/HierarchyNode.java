/*
 * Copyright (C) 2013 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.visualization.d3;

import java.util.LinkedList;
import java.util.List;

import de.knowwe.visualization.ConceptNode;

public class HierarchyNode {

	private final List<HierarchyNode> children;
	private final ConceptNode source;
	private boolean isInSourceYet = false;

	public HierarchyNode(ConceptNode source) {
		if (source == null) throw new NullPointerException();
		this.source = source;
		children = new LinkedList<>();
	}

	public boolean hasChildren() {
		return (!children.isEmpty());
	}

	public boolean hasChild(HierarchyNode hn) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getConceptNode().equals(hn.getConceptNode())) {
				return true;
			}
		}
		return false;
	}

	public void addChild(HierarchyNode hn) {
		if (!hasChild(hn)) children.add(hn);
	}

	public String getName() {
		return source.getName();
	}

	public String getLabel() {
		return source.getConceptLabel();
	}

	public ConceptNode getConceptNode() {
		return source;
	}

	public List<HierarchyNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return source.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof HierarchyNode) {
			return source.equals(((HierarchyNode) obj).source);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return source.hashCode();
	}

	public boolean isInSourceYet() {
		return isInSourceYet;
	}

	public void setIsInSourceYet(boolean b) {
		isInSourceYet = b;
	}

}
