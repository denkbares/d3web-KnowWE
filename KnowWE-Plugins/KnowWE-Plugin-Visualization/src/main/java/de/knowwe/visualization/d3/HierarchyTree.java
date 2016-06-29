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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.SubGraphData;

/**
 * Builds a hierarchy tree based on the data-map given and with the given main concept as root node
 *
 * @author JohannaLatt
 * @created 25.06.2013
 */
public class HierarchyTree {

	private final HierarchyNode root;
	private final SubGraphData data;

	// A flat list with all the HierarchyNodes currently contained in the tree.
	// For checking purposes.
	private final List<HierarchyNode> containedNodes;

	public HierarchyTree(ConceptNode root, SubGraphData data) {
		if (root == null) {
			throw new NullPointerException();
		}
		this.root = new HierarchyNode(root);
		this.data = data;
		containedNodes = new LinkedList<>();
		containedNodes.add(this.root);

		addChildren(this.root);
	}

	/**
	 * Loops through the data given and recursively adds all children of the given HierarchyNode to the tree
	 *
	 * @param hn
	 * @created 25.06.2013
	 */
	private void addChildren(HierarchyNode hn) {
		for (Edge next : data.getAllEdges()) {
			// if next is a child of the HierarchyNode
			if (next.getSubject().equals(hn.getConceptNode())) {
				ConceptNode conceptChild = next.getObject();
				HierarchyNode child;
				if (treeHasNode(conceptChild)) {
					child = getHierarchyNode(conceptChild);
				}
				else {
					child = new HierarchyNode(conceptChild);
				}

				if (!hn.hasChild(child)) {
					hn.addChild(child);
					addNodeToTreeList(child);

					// avoid loops by checking for self-reference
					if (hn.getName() != child.getName()) {
						addChildren(child);
					}
				}
			}
		}
	}

	public HierarchyNode getRoot() {
		return root;
	}

	/**
	 * Adds the given HierarchyNode to the flat list representation of the tree
	 *
	 * @param hn
	 * @created 25.06.2013
	 */
	private void addNodeToTreeList(HierarchyNode hn) {
		containedNodes.add(hn);

	}

	/**
	 * Checks if the tree already contains a HierarchyNode with the given ConceptNode as source
	 *
	 * @param cn
	 * @return
	 * @created 25.06.2013
	 */
	private boolean treeHasNode(ConceptNode cn) {
		for (int i = 0; i < containedNodes.size(); i++) {
			HierarchyNode current = containedNodes.get(i);
			if (current.equals(new HierarchyNode(cn))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Loops through all the HierarchyNodes contained in the tree and returns the HierarchyNode with the given
	 * ConceptNode as source if there is one
	 *
	 * @param cn
	 * @return
	 * @created 25.06.2013
	 */
	private HierarchyNode getHierarchyNode(ConceptNode cn) {
		for (int i = 0; i < containedNodes.size(); i++) {
			HierarchyNode current = containedNodes.get(i);
			if (current.equals(new HierarchyNode(cn))) {
				return current;
			}
		}
		return null;
	}

}
